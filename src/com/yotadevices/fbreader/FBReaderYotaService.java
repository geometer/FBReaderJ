/***********************************************************************************
 *
 *  Copyright 2012 Yota Devices LLC, Russia
 *
 ************************************************************************************/

package com.yotadevices.fbreader;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.sdk.BSDrawer;
import com.yotadevices.sdk.BSDrawer.Waveform;
import com.yotadevices.sdk.Constants.Gestures;
import com.yotadevices.sdk.Constants.VolumeButtonsEvent;
import com.yotadevices.sdk.BSMotionEvent;

import org.geometerplus.android.fbreader.FBReaderApplication;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.FrameLayout;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity {

	private static final String TAG = FBReaderYotaService.class.getSimpleName();

	private ZLAndroidWidget myWidget;

	private Canvas mCanvas;

	private Bitmap mBitmap;

	private Bitmap mWhiteBitmap;

	private int mInitialFontSize;

	public FBReaderYotaService() {
		super();
	}

	@Override
	public void onBSResume() {
		super.onBSResume();
		initBookView();
		getBSDrawer().drawBitmap(0, 0, mBitmap, Waveform.WAVEFORM_GC_FULL);
	}

	@Override
	public void onBSCreate() {
		super.onBSCreate();
		initBookView();
	}

	private void initBookView() {
		mWhiteBitmap = Bitmap.createBitmap(BSDrawer.SCREEN_WIDTH,
				BSDrawer.SCREEN_HEIGHT, Config.ARGB_8888);
		Canvas c = new Canvas(mWhiteBitmap);
		Paint paint = new Paint();
		paint.setColor(0xFFFFFF);
		c.drawRect(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT, paint);
		mBitmap = Bitmap.createBitmap(BSDrawer.SCREEN_WIDTH,
				BSDrawer.SCREEN_HEIGHT, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		myWidget = new ZLAndroidWidget(getApplicationContext(), true);
		if (FBReaderApp.Instance() == null) {
			FBReaderApp.createInstance(myWidget);
		}

		((FBReaderApp) FBReaderApp.Instance()).clearTextCaches();
		((FBView) ZLApplication.Instance().getCurrentView()).resetFooter();
		myWidget.setLayoutParams(new FrameLayout.LayoutParams(
				BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT));
		myWidget.measure(BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		myWidget.layout(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		myWidget.draw(mCanvas);
	}

	@Override
	protected void onVolumeButtonsEvent(VolumeButtonsEvent event) {
		super.onVolumeButtonsEvent(event);
		switch (event) {
		case VOLUME_MINUS_UP:
			handleGesture(Gestures.GESTURES_BS_RL);
			break;
		case VOLUME_PLUS_UP:
			handleGesture(Gestures.GESTURES_BS_LR);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		initBookView();
		getBSDrawer().drawBitmap(0, 0, mBitmap, Waveform.WAVEFORM_GC_FULL);
	}

	@Override
	public void onBSTouchEvent(BSMotionEvent event) {
		handleGesture(event.getBSAction());
	}

	private void handleGesture(Gestures action) {
		if (action == Gestures.GESTURES_BS_RL) {
			myWidget.turnPageStatic(true);
			myWidget.draw(mCanvas);
			getBSDrawer().drawBitmap(0, 0, mBitmap, Waveform.WAVEFORM_GC_FULL);
		} else if (action == Gestures.GESTURES_BS_LR) {
			myWidget.turnPageStatic(false);
			myWidget.draw(mCanvas);
			getBSDrawer().drawBitmap(0, 0, mBitmap, Waveform.WAVEFORM_GC_FULL);
		}
	}
}
