package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.*;

import org.zlibrary.core.image.ZLImage;

import java.util.*;

public abstract class ZLTextParagraphCursor {

	private static abstract class Processor {
		protected ZLTextParagraph myParagraph;
		protected List <ZLTextElement> myElements;
		//protected int myOffset;

		protected Processor(ZLTextParagraph paragraph, int index, List <ZLTextElement> elements) {
			myParagraph = paragraph;
			myElements = elements;
			//myOffset = 0;
		}

		/*Why do we need ZLTextParagraph.Entry interface?*/

		public void fill() {
			for (ZLTextParagraph.Entry entry : myParagraph) {
				if (entry instanceof ZLTextEntry) {
					processTextEntry((ZLTextEntry) entry);
				} else if (entry instanceof ZLTextControlEntry) {
//					System.out.println("Tag = " + ((ZLTextControlEntry) entry).getKind());
					myElements.add(new ZLTextControlElement((ZLTextControlEntry)entry));
				} else if (entry instanceof ZLImageEntry) {
					ZLImageEntry imageEntry = (ZLImageEntry)entry;
					ZLImage image = imageEntry.getImage();
					if (image != null) {
						myElements.add(new ZLTextImageElement(image));
					}
				}
			}
		}
		
		public abstract void processTextEntry(ZLTextEntry textEntry);

		protected final void addWord(char[] data, int from, int to) {
			myElements.add(new ZLTextWord(data, from, to - from));
		}
	}

	private static class StandardProcessor extends Processor {
		public StandardProcessor(ZLTextParagraph paragraph, int index, List <ZLTextElement> elements) {
			super(paragraph, index, elements);
		}		
	
		/*Some useless code in C++ version here.
		  Is spaceInserted variable used for inserting one separator instead of multiple spaces?*/

		public void processTextEntry(ZLTextEntry textEntry) {
			final ZLTextElement hSpace = ZLTextElement.HSpace;
			final int length = textEntry.getDataLength();
			if (length != 0) {
				final int start = textEntry.getDataOffset();
				final int end = start + length;
				final char[] data = textEntry.getData();
				char ch;
				int firstNonSpace = -1;
				boolean spaceInserted = false;
				for (int charPos = start; charPos < end; ++charPos) {
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

	protected ZLTextModel myModel;
	protected int myIndex;
	protected List <ZLTextElement> myElements;

	protected ZLTextParagraphCursor(ZLTextModel model, int index) {
		myModel = model;
		myIndex = Math.min(index, myModel.getParagraphsNumber() - 1);
		myElements = new ArrayList <ZLTextElement> ();
		fill();
	}
	
	public static ZLTextParagraphCursor getCursor(ZLTextModel model, int index) {
		ZLTextParagraphCursor result;
		result = new ZLTextPlainParagraphCursor(model, index);
		return result;
	}

	/*Is it ok to create new instance of Processor here?*/

	protected void fill() {
		ZLTextParagraph	paragraph = myModel.getParagraph(myIndex);
		if (paragraph.getKind() == ZLTextParagraph.Kind.TEXT_PARAGRAPH) {
			(new StandardProcessor(paragraph, myIndex, myElements)).fill();
		}
	}
	
	protected void clear() {
		myElements.clear();
	}

	/*Something strange here*/

	public boolean isNull() {
		return myModel == null;
	}
	
	public boolean isFirst() {
		return myIndex == 0;
	}

	public abstract boolean isLast(); 
	
	public boolean isEndOfSection() {
		return (myModel.getParagraph(myIndex).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);	
	}
	
	public int getParagraphLength() {
		return myElements.size();
	}

	public int getIndex() {
		return myIndex;
	}

	abstract public ZLTextParagraphCursor previous();
	abstract public ZLTextParagraphCursor next();
	
	public ZLTextElement getElement(int index) {
		return myElements.get(index);
	}

	public ZLTextParagraph getParagraph() {
		return myModel.getParagraph(myIndex);	
	}
}


