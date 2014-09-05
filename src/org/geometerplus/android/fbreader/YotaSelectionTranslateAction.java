package org.geometerplus.android.fbreader;


import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class YotaSelectionTranslateAction extends FBAndroidContextAction {

    public YotaSelectionTranslateAction(Context ctx, FBReaderApp fbreader) {
        super(ctx, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final FBView fbview = Reader.getTextView();
        final String text = fbview.getSelectedText();
        boolean oneWordSelected = fbview.getCountOfSelectedWords() == 1;
        YotaTranslatePopup translatePopup = (YotaTranslatePopup)Reader.getPopupById(YotaTranslatePopup.ID);
        translatePopup.setTextToTranlate(text);
        fbview.clearSelection();

        Reader.showPopup(YotaTranslatePopup.ID);
    }
}
