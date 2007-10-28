package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextControlEntryPool;

import java.util.Map;
import java.util.HashMap;

class ZLTextControlEntryPoolImpl implements ZLTextControlEntryPool {
    public static ZLTextControlEntryPoolImpl myPool;
    private Map<Byte, ZLTextParagraphEntry> myStartEntries;
    private Map<Byte, ZLTextParagraphEntry> myEndEntries;

    ZLTextControlEntryPoolImpl() {
        myStartEntries = new HashMap<Byte, ZLTextParagraphEntry>();
        myEndEntries = new HashMap<Byte, ZLTextParagraphEntry>();
    }

    public ZLTextParagraphEntry getControlEntry(byte kind, boolean isStart) {
        Map<Byte, ZLTextParagraphEntry> entries = isStart ? myStartEntries : myEndEntries;
        ZLTextParagraphEntry entry = entries.get(new Byte(kind));
        if (entry != null) {
            return entry;
        }
        entry = new ZLTextControlEntryImpl(kind, isStart);
        entries.put(kind, entry);
        return entry;
    }
}
