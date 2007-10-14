package org.test.zlibrary.model.test;

import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.impl.ZLModelFactory;
import junit.framework.TestCase;

public class TestTextParagraph extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void test() {
        ZLTextParagraph paragraph = factory.createParagraph();
        paragraph.addEntry(factory.createTextEntry("marina1"));
        paragraph.addEntry(factory.createTextEntry("marina2"));
        assertEquals(paragraph.getEntryNumber(), 2);
        assertEquals(paragraph.getTextLength(),14);
    }
}
