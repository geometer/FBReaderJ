package org.test.zlibrary.model;

import junit.framework.TestCase;
import org.zlibrary.model.entry.ZLTextControlEntryPool;
import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.impl.entry.ZLTextControlEntryPoolImpl;

public class TestTextControlEntryPool extends TestCase {
    public void test() {
        ZLTextControlEntryPool zpool = new ZLTextControlEntryPoolImpl();
        byte kind = 0;
        boolean start = true;
        ZLTextParagraphEntry entry = zpool.getControlEntry(kind, start);
        assertEquals(entry, zpool.getControlEntry(kind, start));
    }
}
