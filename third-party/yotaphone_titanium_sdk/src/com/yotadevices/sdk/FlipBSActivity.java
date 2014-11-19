package com.yotadevices.sdk;

import android.os.Handler;
import android.widget.TextView;

public class FlipBSActivity extends NotificationBSActivity {
	private TextView mDescription;
	private Handler mHandler = new Handler();

	@Override
	protected void onBSCreate() {
		super.onBSCreate();
		setBSContentView(R.layout.flip_layout);
		mDescription = (TextView) findViewById(R.id.flip_popup_description);
	}

	protected void setDescription(final String description) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mDescription.setText(description);
			}
		});
	}

	protected void setDescription(final int descriptionResId) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mDescription.setText(getString(descriptionResId));
			}
		});
	}
}
