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
		return new ZLTextParagraphImpl(new ZLTextPlainModelImpl());
	}
	
	public ZLTextParagraph createSpecialParagragraph(byte kind) {
		return new ZLTextSpecialParagraphImpl(kind, new ZLTextPlainModelImpl());
	}
	
	public ZLTextTreeParagraph createTreeParagraph(ZLTextTreeParagraph parent) {
		return new ZLTextTreeParagraphImpl(parent, new ZLTextPlainModelImpl());
	}
	
	public ZLTextTreeParagraph createTreeParagraph() {
		return new ZLTextTreeParagraphImpl(null, new ZLTextPlainModelImpl());
	}

	//entries
	public ZLTextFixedHSpaceEntry createFixedHSpaceEntry(byte lenght) {
		return new ZLTextFixedHSpaceEntry(lenght);
	}
	
	public ZLTextForcedControlEntry createForcedControlEntry() {
		return new ZLTextForcedControlEntryImpl();
	}
}
