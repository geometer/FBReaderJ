package org.test.zlibrary.model;

import junit.framework.TestCase;
import org.zlibrary.model.impl.ZLTextEntryImpl;
import org.zlibrary.model.impl.ZLModelFactory;

public class TestTextEntry extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 

    public void test() {
        String str = "marina";
        ZLTextEntryImpl entry = factory.createTextEntry(str);
        assertEquals(entry.getData(), str);
        assertEquals(entry.getDataLength(), str.length());
    }
}
