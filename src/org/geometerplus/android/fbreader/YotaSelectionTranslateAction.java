package org.geometerplus.android.fbreader;


import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class YotaSelectionTranslateAction extends FBAndroidContextAction {
	private final boolean mOnBackScreen;
    public YotaSelectionTranslateAction(Context ctx, FBReaderApp fbreader, boolean bs) {
        super(ctx, fbreader);
	    mOnBackScreen = bs;
    }

    @Override
    protected void run(Object... params) {
        final FBView fbview = Reader.getTextView();
        final String text = fbview.getSelectedText();
	    final String ID = mOnBackScreen ? YotaTranslateBSPopup.ID : YotaTranslatePopup.ID;
        YotaTranslatePopup translatePopup = (YotaTranslatePopup)Reader.getPopupById(ID);
        translatePopup.setTextToTranlate(text);
	    translatePopup.setNumWordsToDefine(fbview.getCountOfSelectedWords());
        translatePopup.setOnBackScreen(mOnBackScreen);
        fbview.clearSelection();

        Reader.showPopup(ID);
    }
}
