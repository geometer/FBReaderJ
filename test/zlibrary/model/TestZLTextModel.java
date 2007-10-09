package org.test.zlibrary.model;

import junit.framework.TestCase;
import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.entry.ZLTextEntry;
import org.zlibrary.model.impl.ZLTextModelImpl;
import org.zlibrary.model.impl.ZLTextParagraphImpl;
import org.zlibrary.model.impl.entry.ZLTextEntryImpl;
import org.zlibrary.model.impl.entry.ZLTextControlEntryImpl;

import java.util.List;
import java.util.LinkedList;

public class TestZLTextModel extends TestCase {
     
    public void testAddParagraph() {
        ZLTextModel model = new ZLTextModelImpl();
        ZLTextParagraph paragraph = new ZLTextParagraphImpl();
        paragraph.addEntry(new ZLTextEntryImpl("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(new ZLTextParagraphImpl());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
    }

    public void testAddEntry() {
        ZLTextModel model = new ZLTextModelImpl();
        ZLTextParagraph paragraph = new ZLTextParagraphImpl();
        paragraph.addEntry(new ZLTextEntryImpl("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(new ZLTextParagraphImpl());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
        model.addText("addText");
        assertEquals(((ZLTextEntry)model.getParagraph(1).getEntries().get(0)).getData(), "addText");
        List<String> list = new LinkedList<String>();
        list.add("1");
        list.add("2");
        model.addText(list);
        assertEquals(((ZLTextEntry)model.getParagraph(1).getEntries().get(1)).getData(), "12");
    }

    public void testAddControl() {
        ZLTextModel model = new ZLTextModelImpl();
        ZLTextParagraph paragraph = new ZLTextParagraphImpl();
        paragraph.addEntry(new ZLTextEntryImpl("marina"));
        model.addParagraphInternal(paragraph);
        model.addParagraphInternal(new ZLTextParagraphImpl());
        assertEquals(model.getParagraphsNumber(), 2);
        assertEquals(model.getParagraph(0), paragraph);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);
        model.addControl((byte)1, true);
        model.addControl((byte)1, false);
        model.addControl((byte)0, true);
        model.addControl((byte)0, false);
        
    }

}
