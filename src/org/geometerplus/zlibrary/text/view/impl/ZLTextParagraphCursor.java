/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view.impl;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.text.model.*;

public abstract class ZLTextParagraphCursor {
	private static final class Processor {
		protected final ZLTextParagraph myParagraph;
		protected final ArrayList myElements;
		protected int myOffset;
		protected int myFirstMark;
		protected int myLastMark;
		protected final ArrayList myMarks;
		
		protected Processor(ZLTextParagraph paragraph, ArrayList marks, int paragraphIndex, ArrayList elements) {
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
			final ArrayList elements = myElements;
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength());
						break;
					case ZLTextParagraph.Entry.CONTROL:
						if (it.getControlIsStart() && it.getControlIsHyperlink()) {
							elements.add(new ZLTextHyperlinkControlElement(it.getControlKind(), it.getHyperlinkControlLabel()));
						} else {
							elements.add(ZLTextControlElement.get(it.getControlKind(), it.getControlIsStart()));
						}
						break;
					case ZLTextParagraph.Entry.IMAGE:
						final ZLImageEntry imageEntry = it.getImageEntry();
						final ZLImage image = imageEntry.getImage();
						if (image != null) {
							ZLImageData data = ZLImageManager.getInstance().getImageData(image);
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
				final ArrayList elements = myElements;
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
		
		protected final void addWord(char[] data, int offset, int len, int paragraphOffset) {
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

	final public int Index;
	final protected ZLTextModel myModel;
	final protected ArrayList myElements = new ArrayList();

	protected ZLTextParagraphCursor(ZLTextModel model, int index) {
		myModel = model;
		Index = Math.min(index, myModel.getParagraphsNumber() - 1);
		fill();
	}
	
	static ZLTextParagraphCursor cursor(ZLTextModel model, int index) {
		ZLTextParagraphCursor result = ZLTextParagraphCursorCache.get(model, index);
		if (result == null) {
			if (model instanceof ZLTextTreeModel) {
				result = new ZLTextTreeParagraphCursor((ZLTextTreeModel)model, index);
			} else {
				result = new ZLTextPlainParagraphCursor(model, index);
			}
			ZLTextParagraphCursorCache.put(model, index, result);
		}
		return result;
	}

	/*Is it ok to create new instance of Processor here?*/

	protected void fill() {
		ZLTextParagraph	paragraph = myModel.getParagraph(Index);
		switch (paragraph.getKind()) {
			case ZLTextParagraph.Kind.TEXT_PARAGRAPH:
			case ZLTextParagraph.Kind.TREE_PARAGRAPH:
				new Processor(paragraph, myModel.getMarks(), Index, myElements).fill();
				break;
			default:
				break;
		}
	}
	
	protected void clear() {
		myElements.clear();
	}

	boolean isFirst() {
		return Index == 0;
	}

	abstract boolean isLast(); 
	
	boolean isEndOfSection() {
		return (myModel.getParagraph(Index).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);	
	}
	
	final int getParagraphLength() {
		return myElements.size();
	}

	abstract ZLTextParagraphCursor previous();
	abstract ZLTextParagraphCursor next();
	
	final ZLTextElement getElement(int index) {
		return (ZLTextElement)myElements.get(index);
	}

	ZLTextParagraph getParagraph() {
		return myModel.getParagraph(Index);	
	}
}
