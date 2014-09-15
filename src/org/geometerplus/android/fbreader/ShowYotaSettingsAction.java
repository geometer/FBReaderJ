package org.geometerplus.android.fbreader;

import android.view.View;

import com.yotadevices.yotaphone2.fbreader.YotaSettingsPopup;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ShowYotaSettingsAction extends FBAndroidAction {
	private YotaSettingsPopup myYotaSettings = null;

	ShowYotaSettingsAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		Integer viewId = (Integer)params[0];
		View button = BaseActivity.findViewById(viewId);

		if (myYotaSettings == null) {
			myYotaSettings = new YotaSettingsPopup(BaseActivity, button);
		}
		if (!myYotaSettings.isShowing()) {
			myYotaSettings.show(Reader);
		} else {
			myYotaSettings.hide();
		}
	}
}
