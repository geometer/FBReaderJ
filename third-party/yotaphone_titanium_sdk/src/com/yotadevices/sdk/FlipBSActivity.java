package com.yotadevices.sdk;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 * 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 * 
 * */

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
