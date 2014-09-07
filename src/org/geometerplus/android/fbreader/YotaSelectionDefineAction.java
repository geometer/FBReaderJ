package org.geometerplus.android.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class YotaSelectionDefineAction extends FBAndroidContextAction {
	private final boolean mOnBackScreen;
	public YotaSelectionDefineAction(Context ctx, FBReaderApp fbreader, boolean bs) {
		super(ctx, fbreader);
		mOnBackScreen = bs;
	}

	@Override
	protected void run(Object... params) {
		final FBView fbview = Reader.getTextView();
		final String text = fbview.getSelectedText();
		boolean oneWordSelected = fbview.getCountOfSelectedWords() == 1;
//		final String ID = mOnBackScreen ? YotaDefineBSPopup.ID : YotaDefinePopup.ID;
		final String ID = YotaDefinePopup.ID;
		YotaDefinePopup translatePopup = (YotaDefinePopup)Reader.getPopupById(ID);
		translatePopup.setTextToTranlate(text);
		fbview.clearSelection();

		Reader.showPopup(ID);
	}
}
