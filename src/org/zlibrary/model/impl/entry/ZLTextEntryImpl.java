package org.zlibrary.model.impl.entry;

import org.zlibrary.model.entry.ZLTextEntry;

/**
 * Created by IntelliJ IDEA.
 * User: 465
 * Date: 06.10.2007
 * Time: 11:29:13
 * To change this template use File | Settings | File Templates.
 */
public class ZLTextEntryImpl implements ZLTextEntry {
    private String myData;

    public ZLTextEntryImpl(String data) {
        myData = data;
    }

    public int getDataLength() {
        return myData.length();
    }

    public String getData() {
        return myData;
    }

}
