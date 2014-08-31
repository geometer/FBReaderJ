package org.geometerplus.android.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaSelectionHidePanelAction extends FBAndroidContextAction
{
    public YotaSelectionHidePanelAction(Context ctx, FBReaderApp fbreader) {
        super(ctx, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final FBReaderApp.PopupPanel popup = Reader.getActivePopup();
        if (popup != null && popup.getId().equals(YotaSelectionPopup.ID)) {
            Reader.hideActivePopup();
        }
    }
}
