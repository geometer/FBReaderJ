package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextControlEntryPool;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;

import java.util.LinkedList;

class ZLTextModelImpl implements ZLTextModel {
    private LinkedList<ZLTextParagraph> myParagraphs = new LinkedList<ZLTextParagraph>();
    private ZLTextControlEntryPool myEntryPool = new ZLTextControlEntryPoolImpl();

    public int getParagraphsNumber() {
        return myParagraphs.size();
    }

    public ZLTextParagraph getParagraph(int index) {
        return myParagraphs.get(index);
    }

    public void addParagraphInternal(ZLTextParagraph paragraph) {
        this.myParagraphs.add(paragraph);
    }
    
    public void removeParagraphInternal(int index) {
    	this.myParagraphs.remove(index);
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
    
    public void addControl(ZLTextForcedControlEntry entry) {
    	myParagraphs.getLast().addEntry(entry);
    }
    
	public void addHyperlinkControl(byte textKind, String label) {
		myParagraphs.getLast().addEntry(new ZLTextHyperlinkControlEntryImpl(textKind, label));
	}
	//void addImage(String id, ZLImageMap imageMap, short vOffset);
	public void addFixedHSpace(byte length) {
		myParagraphs.getLast().addEntry(new ZLTextFixedHSpaceEntryImpl(length));
	}	

    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (ZLTextParagraph paragraph: myParagraphs) {
            sb.append("[PARAGRAPH]\n");
            for (ZLTextParagraphEntry entry: paragraph.getEntries()) {
                if (entry instanceof ZLTextEntryImpl) {
                    sb.append("[TEXT]");
                    sb.append(((ZLTextEntryImpl)entry).getData());
                    sb.append("[/TEXT]");
                } else if (entry instanceof ZLTextControlEntryImpl) {
                    ZLTextControlEntryImpl entryControl = (ZLTextControlEntryImpl)entry;
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
