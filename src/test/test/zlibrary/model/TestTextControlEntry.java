package org.test.zlibrary.model;

import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.impl.entry.ZLTextControlEntryImpl;
import junit.framework.TestCase;

public class TestTextControlEntry extends TestCase {
    public void test() {
        boolean start = true;
        byte kind = (byte)0;
        ZLTextControlEntry entry = new ZLTextControlEntryImpl(kind, start);
        assertEquals(entry.getKind(), kind);
        assertEquals(entry.isStart(), start);
    }
}
