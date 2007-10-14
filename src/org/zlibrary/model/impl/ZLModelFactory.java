package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.entry.ZLTextControlEntryPool;
import org.zlibrary.model.entry.ZLTextEntry;

public class ZLModelFactory {
    public ZLTextModel createModel() {
        return new ZLTextModelImpl();
    }

    public ZLTextParagraph createParagraph() {
        return new ZLTextParagraphImpl();
    }
    
    public ZLTextControlEntry createControlEntry(byte kind, boolean isStart) {
    	return new ZLTextControlEntryImpl(kind, isStart);
    }
    
    public ZLTextEntry createTextEntry(String text) {
    	return new ZLTextEntryImpl(text);
    }
    
    public ZLTextControlEntryPool createControlEntryPool() {
    	return new ZLTextControlEntryPoolImpl();
    }
}
