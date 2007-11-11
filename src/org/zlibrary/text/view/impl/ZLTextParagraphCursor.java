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

		public void fill() {
			List <ZLTextParagraphEntry> entries = myParagraph.getEntries();
			for (ZLTextParagraphEntry entry : entries) {
	//			switch (entry.Kind) {
	//				case ZLTextParagraphEntry.Kind.TEXT_ENTRY: {
						processTextEntry((ZLTextEntry) entry);
	//					break;
	//				}
	//			}
			}
		}
		
		public abstract void processTextEntry(ZLTextEntry textEntry);

		protected void addWord(String s, int offset, int len) {
			myElements.add(new ZLTextWord(s, (short) len, offset));
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


