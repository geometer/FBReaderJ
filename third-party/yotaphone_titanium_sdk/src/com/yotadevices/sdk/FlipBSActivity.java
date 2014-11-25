package com.yotadevices.sdk;

import com.yotadevices.sdk.utils.EinkUtils;

import android.os.Handler;
import android.widget.TextView;

public class FlipBSActivity extends NotificationBSActivity {
	private TextView mDescription;
	private Handler mHandler = new Handler();

	@Override
	protected void onBSCreate() {
		super.onBSCreate();
		setBSContentView(R.layout.flip_layout);
		GifView2 gifView = (GifView2) findViewById(R.id.flip_popup_image);
		EinkUtils.setViewWaveform(gifView, Drawer.Waveform.WAVEFORM_A2);
		EinkUtils.setViewDithering(gifView, Drawer.Dithering.DITHER_ATKINSON_BINARY);
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
