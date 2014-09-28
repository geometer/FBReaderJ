package org.geometerplus.android.fbreader;

import android.view.View;

import com.yotadevices.yotaphone2.fbreader.YotaBookContentPopup;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ShowYotaBookContentsAction extends FBAndroidAction {
	private YotaBookContentPopup myContents = null;

	ShowYotaBookContentsAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		Integer viewId = (Integer)params[0];
		View button = BaseActivity.findViewById(viewId);

		if (myContents == null) {
			myContents = new YotaBookContentPopup(BaseActivity, button, false);
		}
		if (!myContents.isShowing()) {
			myContents.show(Reader);
		} else {
			myContents.hide();
		}
	}
}
