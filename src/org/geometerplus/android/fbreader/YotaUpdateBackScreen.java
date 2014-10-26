package org.geometerplus.android.fbreader;

import android.content.Context;

import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaUpdateBackScreen extends FBAndroidContextAction {
	private int mUpdateCount = 0;
	public YotaUpdateBackScreen(Context ctx, FBReaderApp fbreader) {
		super(ctx, fbreader);
	}

	@Override
	protected void run(Object... params) {
		if (++mUpdateCount >= 5) {
			mUpdateCount = 0;
			((FBReaderYotaService)mContext).performSingleFullUpdate();
		}
	}
}
