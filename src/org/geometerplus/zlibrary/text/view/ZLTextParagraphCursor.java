/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.text.model.*;

public final class ZLTextParagraphCursor {
	private static final class Processor {
		private final ZLTextParagraph myParagraph;
		private final ArrayList<ZLTextElement> myElements;
		private int myOffset;
		private int myFirstMark;
		private int myLastMark;
		private final ArrayList myMarks;
		
		private Processor(ZLTextParagraph paragraph, ArrayList marks, int paragraphIndex, ArrayList<ZLTextElement> elements) {
			myParagraph = paragraph;
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
			final ArrayList<ZLTextElement> elements = myElements;
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength());
						break;
					case ZLTextParagraph.Entry.CONTROL:
						if (it.getControlIsStart()) {
							final byte hyperlinkType = it.getHyperlinkType();
							if (hyperlinkType != 0) {
								elements.add(new ZLTextHyperlinkControlElement(
									it.getControlKind(), hyperlinkType, it.getHyperlinkId()
								));
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
								elements.add(new ZLTextImageElement(imageEntry.Id, data));
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
		
		private void processTextEntry(final char[] data, final int offset, final int length) {
			if (length != 0) {
				final ZLTextElement hSpace = ZLTextElement.HSpace;
				final int end = offset + length;
				int firstNonSpace = -1;
				boolean spaceInserted = false;
				final ArrayList<ZLTextElement> elements = myElements;
				for (int charPos = offset; charPos < end; ++charPos) {
					final char ch = data[charPos];
					if ((ch == ' ') || (ch <= 0x0D)) {
						if (firstNonSpace != -1) {
							addWord(data, firstNonSpace, charPos - firstNonSpace, myOffset + (firstNonSpace - offset));
							elements.add(hSpace);
							spaceInserted = true;
							firstNonSpace = -1;					
						} else if (!spaceInserted) {
							elements.add(hSpace);
							spaceInserted = true;	
						}	
					} else if (firstNonSpace == -1) {
						firstNonSpace = charPos;
					}
				} 
				if (firstNonSpace != -1) {
					addWord(data, firstNonSpace, end - firstNonSpace, myOffset + (firstNonSpace - offset));
//					elements.add(new ZLTextWord(data, firstNonSpace, end - firstNonSpace, 0));
				}
				myOffset += length;
			}
		}
		
		private final void addWord(char[] data, int offset, int len, int paragraphOffset) {
			ZLTextWord word = new ZLTextWord(data, offset, len, paragraphOffset);
			for (int i = myFirstMark; i < myLastMark; ++i) {
				final ZLTextMark mark = (ZLTextMark)myMarks.get(i);
				if ((mark.Offset < paragraphOffset + len) && (mark.Offset + mark.Length > paragraphOffset)) {
					word.addMark(mark.Offset - paragraphOffset, mark.Length);
				}
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

	void fill() {
		ZLTextParagraph	paragraph = Model.getParagraph(Index);
		switch (paragraph.getKind()) {
			case ZLTextParagraph.Kind.TEXT_PARAGRAPH:
				new Processor(paragraph, Model.getMarks(), Index, myElements).fill();
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
