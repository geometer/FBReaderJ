package org.geometerplus.android.fbreader;

import android.view.View;

import com.yotadevices.yotaphone2.fbreader.YotaBookContentPopup;
import com.yotadevices.yotaphone2.fbreader.YotaSettingsPopup;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ShowYotaBookContentsAction extends FBAndroidAction {

	ShowYotaBookContentsAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		Integer viewId = (Integer) params[0];
		View button = BaseActivity.findViewById(viewId);
		YotaBookContentPopup contentPopup = (YotaBookContentPopup) Reader.getPopupById(YotaBookContentPopup.ID);
		contentPopup.setRootView(button);
		Reader.showPopup(YotaBookContentPopup.ID);
	}
}
