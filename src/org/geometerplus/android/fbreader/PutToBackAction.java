// YOTA changes

package org.geometerplus.android.fbreader;

import com.yotadevices.fbreader.FBReaderYotaService;
import com.yotadevices.sdk.utils.RotationAlgorithm;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class PutToBackAction extends FBAndroidAction {
	PutToBackAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		RotationAlgorithm.getInstance(FBReaderApplication.getAppContext()).turnScreenOffIfRotated();
		Intent serviceIntent = new Intent(FBReaderApplication.getAppContext(), FBReaderYotaService.class);
		//serviceIntent.setAction(BroadcastEvents.BROADCAST_ACTION_BACKSCREEN_APPLICATION_ACTIVE);
		FBReaderApplication.getAppContext().startService(serviceIntent);
	}
}
