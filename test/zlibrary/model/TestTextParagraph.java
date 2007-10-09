package org.test.zlibrary.model;

import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.impl.ZLTextParagraphImpl;
import org.zlibrary.model.impl.entry.ZLTextEntryImpl;
import junit.framework.TestCase;

public class TestTextParagraph extends TestCase {
    public void test() {
        ZLTextParagraph paragraph = new ZLTextParagraphImpl();
        paragraph.addEntry(new ZLTextEntryImpl("marina1"));
        paragraph.addEntry(new ZLTextEntryImpl("marina2"));
        assertEquals(paragraph.getEntryNumber(), 2);
        assertEquals(paragraph.getTextLength(),14);
    }
}
