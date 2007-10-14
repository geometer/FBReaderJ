package org.test.zlibrary.model.test;

import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.impl.ZLModelFactory;
import junit.framework.TestCase;

public class TestTextControlEntry extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void test() {
        boolean start = true;
        byte kind = (byte)0;
        ZLTextControlEntry entry = factory.createControlEntry(kind, start);
        assertEquals(entry.getKind(), kind);
        assertEquals(entry.isStart(), start);
    }
}
