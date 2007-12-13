package org.test.zlibrary.model;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextEntry;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.impl.ZLModelFactory;

public class TestZLTextModel extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
    
    public void testAddParagraph() {
        ZLTextPlainModel model = factory.createPlainModel();
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        model.addText("marina".toCharArray());
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        assertEquals(model.getParagraphsNumber(), 2);
        //assertEquals(model.getParagraph(0), paragraph);
    }

    public void testAddEntry() {
        ZLTextPlainModel model = factory.createPlainModel();
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        model.addText("marina".toCharArray());
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        assertEquals(model.getParagraphsNumber(), 2);
        model.addText("addText".toCharArray());
				{
					ZLTextEntry entry = (ZLTextEntry)model.getParagraph(1).getEntries().get(0);
					String s = new String(entry.getData(), entry.getDataOffset(), entry.getDataLength());
					System.err.println(s + " ?= addText");
          assertEquals(s, "addText");
				}
        ArrayList<char[]> sb = new ArrayList<char[]>();
        sb.add("1".toCharArray());
        sb.add("2".toCharArray());
        model.addText(sb);
				{
					ZLTextEntry entry = (ZLTextEntry)model.getParagraph(1).getEntries().get(1);
					String s = new String(entry.getData(), entry.getDataOffset(), entry.getDataLength());
					System.err.println(s + " ?= 12");
          assertEquals(s, "12");
				}
    }

    public void testAddControl() {
        ZLTextPlainModel model = factory.createPlainModel();
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        model.addText("marina".toCharArray());
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        assertEquals(model.getParagraphsNumber(), 2);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);
        model.addControl((byte)1, true);
        model.addControl((byte)1, false);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);   
    }
    
    public void testAddForcedControl() {
        ZLTextPlainModel model = factory.createPlainModel();
        ZLTextParagraph paragraph = factory.createParagraph();
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        ZLTextForcedControlEntry control = factory.createForcedControlEntry();
        model.addControl(control);
        assertEquals(model.getParagraph(0).getEntryNumber(), 1);       
    }
}
