package org.test.zlibrary.model;

import junit.framework.TestCase;

import org.geometerplus.zlibrary.core.util.ZLTextBuffer;

import org.geometerplus.zlibrary.text.model.ZLTextPlainModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.impl.ZLTextForcedControlEntry;
import org.geometerplus.zlibrary.text.model.impl.ZLModelFactory;

public class TestZLTextModel extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
    
    public void testAddParagraph() {
        ZLTextPlainModel model = factory.createPlainModel(4096);
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        model.addText("marina".toCharArray());
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        assertEquals(model.getParagraphsNumber(), 2);
        //assertEquals(model.getParagraph(0), paragraph);
    }

    public void testAddEntry() {
        ZLTextPlainModel model = factory.createPlainModel(4096);
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        model.addText("marina".toCharArray());
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        assertEquals(model.getParagraphsNumber(), 2);
        model.addText("addText".toCharArray());
				{
					ZLTextParagraph.EntryIterator it = model.getParagraph(1).iterator();
					it.next();
          assertEquals(it.getType(), ZLTextParagraph.Entry.TEXT);
					String s = new String(it.getTextData(), it.getTextOffset(), it.getTextLength());
          assertEquals(s, "addText");
				}
				ZLTextBuffer buffer = new ZLTextBuffer();
				buffer.append("1".toCharArray());
				buffer.append("2".toCharArray());
        model.addText(buffer);
				{
					ZLTextParagraph.EntryIterator it = model.getParagraph(1).iterator();
					it.next();
					it.next();
          assertEquals(it.getType(), ZLTextParagraph.Entry.TEXT);
					String s = new String(it.getTextData(), it.getTextOffset(), it.getTextLength());
          assertEquals(s, "12");
				}
    }

    public void testAddControl() {
        ZLTextPlainModel model = factory.createPlainModel(4096);
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
        ZLTextPlainModel model = factory.createPlainModel(4096);
        ZLTextParagraph paragraph = factory.createParagraph();
        model.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
        //ZLTextForcedControlEntry control = factory.createForcedControlEntry();
        //model.addControl(control);
        //assertEquals(model.getParagraph(0).getEntryNumber(), 1);       
				assertFalse(true);
    }
}
