package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.Intent;

import com.yotadevices.sdk.utils.RotationAlgorithm;
import com.yotadevices.yotaphone2.fbreader.ShareFlipActivity;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaSelectionShareAction extends FBAndroidContextAction {
	public YotaSelectionShareAction(Context ctx, FBReaderApp fbreader) {
		super(ctx, fbreader);
	}

	@Override
	protected void run(Object... params) {
		final String text = Reader.getTextView().getSelectedText();
		final String title = Reader.getCurrentBook().getTitle();
		Reader.getTextView().clearSelection();

		RotationAlgorithm.getInstance(mContext.getApplicationContext()).turnScreenOffIfRotated(
				RotationAlgorithm.OPTION_START_WITH_BS | RotationAlgorithm.OPTION_NO_UNLOCK
						| RotationAlgorithm.OPTION_POWER_ON | RotationAlgorithm.OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC, new RotationAlgorithm.OnPhoneRotatedListener() {
					@Override
					public void onRotataionCancelled() {

					}

					@Override
					public void onPhoneRotatedToFS() {

					}

					@Override
					public void onPhoneRotatedToBS() {

					}
				});
		ShareFlipActivity.subject = ZLResource.resource("selection").getResource("quoteFrom").getValue().replace("%s", title);
		ShareFlipActivity.text = text;

		Intent shareFlipPopup = new Intent(mContext, ShareFlipActivity.class);
		mContext.startService(shareFlipPopup);
	}
}
