package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.entry.ZLTextControlEntry;
import org.zlibrary.text.model.entry.ZLTextEntry;
import org.zlibrary.text.model.entry.ZLTextFixedHSpaceEntry;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.entry.ZLTextHyperlinkControlEntry;


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
    	return new ZLTextTreeParagraphImpl(null);
    }

    //entries
    public ZLTextControlEntry createControlEntry(byte kind, boolean isStart) {
    	return new ZLTextControlEntryImpl(kind, isStart);
    }
    
    public ZLTextEntry createTextEntry(String text) {
    	return new ZLTextEntryImpl(text.toCharArray());
    }
    
    public ZLTextHyperlinkControlEntry createHyperlinkControlEntry(byte kind, String label) {
    	return new ZLTextHyperlinkControlEntryImpl(kind, label);
    }

    public ZLTextFixedHSpaceEntry createFixedHSpaceEntry(byte lenght) {
    	return new ZLTextFixedHSpaceEntryImpl(lenght);
    }
    
    public ZLTextForcedControlEntry createForcedControlEntry() {
    	return new ZLTextForcedControlEntryImpl();
    }
}
