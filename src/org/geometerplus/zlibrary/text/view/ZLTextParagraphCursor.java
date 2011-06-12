/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view;

import java.util.*;
import org.vimgadgets.linebreak.LineBreaker;

import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.text.model.*;

public final class ZLTextParagraphCursor {
	private static final class Processor {
		private final ZLTextParagraph myParagraph;
		private final LineBreaker myLineBreaker;
		private final ArrayList<ZLTextElement> myElements;
		private int myOffset;
		private int myFirstMark;
		private int myLastMark;
		private final List<ZLTextMark> myMarks;

		private Processor(ZLTextParagraph paragraph, LineBreaker lineBreaker, List<ZLTextMark> marks, int paragraphIndex, ArrayList<ZLTextElement> elements) {
			myParagraph = paragraph;
			myLineBreaker = lineBreaker;
			myElements = elements;
			myMarks = marks;
			final ZLTextMark mark = new ZLTextMark(paragraphIndex, 0, 0);
			int i;
			for (i = 0; i < myMarks.size(); i++) {
				if (((ZLTextMark)myMarks.get(i)).compareTo(mark) >= 0) {
					break;
				}
			}
			myFirstMark = i;
			myLastMark = myFirstMark;
			for (; (myLastMark != myMarks.size()) && (((ZLTextMark)myMarks.get(myLastMark)).ParagraphIndex == paragraphIndex); myLastMark++);
			myOffset = 0;
		}

		void fill() {
			int hyperlinkDepth = 0;
			ZLTextHyperlink hyperlink = null;

			final ArrayList<ZLTextElement> elements = myElements;
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength(), hyperlink);
						break;
					case ZLTextParagraph.Entry.CONTROL:
						if (hyperlink != null) {
							hyperlinkDepth += it.getControlIsStart() ? 1 : -1;
							if (hyperlinkDepth == 0) {
								hyperlink = null;
							}
						}
						if (it.getControlIsStart()) {
							final byte hyperlinkType = it.getHyperlinkType();
							if (hyperlinkType != 0) {
								final ZLTextHyperlinkControlElement control =
									new ZLTextHyperlinkControlElement(
										it.getControlKind(), hyperlinkType, it.getHyperlinkId()
									);
								elements.add(control);
								hyperlink = control.Hyperlink;
								hyperlinkDepth = 1;
								break;
							}
						}
						elements.add(ZLTextControlElement.get(it.getControlKind(), it.getControlIsStart()));
						break;
					case ZLTextParagraph.Entry.IMAGE:
						final ZLImageEntry imageEntry = it.getImageEntry();
						final ZLImage image = imageEntry.getImage();
						if (image != null) {
							ZLImageData data = ZLImageManager.Instance().getImageData(image);
							if (data != null) {
								if (hyperlink != null) {
									hyperlink.addElementIndex(elements.size());
								}
								elements.add(new ZLTextImageElement(imageEntry.Id, data, image.getURI()));
							}
						}
						break;
					case ZLTextParagraph.Entry.FORCED_CONTROL:
						// TODO: implement
						break;
					case ZLTextParagraph.Entry.FIXED_HSPACE:
						elements.add(ZLTextFixedHSpaceElement.getElement(it.getFixedHSpaceLength()));
						break;
				}
			}
		}

		private static byte[] ourBreaks = new byte[1024];
		private static final int NO_SPACE = 0;
		private static final int SPACE = 1;
		//private static final int NON_BREAKABLE_SPACE = 2;
		private void processTextEntry(final char[] data, final int offset, final int length, ZLTextHyperlink hyperlink) {
			if (length != 0) {
				if (ourBreaks.length < length) {
					ourBreaks = new byte[length];
				}
				final byte[] breaks = ourBreaks;
				myLineBreaker.setLineBreaks(data, offset, length, breaks);

				final ZLTextElement hSpace = ZLTextElement.HSpace;
				final ArrayList<ZLTextElement> elements = myElements;
				char ch = 0;
				char previousChar = 0;
				int spaceState = NO_SPACE;
				int wordStart = 0;
				for (int index = 0; index < length; ++index) {
					previousChar = ch;
					ch = data[offset + index];
					if (Character.isSpace(ch)) {
						if (index > 0 && spaceState == NO_SPACE) {
							addWord(data, offset + wordStart, index - wordStart, myOffset + wordStart, hyperlink);
						}
						spaceState = SPACE;
					} else {
						switch (spaceState) {
							case SPACE:
								//if (breaks[index - 1] == LineBreak.NOBREAK || previousChar == '-') {
								//}
								elements.add(hSpace);
								wordStart = index;
								break;
							//case NON_BREAKABLE_SPACE:
								//break;
							case NO_SPACE:
								if (index > 0 &&
									breaks[index - 1] != LineBreaker.NOBREAK &&
									previousChar != '-' &&
									index != wordStart) {
									addWord(data, offset + wordStart, index - wordStart, myOffset + wordStart, hyperlink);
									wordStart = index;
								}
								break;
						}
						spaceState = NO_SPACE;
					}
				}
				switch (spaceState) {
					case SPACE:
						elements.add(hSpace);
						break;
					//case NON_BREAKABLE_SPACE:
						//break;
					case NO_SPACE:
						addWord(data, offset + wordStart, length - wordStart, myOffset + wordStart, hyperlink);
						break;
				}
				myOffset += length;
			}
		}

		private final void addWord(char[] data, int offset, int len, int paragraphOffset, ZLTextHyperlink hyperlink) {
			ZLTextWord word = new ZLTextWord(data, offset, len, paragraphOffset);
			for (int i = myFirstMark; i < myLastMark; ++i) {
				final ZLTextMark mark = (ZLTextMark)myMarks.get(i);
				if ((mark.Offset < paragraphOffset + len) && (mark.Offset + mark.Length > paragraphOffset)) {
					word.addMark(mark.Offset - paragraphOffset, mark.Length);
				}
			}
			if (hyperlink != null) {
				hyperlink.addElementIndex(myElements.size());
			}
			myElements.add(word);
		}
	}

	public final int Index;
	public final ZLTextModel Model;
	private final ArrayList<ZLTextElement> myElements = new ArrayList<ZLTextElement>();

	private ZLTextParagraphCursor(ZLTextModel model, int index) {
		Model = model;
		Index = Math.min(index, Model.getParagraphsNumber() - 1);
		fill();
	}

	static ZLTextParagraphCursor cursor(ZLTextModel model, int index) {
		ZLTextParagraphCursor result = ZLTextParagraphCursorCache.get(model, index);
		if (result == null) {
			result = new ZLTextParagraphCursor(model, index);
			ZLTextParagraphCursorCache.put(model, index, result);
		}
		return result;
	}

	private static final char[] SPACE_ARRAY = { ' ' };
	void fill() {
		ZLTextParagraph	paragraph = Model.getParagraph(Index);
		switch (paragraph.getKind()) {
			case ZLTextParagraph.Kind.TEXT_PARAGRAPH:
				new Processor(paragraph, new LineBreaker(Model.getLanguage()), Model.getMarks(), Index, myElements).fill();
				break;
			case ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH:
				myElements.add(new ZLTextWord(SPACE_ARRAY, 0, 1, 0));
				break;
			default:
				break;
		}
	}

	void clear() {
		myElements.clear();
	}

	public boolean isFirst() {
		return Index == 0;
	}

	public boolean isLast() {
		return (Index + 1 >= Model.getParagraphsNumber());
	}

	public boolean isEndOfSection() {
		return (Model.getParagraph(Index).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}

	int getParagraphLength() {
		return myElements.size();
	}

	public ZLTextParagraphCursor previous() {
		return isFirst() ? null : cursor(Model, Index - 1);
	}

	public ZLTextParagraphCursor next() {
		return isLast() ? null : cursor(Model, Index + 1);
	}

	ZLTextElement getElement(int index) {
		try {
			return myElements.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	ZLTextParagraph getParagraph() {
		return Model.getParagraph(Index);
	}

	@Override
	public String toString() {
		return "ZLTextParagraphCursor [" + Index + " (0.." + myElements.size() + ")]";
	}
}
