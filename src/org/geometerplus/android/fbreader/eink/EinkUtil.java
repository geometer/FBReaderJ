package org.geometerplus.android.fbreader.eink;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Activity;

public abstract class EinkUtil {

	//Now we have only this behavior
	public static void prepareSingleFullRefresh(ZLAndroidLibrary.Devices device, Activity a) {
		if (device == ZLAndroidLibrary.Devices.NOOK || device == ZLAndroidLibrary.Devices.NOOK12) {
			Nook2Util.setGL16Mode(a);
		}
	}
};
