package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.impl.entry.ZLTextEntryImpl;
import org.zlibrary.model.impl.entry.ZLTextControlEntryPoolImpl;
import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextEntry;
import org.zlibrary.model.entry.ZLTextControlEntry;
import org.zlibrary.model.entry.ZLTextControlEntryPool;

import java.util.List;
import java.util.LinkedList;

public class ZLTextModelImpl implements ZLTextModel {
    private List<ZLTextParagraph> myParagraphs = new LinkedList<ZLTextParagraph>();
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
        myParagraphs.get(myParagraphs.size()-1).addEntry(myEntryPool.getControlEntry(textKind, isStart));
    }

    public void addText(String text) {
        myParagraphs.get(myParagraphs.size()-1).addEntry(new ZLTextEntryImpl(text));
    }

    public void addText(List<String> text) {
        ZLTextParagraph paragraph = myParagraphs.get(myParagraphs.size()-1);
        StringBuilder sb = new StringBuilder();
        for (String str: text)
            sb.append(str);
        paragraph.addEntry(new ZLTextEntryImpl(sb.toString()));
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
