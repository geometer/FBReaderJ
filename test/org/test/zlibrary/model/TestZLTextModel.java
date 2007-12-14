package org.test.zlibrary.model;

import java.util.Iterator;

import junit.framework.TestCase;

import org.zlibrary.core.util.ZLTextBuffer;

import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.ZLTextEntry;
import org.zlibrary.text.model.impl.ZLTextForcedControlEntry;
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
					Iterator<ZLTextParagraph.Entry> it = model.getParagraph(1).iterator();
					ZLTextEntry entry = (ZLTextEntry)it.next();
					String s = new String(entry.getData(), entry.getDataOffset(), entry.getDataLength());
					System.err.println(s + " ?= addText");
          assertEquals(s, "addText");
				}
				ZLTextBuffer buffer = new ZLTextBuffer();
				buffer.append("1".toCharArray());
				buffer.append("2".toCharArray());
        model.addText(buffer);
				{
					Iterator<ZLTextParagraph.Entry> it = model.getParagraph(1).iterator();
					it.next();
					ZLTextEntry entry = (ZLTextEntry)it.next();
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
