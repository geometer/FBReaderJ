package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.*;

import org.zlibrary.core.image.*;

import java.util.*;

public abstract class ZLTextParagraphCursor {
	private static abstract class Processor {
		protected ZLTextParagraph myParagraph;
		protected ArrayList<ZLTextElement> myElements;
		//protected int myOffset;

		protected Processor(ZLTextParagraph paragraph, int index, ArrayList<ZLTextElement> elements) {
			myParagraph = paragraph;
			myElements = elements;
			//myOffset = 0;
		}

		void fill() {
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength());
						break;
					case ZLTextParagraph.Entry.CONTROL:
						myElements.add(ZLTextControlElement.get(it.getControlKind(), it.getControlIsStart()));
						break;
					case ZLTextParagraph.Entry.IMAGE:
						ZLImage image = it.getImageEntry().getImage();
						if (image != null) {
							ZLImageData data = ZLImageManager.getInstance().getImageData(image);
							if (data != null) {
								myElements.add(new ZLTextImageElement(data));
							}
						}
						break;
					case ZLTextParagraph.Entry.FORCED_CONTROL:
						// TODO: implement
						break;
					case ZLTextParagraph.Entry.FIXED_HSPACE:
						// TODO: implement
						break;
				}
			}
		}
		
		abstract void processTextEntry(final char[] data, final int offset, final int length);

		protected final void addWord(char[] data, int from, int to) {
			myElements.add(new ZLTextWord(data, from, to - from));
		}
	}

	private static final class StandardProcessor extends Processor {
		StandardProcessor(ZLTextParagraph paragraph, int index, ArrayList<ZLTextElement> elements) {
			super(paragraph, index, elements);
		}		
	
		/*Some useless code in C++ version here.
			Is spaceInserted variable used for inserting one separator instead of multiple spaces?*/

		void processTextEntry(final char[] data, final int offset, final int length) {
			if (length != 0) {
				final ZLTextElement hSpace = ZLTextElement.HSpace;
				final int end = offset + length;
				char ch;
				int firstNonSpace = -1;
				boolean spaceInserted = false;
				for (int charPos = offset; charPos < end; ++charPos) {
					if (Character.isWhitespace(data[charPos])) {
						if (firstNonSpace != -1) {
							addWord(data, firstNonSpace, charPos);
							myElements.add(hSpace);
							spaceInserted = true;
							firstNonSpace = -1;					
						} else if (!spaceInserted) {
							myElements.add(hSpace);
							spaceInserted = true;	
						}	
					} else if (firstNonSpace == -1) {
						firstNonSpace = charPos;
					}
				} 
				if (firstNonSpace != -1) {
					addWord(data, firstNonSpace, end);
				}
				//myOffset += length;
			}
		}
	}

	protected final ZLTextModel myModel;
	protected int myIndex;
	protected final ArrayList<ZLTextElement> myElements = new ArrayList<ZLTextElement>();

	protected ZLTextParagraphCursor(ZLTextModel model, int index) {
		myModel = model;
		myIndex = Math.min(index, myModel.getParagraphsNumber() - 1);
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
		ZLTextParagraph	paragraph = myModel.getParagraph(myIndex);
		switch (paragraph.getKind()) {
			case ZLTextParagraph.Kind.TEXT_PARAGRAPH:
			case ZLTextParagraph.Kind.TREE_PARAGRAPH:
				new StandardProcessor(paragraph, myIndex, myElements).fill();
				break;
			default:
				break;
		}
	}
	
	protected void clear() {
		myElements.clear();
	}

	boolean isFirst() {
		return myIndex == 0;
	}

	abstract boolean isLast(); 
	
	boolean isEndOfSection() {
		return (myModel.getParagraph(myIndex).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);	
	}
	
	final int getParagraphLength() {
		return myElements.size();
	}

	public final int getIndex() {
		return myIndex;
	}

	abstract ZLTextParagraphCursor previous();
	abstract ZLTextParagraphCursor next();
	
	ZLTextElement getElement(int index) {
		return myElements.get(index);
	}

	ZLTextParagraph getParagraph() {
		return myModel.getParagraph(myIndex);	
	}
}
