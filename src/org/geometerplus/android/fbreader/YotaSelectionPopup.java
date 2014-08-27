package org.geometerplus.android.fbreader;

import android.content.Context;
import android.view.LayoutInflater;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

public class YotaSelectionPopup extends ZLApplication.PopupPanel {
    final static String ID = "YotaSelectionPopup";

    private FBReaderApp mReaderApp;
    private android.widget.PopupWindow mPopup;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    protected YotaSelectionPopup(FBReaderApp application, Context ctx) {
        super(application);
        mReaderApp = application;
        mContext = ctx;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void update() {

    }

    @Override
    protected void hide_() {

    }

    @Override
    protected void show_() {

    }
}
