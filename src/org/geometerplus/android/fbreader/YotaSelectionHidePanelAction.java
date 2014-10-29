package org.geometerplus.android.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaSelectionHidePanelAction extends FBAndroidContextAction
{
    private final String mID;
    public YotaSelectionHidePanelAction(Context ctx, FBReaderApp fbreader, String popupID) {
        super(ctx, fbreader);
        mID = popupID;
    }

    @Override
    protected void run(Object... params) {
        final FBReaderApp.PopupPanel popup = Reader.getActivePopup();
        if (popup != null && popup.getId().equals(mID)) {
            Reader.hideActivePopup();
        }
    }
}
