package org.test.zlibrary.model;

import org.zlibrary.text.model.entry.ZLTextControlEntry;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;
import org.zlibrary.text.model.impl.ZLModelFactory;

import junit.framework.TestCase;

public class TestTextControlEntry extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void test() {
        boolean start = true;
        byte kind = (byte)0;
        ZLTextControlEntry entry = factory.createControlEntry(kind, start);
        assertEquals(entry.isHyperlink(), false);
        assertEquals(entry.getKind(), kind);
        assertEquals(entry.isStart(), start);
    }
}
