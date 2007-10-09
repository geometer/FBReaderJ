package org.test.zlibrary.model;

import junit.framework.TestCase;
import org.zlibrary.model.entry.ZLTextEntry;
import org.zlibrary.model.impl.entry.ZLTextEntryImpl;

public class TestTextEntry extends TestCase {

    public void test() {
        String str = "marina";
        ZLTextEntry entry = new ZLTextEntryImpl(str);
        assertEquals(entry.getData(), str);
        assertEquals(entry.getDataLength(), str.length());
    }
}
