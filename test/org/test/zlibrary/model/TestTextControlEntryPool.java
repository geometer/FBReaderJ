package org.test.zlibrary.model;

import junit.framework.TestCase;
import org.zlibrary.model.entry.ZLTextControlEntryPool;
import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.impl.ZLModelFactory;

public class TestTextControlEntryPool extends TestCase {
	private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void test() {
        ZLTextControlEntryPool zpool = factory.createControlEntryPool();
        byte kind = 0;
        boolean start = true;
        ZLTextParagraphEntry entry = zpool.getControlEntry(kind, start);
        assertEquals(entry, zpool.getControlEntry(kind, start));
    }
}
