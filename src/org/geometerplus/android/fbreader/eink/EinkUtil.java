package org.geometerplus.android.fbreader.eink;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Activity;

public abstract class EinkUtil {
	//Now we have only this behavior
	public static void prepareSingleFullRefresh(ZLAndroidLibrary.Device device, Activity a) {
		if (device == ZLAndroidLibrary.Device.NOOK || device == ZLAndroidLibrary.Device.NOOK12) {
			Nook2Util.setGL16Mode(a);
		}
	}
};
