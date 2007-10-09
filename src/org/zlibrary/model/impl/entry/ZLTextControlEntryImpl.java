package org.zlibrary.model.impl.entry;

import org.zlibrary.model.entry.ZLTextControlEntry;

/**
 * Created by IntelliJ IDEA.
 * User: 465
 * Date: 06.10.2007
 * Time: 11:35:04
 * To change this template use File | Settings | File Templates.
 */
public class ZLTextControlEntryImpl implements ZLTextControlEntry {
    private byte myKind;
    private boolean myStart;

    public ZLTextControlEntryImpl(byte kind, boolean isStart) {
        myKind = kind;
        myStart = isStart;
    }

    public byte getKind() {
        return myKind;
    }

    public boolean isStart() {
        return myStart;
    }
}
