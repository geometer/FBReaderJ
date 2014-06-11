/***********************************************************************************
 *
 *  Copyright 2012 Yota Devices LLC, Russia
 *
 ************************************************************************************/

package com.yotadevices.fbreader;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yotadevices.sdk.*;
import com.yotadevices.sdk.utils.EinkUtils;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;

import org.geometerplus.android.fbreader.api.FBReaderIntents;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity {
	public static final String KEY_BACK_SCREEN_IS_ACTIVE =
		"com.yotadevices.fbreader.backScreenIsActive";

	public static ZLAndroidWidget Widget;
	private Canvas myCanvas;
	private Bitmap myBitmap;

	private final ZLKeyBindings myBindings = new ZLKeyBindings();
	private volatile boolean myBackScreenIsActive;
	private Book myCurrentBook;

	@Override
	public void onBSCreate() {
		super.onBSCreate();
		initBookView(false);
	}

	@Override
	public void onBSResume() {
		super.onBSResume();
		initBookView(true);
	}

	@Override
	public void onBSDestroy() {
		Widget = null;
		super.onBSDestroy();
	}
	
	private static byte[] MD5(Bitmap image) {
		// TODO: possible too large array(s)?
		final int bytesNum = image.getWidth() * image.getHeight() * 2;
		final ByteBuffer buffer = ByteBuffer.allocate(bytesNum);
		image.copyPixelsToBuffer(buffer);
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(buffer.array());
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}



	private void initBookView(final boolean refresh) {
		Log.d(TAG, "--- init book view:" + refresh);
		/*
		int width = BSDrawer.SCREEN_WIDTH ;
		int height = BSDrawer.SCREEN_HEIGHT;
		if (myBitmap == null) {
			myBitmap = Bitmap.createBitmap(
				width, height, Bitmap.Config.RGB_565
			);
			myCanvas = new Canvas(myBitmap);
		}
		if (Widget == null) {
			Widget = new YotaBackScreenWidget(getApplicationContext());
		}
		Widget.setLayoutParams(
			new FrameLayout.LayoutParams(width, height)
		);
		Widget.measure(width, height);
		Widget.layout(0, 0, width, height);
		Widget.draw(myCanvas);
		
		ViewGroup parent = (ViewGroup) Widget.getParent();
		if (parent != null) parent.removeView(Widget);
		setBSContentView(Widget);
		*/
		
		View v = getBSDrawer().getBSLayoutInflater().inflate(R.layout.bs_main, null);
		YotaBackScreenWidget widget = (YotaBackScreenWidget) v.findViewById(R.id.bs_main_widget);
		widget.setBook(myCurrentBook);
		widget.setIsBsActive(myBackScreenIsActive);
		setBSContentView(v);
//
//		if (refresh) {
//			getBSDrawer().drawBitmap(0, 0, myBitmap, BSDrawer.Waveform.WAVEFORM_GC_PARTIAL);
//		}
	}

//	@Override
//	protected void onVolumeButtonsEvent(Constants.VolumeButtonsEvent event) {
//		super.onVolumeButtonsEvent(event);
//
//		String action = null;
//		switch (event) {
//			case VOLUME_MINUS_UP:
//				action = myBindings.getBinding(KeyEvent.KEYCODE_VOLUME_DOWN, false);
//				break;
//			case VOLUME_PLUS_UP:
//				action = myBindings.getBinding(KeyEvent.KEYCODE_VOLUME_UP, false);
//				break;
//			default:
//				break;
//		}
//
//		if (ActionCode.VOLUME_KEY_SCROLL_FORWARD.equals(action)) {
//			Widget.turnPageStatic(true);
//		} else if (ActionCode.VOLUME_KEY_SCROLL_BACK.equals(action)) {
//			Widget.turnPageStatic(false);
//		}
//	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(KEY_BACK_SCREEN_IS_ACTIVE)) {
			myBackScreenIsActive = intent.getBooleanExtra(KEY_BACK_SCREEN_IS_ACTIVE, false);
		} else {
			myBackScreenIsActive = new ViewOptions().YotaDrawOnBackScreen.getValue();
		}
		
		if (ZLApplication.Instance()==null && ZLApplication.Instance().getCurrentView() == null)
			return;
		
		myCurrentBook = FBReaderIntents.getBookExtra(intent);

		initBookView(true);
		//setYotaGesturesEnabled(myBackScreenIsActive);
	}

//	@Override
//	public void onBSTouchEvent(BSMotionEvent event) {
//		handleGesture(event.getBSAction());
//	}
//
//	public void setYotaGesturesEnabled(boolean enabled) {
//		if (enabled) {
//			enableGestures(
//				EinkUtils.GESTURE_BACK_SINGLE_TAP |
//				EinkUtils.GESTURE_BACK_SWIPE_LEFT |
//				EinkUtils.GESTURE_BACK_SWIPE_RIGHT
//			);
//		} else {
//			enableGestures(0);
//		}
//	}

//	private void handleGesture(Constants.Gestures action) {
//		if (action == Constants.Gestures.GESTURES_BS_RL) {
//			Widget.turnPageStatic(true);
//		} else if (action == Constants.Gestures.GESTURES_BS_LR) {
//			Widget.turnPageStatic(false);
//		}
//	}
}
