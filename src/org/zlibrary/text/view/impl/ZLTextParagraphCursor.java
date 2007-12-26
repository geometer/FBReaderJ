package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLImageEntry;

import org.zlibrary.core.image.ZLImage;

import java.util.*;

abstract class ZLTextParagraphCursor {
	private static abstract class Processor {
		protected ZLTextParagraph myParagraph;
		protected ArrayList<ZLTextElement> myElements;
		//protected int myOffset;

		protected Processor(ZLTextParagraph paragraph, int index, ArrayList<ZLTextElement> elements) {
			myParagraph = paragraph;
			myElements = elements;
			//myOffset = 0;
		}

		/*Why do we need ZLTextParagraph.Entry interface?*/

		void fill() {
			//for (ZLTextParagraph.Entry entry : myParagraph) {
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.hasNext(); ) {
				ZLTextParagraph.Entry entry = it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						//processTextEntry((ZLTextEntry)entry);
						processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength());
						break;
					case ZLTextParagraph.Entry.CONTROL:
//					System.out.println("Tag = " + ((ZLTextControlEntry) entry).getKind());
						//myElements.add(new ZLTextControlElement((ZLTextControlEntry)entry));
						myElements.add(ZLTextControlElement.get(it.getControlKind(), it.getControlIsStart()));
						break;
					case ZLTextParagraph.Entry.IMAGE:
						ZLImageEntry imageEntry = (ZLImageEntry)entry;
						ZLImage image = imageEntry.getImage();
						if (image != null) {
							myElements.add(new ZLTextImageElement(image));
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
		ZLTextParagraphCursor result;
		if (model instanceof ZLTextTreeModel) {
			result = new ZLTextTreeParagraphCursor((ZLTextTreeModel)model, index);
		} else {
			result = new ZLTextPlainParagraphCursor(model, index);
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

	/*Something strange here*/

	boolean isNull() {
		return myModel == null;
	}

	boolean isFirst() {
		return myIndex == 0;
	}

	abstract boolean isLast(); 
	
	boolean isEndOfSection() {
		return (myModel.getParagraph(myIndex).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);	
	}
	
	int getParagraphLength() {
		return myElements.size();
	}

	int getIndex() {
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


