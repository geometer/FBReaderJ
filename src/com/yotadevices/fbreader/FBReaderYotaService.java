package com.yotadevices.fbreader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FBReaderYotaService extends Service {
	public static final String KEY_BACK_SCREEN_IS_ACTIVE =
		"com.yotadevices.fbreader.backScreenIsActive";
	public static final String KEY_CURRENT_BOOK =
		"com.yotadevices.fbreader.currentBook";

	public IBinder onBind(Intent intent) {
		return null;
	}
}
