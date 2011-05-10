/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.formats.pdb;

import java.util.*;
import java.io.*;
import java.nio.charset.CharsetDecoder;

import org.geometerplus.zlibrary.core.html.ZLByteBuffer;
import org.geometerplus.zlibrary.core.html.ZLHtmlAttributeMap;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.formats.html.HtmlReader;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.fbreader.bookmodel.BookModel;

public class MobipocketHtmlBookReader extends HtmlReader {
	private final CharsetDecoder myTocDecoder;
	private MobipocketStream myMobipocketStream;

	MobipocketHtmlBookReader(BookModel model) throws UnsupportedEncodingException {
		super(model);
		myTocDecoder = createDecoder();
	}

	public InputStream getInputStream() throws IOException {
		myMobipocketStream = new MobipocketStream(Model.Book.File);
		return myMobipocketStream;
	}

	private boolean myReadGuide;
	private int myTocStartOffset = Integer.MAX_VALUE;
	private int myTocEndOffset = Integer.MAX_VALUE;
	private final TreeMap<Integer,String> myTocEntries = new TreeMap<Integer,String>();
	private final TreeMap<Integer,Integer> myPositionToParagraph = new TreeMap<Integer,Integer>();
	private final TreeSet<Integer> myFileposReferences = new TreeSet<Integer>();
	private int myCurrentTocPosition = -1;
	private final ZLByteBuffer myTocBuffer = new ZLByteBuffer();

	private boolean tocRangeContainsPosition(int position) {
		return (myTocStartOffset <= position) && (position < myTocEndOffset);
	}

	@Override
	public void startElementHandler(byte tag, int offset, ZLHtmlAttributeMap attributes) {
		final int paragraphIndex = Model.BookTextModel.getParagraphsNumber();
		myPositionToParagraph.put(offset, paragraphIsOpen() ? paragraphIndex - 1 : paragraphIndex);
		switch (tag) {
			case HtmlTag.IMG:
			{
				final ZLByteBuffer recIndex = attributes.getValue("recindex");
				if (recIndex != null) {
					try {
						final int index = Integer.parseInt(recIndex.toString());
						if (paragraphIsOpen()) {
							endParagraph();
							addImageReference("" + index);
							beginParagraph();
						} else {
							addImageReference("" + index);
						}
					} catch (NumberFormatException e) {
					}
				}
				break;
			}
			case HtmlTag.GUIDE:
				myReadGuide = true;
				break;
			case HtmlTag.REFERENCE:
				if (myReadGuide) {
					final ZLByteBuffer fp = attributes.getValue("filepos");
					final ZLByteBuffer title = attributes.getValue("title");
					if ((fp != null) && (title != null)) {
						try {
							int filePosition = Integer.parseInt(fp.toString());
							myTocEntries.put(filePosition, title.toString(myAttributeDecoder));
							if (tocRangeContainsPosition(filePosition)) {
								myTocEndOffset = filePosition;
							}
							if (attributes.getValue("type").equalsToLCString("toc")) {
								myTocStartOffset = filePosition;
								final SortedMap<Integer,String> subMap =
									myTocEntries.tailMap(filePosition + 1);
								if (!subMap.isEmpty()) {
									myTocEndOffset = subMap.firstKey();
								}
							}
						} catch (NumberFormatException e) {
						}
					}
				}
				break;
			case HtmlTag.A:
			{
				final ZLByteBuffer fp = attributes.getValue("filepos");
				if (fp != null) {
					try {
						int filePosition = Integer.parseInt(fp.toString());
						if (tocRangeContainsPosition(offset)) {
							myCurrentTocPosition = filePosition;
							if (tocRangeContainsPosition(filePosition)) {
								myTocEndOffset = filePosition;
							}
						}
						myFileposReferences.add(filePosition);
						attributes.put(new ZLByteBuffer("href"), new ZLByteBuffer("&filepos" + filePosition));
					} catch (NumberFormatException e) {
					}
				}
				super.startElementHandler(tag, offset, attributes);
				break;
			}
			default:
				super.startElementHandler(tag, offset, attributes);
				break;
		}
	}

	@Override
	public void endElementHandler(byte tag) {
		switch (tag) {
			case HtmlTag.IMG:
				break;
			case HtmlTag.GUIDE:
				myReadGuide = false;
				break;
			case HtmlTag.REFERENCE:
				break;
			case HtmlTag.A:
				if (myCurrentTocPosition != -1) {
					if (!myTocBuffer.isEmpty()) {
						myTocEntries.put(myCurrentTocPosition, myTocBuffer.toString(myTocDecoder));
						myTocBuffer.clear();
					}
					myCurrentTocPosition = -1;
				}
				super.endElementHandler(tag);
				break;
			default:
				super.endElementHandler(tag);
				break;
		}
	}

	@Override
	public void byteDataHandler(byte[] data, int start, int length) {
		if (myCurrentTocPosition != -1) {
			myTocBuffer.append(data, start, length);
		}
		super.addByteData(data, start, length);
	}

	@Override
	public void startDocumentHandler() {
		super.startDocumentHandler();

		for (int index = 0; ; ++index) {
			final int offset = myMobipocketStream.getImageOffset(index);
			if (offset < 0) {
				break;
			}
			final int length = myMobipocketStream.getImageLength(index);
			if (length <= 0) {
				break;
			}
			addImage("" + (index + 1), new ZLFileImage(MimeType.IMAGE_AUTO, Model.Book.File, offset, length));
		}
	}

	@Override
	public void endDocumentHandler() {
		for (Integer entry: myFileposReferences) {
			final SortedMap<Integer,Integer> subMap =
				myPositionToParagraph.tailMap(entry);
			if (subMap.isEmpty()) {
				break;
			}
			addHyperlinkLabel("filepos" + entry, subMap.get(subMap.firstKey()));
		}

		for (Map.Entry<Integer,String> entry : myTocEntries.entrySet()) {
			final SortedMap<Integer,Integer> subMap =
				myPositionToParagraph.tailMap(entry.getKey());
			if (subMap.isEmpty()) {
				break;
			}
			beginContentsParagraph(subMap.get(subMap.firstKey()));
			addContentsData(entry.getValue().toCharArray());
			endContentsParagraph();
		}
		super.endDocumentHandler();
	}
}
