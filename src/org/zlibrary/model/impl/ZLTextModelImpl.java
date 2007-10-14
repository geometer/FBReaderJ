package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextEntry;
import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.entry.ZLTextControlEntryPool;
import java.util.LinkedList;

class ZLTextModelImpl implements ZLTextModel {
    private LinkedList<ZLTextParagraph> myParagraphs = new LinkedList<ZLTextParagraph>();
    private ZLTextControlEntryPool myEntryPool = new ZLTextControlEntryPoolImpl();

    public Kind getKind() {
        return Kind.PLAIN_TEXT_MODEL;
    }

    public int getParagraphsNumber() {
        return myParagraphs.size();
    }

    public ZLTextParagraph getParagraph(int index) {
        return myParagraphs.get(index);
    }

    public void addParagraphInternal(ZLTextParagraph paragraph) {
        this.myParagraphs.add(paragraph);
    }

    public void addControl(byte textKind, boolean isStart) {
    	myParagraphs.getLast().addEntry(myEntryPool.getControlEntry(textKind, isStart));
    }

    public void addText(String text) {
        myParagraphs.getLast().addEntry(new ZLTextEntryImpl(text));
    }

    public void addText(StringBuffer text) {
        ZLTextParagraph paragraph = myParagraphs.getLast();
        paragraph.addEntry(new ZLTextEntryImpl(text.toString()));
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (ZLTextParagraph paragraph: myParagraphs) {
            sb.append("[PARAGRAPH]\n");
            for (ZLTextParagraphEntry entry: paragraph.getEntries()) {
                if (entry instanceof ZLTextEntry) {
                    sb.append("[TEXT]");
                    sb.append(((ZLTextEntry)entry).getData());
                    sb.append("[/TEXT]");
                } else if (entry instanceof ZLTextControlEntry) {
                    ZLTextControlEntry entryControl = (ZLTextControlEntry)entry;
                    if (entryControl.isStart())
                        sb.append("[CONTROL "+entryControl.getKind()+"]");
                    else
                        sb.append("[/CONTROL "+entryControl.getKind()+"]");                    
                }
            }
            sb.append("[/PARAGRAPH]\n");
        }
        return sb.toString();
    }
}
