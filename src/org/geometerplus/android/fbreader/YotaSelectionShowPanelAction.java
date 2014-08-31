package org.geometerplus.android.fbreader;


import android.content.Context;

import org.geometerplus.android.fbreader.network.action.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;

public class YotaSelectionShowPanelAction extends FBAndroidContextAction {
    public YotaSelectionShowPanelAction(Context ctx, FBReaderApp fbreader) {
        super(ctx, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final ZLTextView view = Reader.getTextView();
        YotaSelectionPopup selectionPanel = (YotaSelectionPopup)Reader.getPopupById(YotaSelectionPopup.ID);
        selectionPanel.move(view.getSelectionStartY(), view.getSelectionEndY());
        Reader.showPopup(YotaSelectionPopup.ID);
    }
}
