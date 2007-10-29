package org.test.zlibrary.model;

import junit.framework.TestCase;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.entry.ZLTextEntry;
import org.zlibrary.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.model.impl.ZLModelFactory;

public class TestZLTextModel extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
    
    public void testAddParagraph() {
        ZLTextModel model = factory.createModel();
        ZLTextParagraph paragraph = factory.createParagraph();
        paragraph.addEntry(factory.createTextEntry("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(factory.createParagraph());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
    }

    public void testAddEntry() {
        ZLTextModel model = factory.createModel();
        ZLTextParagraph paragraph = factory.createParagraph();
        paragraph.addEntry(factory.createTextEntry("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(factory.createParagraph());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
        model.addText("addText");
        assertEquals(((ZLTextEntry)(model.getParagraph(1).getEntries().get(0))).getData(), "addText");
        StringBuffer sb = new StringBuffer();
        sb.append("1");
        sb.append("2");
        model.addText(sb);
        assertEquals(((ZLTextEntry)model.getParagraph(1).getEntries().get(1)).getData(), "12");
    }

    public void testAddControl() {
        ZLTextModel model = factory.createModel();
        ZLTextParagraph paragraph = factory.createParagraph();
        paragraph.addEntry(factory.createTextEntry("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(factory.createParagraph());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);
        model.addControl((byte)1, true);
        model.addControl((byte)1, false);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);   
    }
    
    public void testAddForcedControl() {
        ZLTextModel model = factory.createModel();
        ZLTextParagraph paragraph = factory.createParagraph();
        model.addParagraphInternal(factory.createParagraph());
        ZLTextForcedControlEntry control = factory.createForcedControlEntry();
        model.addControl(control);
        assertEquals(model.getParagraph(0).getEntryNumber(), 1);       
    }
}
