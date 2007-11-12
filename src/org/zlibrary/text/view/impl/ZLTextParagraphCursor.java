package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;
import org.zlibrary.text.model.entry.ZLTextEntry;

import java.util.*;

abstract class ZLTextParagraphCursor {
	

	private static abstract class Processor {
		protected ZLTextParagraph myParagraph;
		protected List <ZLTextElement> myElements;
		protected int myOffset;

		protected Processor(ZLTextParagraph paragraph, int index, List <ZLTextElement> elements) {
			myParagraph = paragraph;
			myElements = elements;
			myOffset = 0;
		}

		/*Why do we need ZLTextParagraphEntry interface?*/

		public void fill() {
			List <ZLTextParagraphEntry> entries = myParagraph.getEntries();
			for (ZLTextParagraphEntry entry : entries) {
				if (entry instanceof ZLTextEntry) {
					processTextEntry((ZLTextEntry) entry);
				}
			}
		}
		
		public abstract void processTextEntry(ZLTextEntry textEntry);

		protected void addWord(String s, int offset, int len) {
			myElements.add(new ZLTextWord(s, (short) len, offset));
		}
	}

	private static class StandardProcessor extends Processor {
		public StandardProcessor(ZLTextParagraph paragraph, int index, List <ZLTextElement> elements) {
			super(paragraph, index, elements);
		}		
	
		/*Some useless code in C++ version here.
		  Is spaceInserted variable used for inserting one separator for multiple spaces?*/

		public void processTextEntry(ZLTextEntry textEntry) {
			int dataLength = textEntry.getDataLength();
			if (dataLength != 0) {
				String data = textEntry.getData();
				char ch;
				int firstNonSpace = 0;
				boolean spaceInserted = false;
				for (int charPos = 0; charPos < data.length(); charPos++) {
					char current = data.charAt(charPos);
					if (current == ' ') {
						if (firstNonSpace != 0) {
							addWord(data.substring(firstNonSpace, charPos), myOffset + (firstNonSpace - data.length()), 
								charPos - firstNonSpace);
							myElements.add(new ZLTextHSpaceElement());
							spaceInserted = true;
							firstNonSpace = 0;					
						} else if (!spaceInserted) {
							myElements.add(new ZLTextHSpaceElement());
							spaceInserted = true;	
						}	
					} else if (firstNonSpace == 0) {
						firstNonSpace = charPos;
					}
				} 
				if (firstNonSpace != 0) {
					addWord(data.substring(firstNonSpace, data.length()), myOffset + (firstNonSpace - data.length()), 
							data.length() - firstNonSpace);
				}
				myOffset += data.length();
			}
		}
	}

	protected ZLTextModel myModel;
	protected int myIndex;
	protected List <ZLTextElement> myElements;

	protected ZLTextParagraphCursor(ZLTextModel model, int index) {
		myModel = model;
		myIndex = Math.min(index, myModel.getParagraphsNumber() - 1);
		fill();
	}
	
	public static ZLTextParagraphCursor getCursor(ZLTextModel model, int index) {
		ZLTextParagraphCursor result;
		result = new ZLTextPlainParagraphCursor(model, index);
		return result;
	}

	protected void fill() {
		ZLTextParagraph	paragraph = myModel.getParagraph(myIndex);
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


