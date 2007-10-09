package org.zlibrary.model.impl.entry;

import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextControlEntryPool;

import java.util.Map;
import java.util.HashMap;

public class ZLTextControlEntryPoolImpl implements ZLTextControlEntryPool {
    public static ZLTextControlEntryPoolImpl myPool;
    private Map<Byte, ZLTextParagraphEntry> myStartEntries;
    private Map<Byte, ZLTextParagraphEntry> myEndEntries;

    public ZLTextControlEntryPoolImpl() {
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
