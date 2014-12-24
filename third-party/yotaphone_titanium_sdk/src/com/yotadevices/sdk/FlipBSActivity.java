package com.yotadevices.sdk;

import com.yotadevices.sdk.utils.EinkUtils;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class FlipBSActivity extends NotificationBSActivity {
	private TextView mDescription;
	private Handler mHandler = new Handler();

	@Override
	protected void onBSCreate() {
		super.onBSCreate();
		setBSContentView(R.layout.flip_layout);
		mDescription = (TextView) findViewById(R.id.flip_popup_description);

		GifView2 gifView = (GifView2) findViewById(R.id.flip_popup_image);
		gifView.setPlayCount(2);
		long stopDuration = gifView.getMovieDuration() + 3300;
		gifView.setStopOnDuration(stopDuration);

		EinkUtils.setViewWaveform(gifView, Drawer.Waveform.WAVEFORM_A2);
		EinkUtils.setViewDithering(gifView, Drawer.Dithering.DITHER_NONE);
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
