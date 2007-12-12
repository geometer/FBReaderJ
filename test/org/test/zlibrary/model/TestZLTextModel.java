package org.test.zlibrary.model;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextEntry;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.impl.ZLModelFactory;

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
        model.addText("addText".toCharArray());
        assertEquals(new String(((ZLTextEntry)(model.getParagraph(1).getEntries().get(0))).getData()), "addText");
        ArrayList<char[]> sb = new ArrayList<char[]>();
        sb.add("1".toCharArray());
        sb.add("2".toCharArray());
        model.addText(sb);
        assertEquals(new String(((ZLTextEntry)model.getParagraph(1).getEntries().get(1)).getData()), "12");
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
