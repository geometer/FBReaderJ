// YOTA changes

package org.geometerplus.android.fbreader;

import com.yotadevices.sdk.utils.RotationAlgorithm;

import com.yotadevices.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class YotaSwitchScreenAction extends FBAndroidAction {
	private final boolean mySwitchToBack;

	YotaSwitchScreenAction(FBReader baseActivity, FBReaderApp fbreader, boolean switchToBack) {
		super(baseActivity, fbreader);
		mySwitchToBack = switchToBack;
	}

	@Override
	protected void run(Object ... params) {
		RotationAlgorithm.getInstance(FBReaderApplication.getAppContext()).turnScreenOffIfRotated();
		Intent serviceIntent = new Intent(FBReaderApplication.getAppContext(), FBReaderYotaService.class);
		//serviceIntent.setAction(BroadcastEvents.BROADCAST_ACTION_BACKSCREEN_APPLICATION_ACTIVE);
		FBReaderApplication.getAppContext().startService(serviceIntent);
	}
}
