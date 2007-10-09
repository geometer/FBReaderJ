package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextEntry;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: 465
 * Date: 06.10.2007
 * Time: 12:05:51
 * To change this template use File | Settings | File Templates.
 */
public class ZLTextParagraphImpl implements ZLTextParagraph {
    private List<ZLTextParagraphEntry> myEntries;

    public ZLTextParagraphImpl() {
        myEntries = new LinkedList<ZLTextParagraphEntry>();
    }

    public List<ZLTextParagraphEntry> getEntries() {
        return Collections.unmodifiableList(myEntries);
    }

    public Kind getKind() {
        return Kind.TEXT_PARAGRAPH;
    }

    public int getEntryNumber() {
        return myEntries.size();
    }

    public int getTextLength() {
        int size = 0;
        for (ZLTextParagraphEntry entry: myEntries) {
            if (entry instanceof ZLTextEntry) {
                size += ((ZLTextEntry)entry).getDataLength();
            }
        }
        return size;
    }

    public void addEntry(ZLTextParagraphEntry entry) {
        myEntries.add(entry);
    }
}
