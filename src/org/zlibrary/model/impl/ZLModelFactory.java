package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextForcedControlEntry;
import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.ZLTextPlainModel;
import org.zlibrary.model.ZLTextTreeModel;
import org.zlibrary.model.ZLTextTreeParagraph;
import org.zlibrary.model.entry.ZLTextControlEntryPool;
import org.zlibrary.model.entry.ZLTextParagraphEntry;


public class ZLModelFactory {
    //models
	public ZLTextModel createModel() {
        return new ZLTextModelImpl();
    }
    
    public ZLTextPlainModel createPlainModel() {
    	return new ZLTextPlainModelImpl();
    } 
    
    public ZLTextTreeModel createZLTextTreeModel() {
    	return new ZLTextTreeModelImpl();
    }
    //paragraphs
    public ZLTextParagraph createParagraph() {
        return new ZLTextParagraphImpl();
    }
    
    public ZLTextParagraph  createSpecialParagragraph(ZLTextParagraph.Kind kind) {
    	return new ZLTextSpecialParagraphImpl(kind);
    }
    
    public ZLTextTreeParagraph createTreeParagraph(ZLTextTreeParagraph parent) {
    	return new ZLTextTreeParagraphImpl(parent);
    }
    
    public ZLTextTreeParagraph createTreeParagraph() {
    	return new ZLTextTreeParagraphImpl();
    }

    //entries
    public ZLTextControlEntryImpl createControlEntry(byte kind, boolean isStart) {
    	return new ZLTextControlEntryImpl(kind, isStart);
    }
    
    public ZLTextEntryImpl createTextEntry(String text) {
    	return new ZLTextEntryImpl(text);
    }
    
    public ZLTextParagraphEntry createHyperlinkControlEntry(byte kind, String label) {
    	return new ZLTextHyperlinkControlEntryImpl(kind, label);
    }

    public ZLTextParagraphEntry createFixedHSpaceEntry(byte lenght) {
    	return new ZLTextFixedHSpaceEntryImpl(lenght);
    }
    
    public ZLTextForcedControlEntry createForcedControlEntry() {
    	return new ZLTextForcedControlEntryImpl();
    }
    

    //pool
    public ZLTextControlEntryPool createControlEntryPool() {
    	return new ZLTextControlEntryPoolImpl();
    }
}
