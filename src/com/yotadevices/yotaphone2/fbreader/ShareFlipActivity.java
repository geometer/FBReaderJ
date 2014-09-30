package com.yotadevices.yotaphone2.fbreader;

import android.content.Intent;

import com.yotadevices.sdk.FlipBSActivity;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class ShareFlipActivity extends FlipBSActivity {
	public static String subject;
	public static String text;
	@Override
	protected void onBSCreate() {
		super.onBSCreate();
		setDescription(R.string.flip_popup_default_description);

		final Intent intent = new Intent(FBReaderIntents.Action.SHARE);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);
	}
}