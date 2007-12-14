package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;


public class ZLModelFactory {
	//models
	public ZLTextPlainModel createPlainModel() {
		return new ZLTextPlainModelImpl();
	} 
	
	public ZLTextTreeModel createZLTextTreeModel() {
		return new ZLTextTreeModelImpl();
	}

	//paragraphs
	public ZLTextParagraph createParagraph() {
		return new ZLTextParagraphImpl(new ArrayList<ZLTextParagraph.Entry>());
	}
	
	public ZLTextParagraph createSpecialParagragraph(ZLTextParagraph.Kind kind) {
		return new ZLTextSpecialParagraphImpl(kind, new ArrayList<ZLTextParagraph.Entry>());
	}
	
	public ZLTextTreeParagraph createTreeParagraph(ZLTextTreeParagraph parent) {
		return new ZLTextTreeParagraphImpl(parent, new ArrayList<ZLTextParagraph.Entry>());
	}
	
	public ZLTextTreeParagraph createTreeParagraph() {
		return new ZLTextTreeParagraphImpl(null, new ArrayList<ZLTextParagraph.Entry>());
	}

	//entries
	public ZLTextControlEntry createControlEntry(byte kind, boolean isStart) {
		return new ZLTextControlEntry(kind, isStart);
	}
	
	public ZLTextEntry createTextEntry(String text) {
		char[] array = text.toCharArray();
		return new ZLTextEntryImpl(array, 0, array.length);
	}
	
	public ZLTextHyperlinkControlEntry createHyperlinkControlEntry(byte kind, String label) {
		return new ZLTextHyperlinkControlEntry(kind, label);
	}

	public ZLTextFixedHSpaceEntry createFixedHSpaceEntry(byte lenght) {
		return new ZLTextFixedHSpaceEntry(lenght);
	}
	
	public ZLTextForcedControlEntry createForcedControlEntry() {
		return new ZLTextForcedControlEntryImpl();
	}
}
