package org.test.zlibrary.model;

import org.zlibrary.model.ZLTextParagraphEntry;
import org.zlibrary.model.impl.ZLModelFactory;
import org.zlibrary.model.impl.ZLTextControlEntryImpl;

import junit.framework.TestCase;

public class TestTextControlEntry extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void test() {
        boolean start = true;
        byte kind = (byte)0;
        ZLTextControlEntryImpl entry = factory.createControlEntry(kind, start);
        assertEquals(entry.isHyperlink(), false);
        assertEquals(entry.getKind(), kind);
        assertEquals(entry.isStart(), start);
    }
}
