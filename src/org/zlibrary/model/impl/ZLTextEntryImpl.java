package org.zlibrary.model.impl;

import org.zlibrary.model.entry.ZLTextEntry;

class ZLTextEntryImpl implements ZLTextEntry {
    private String myData;

    ZLTextEntryImpl(String data) {
        myData = data;
    }

    public int getDataLength() {
        return myData.length();
    }

    public String getData() {
        return myData;
    }

}
