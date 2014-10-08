package org.geometerplus.android.fbreader;

import android.view.View;

import com.yotadevices.yotaphone2.fbreader.YotaSettingsPopup;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ShowYotaSettingsAction extends FBAndroidAction {

	ShowYotaSettingsAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		Integer viewId = (Integer)params[0];
		View button = BaseActivity.findViewById(viewId);
		YotaSettingsPopup settingsPopup = (YotaSettingsPopup)Reader.getPopupById(YotaSettingsPopup.ID);
		settingsPopup.setRootView(button, BaseActivity);
		if (settingsPopup.isShowing()) {
			Reader.hideActivePopup();
		}
		else {
			Reader.showPopup(YotaSettingsPopup.ID);
		}
	}
}
