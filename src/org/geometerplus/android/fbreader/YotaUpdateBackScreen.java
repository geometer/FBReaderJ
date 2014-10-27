package org.geometerplus.android.fbreader;

import android.content.Context;

import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;

public class YotaUpdateBackScreen extends FBAndroidContextAction {
	private int mUpdateCount = 0;
	public YotaUpdateBackScreen(Context ctx, FBReaderApp fbreader) {
		super(ctx, fbreader);
	}

	@Override
	protected void run(Object... params) {
		int countLimit = Reader.ViewOptions.YotaBSColorProfileName.getValue().equals(ColorProfile.YOTA_BS_WHITE) ? 10 : 3;
		if (++mUpdateCount >= countLimit) {
			mUpdateCount = 0;
			((FBReaderYotaService)mContext).performSingleFullUpdate();
		}
	}
}
