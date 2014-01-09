package org.geometerplus.android.util.eink;

import android.app.Activity;

import org.geometerplus.android.util.DeviceType;

public abstract class EInkUtil {
	//Now we have only this behavior
	public static void prepareSingleFullRefresh(Activity a) {
		final DeviceType deviceType = DeviceType.Instance();
		if (deviceType == DeviceType.NOOK || deviceType == DeviceType.NOOK12) {
			Nook2Util.setGL16Mode(a);
		}
	}
};
