package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaBookContentBSPopup extends YotaBookContentPopup {
	public final static String ID = "YotaBookContentBSPopup";
	public YotaBookContentBSPopup(FBReaderApp app, Context context, boolean onBackScreen) {
		super(app, context, onBackScreen);
	}

	@Override
	public String getId() {
		return YotaBookContentBSPopup.ID;
	}
}
