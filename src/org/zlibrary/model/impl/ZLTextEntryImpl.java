package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextParagraphEntry;

public class ZLTextEntryImpl implements ZLTextParagraphEntry {
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
