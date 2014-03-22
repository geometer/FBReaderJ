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
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.yotadevices.sdk.*;
import com.yotadevices.sdk.utils.EinkUtils;

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

import org.geometerplus.android.fbreader.FBReaderIntents;

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

	private class YotaBackScreenWidget extends ZLAndroidWidget {
		private Bitmap myDefaultCoverBitmap;
		private Boolean myLastPaintWasActive;
		private Book myLastBook;

		YotaBackScreenWidget(Context context) {
			super(context);
		}
		
		private volatile byte[] myStoredMD5 = null;

		@Override
		public synchronized void repaint() {
			draw(myCanvas);
			final byte[] currentMD5 = MD5(myBitmap);
			if (myStoredMD5 == null || !myStoredMD5.equals(currentMD5)) {
				getBSDrawer().drawBitmap(0, 0, myBitmap, BSDrawer.Waveform.WAVEFORM_GC_PARTIAL);
				myStoredMD5 = currentMD5;
			}
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			if (myBackScreenIsActive) {
				super.onDraw(canvas);
			} else {
				if (myLastPaintWasActive == null ||
					myLastPaintWasActive ||
					!MiscUtil.equals(myCurrentBook, myLastBook)) {
					drawCover(canvas, myCurrentBook);
				}
			}
			myLastPaintWasActive = myBackScreenIsActive;
			myLastBook = myCurrentBook;
		}

		private void drawCover(Canvas canvas, Book currentBook) {
			final Paint paint = new Paint();
			paint.setColor(0xFFFFFFFF);
			canvas.drawRect(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT, paint);

			Bitmap coverBitmap = null;
			if (currentBook != null) {
				final ZLImage image = BookUtil.getCover(currentBook);

				if (image != null) {
					if (image instanceof ZLLoadableImage) {
						final ZLLoadableImage loadableImage = (ZLLoadableImage)image;
						if (!loadableImage.isSynchronized()) {
							loadableImage.synchronize();
						}
					}
					final ZLAndroidImageData data =
						((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
					if (data != null) {
						coverBitmap = data.getBitmap(
							BSDrawer.SCREEN_WIDTH - 20, BSDrawer.SCREEN_HEIGHT - 20
						);
					}
				}
			}
			if (coverBitmap == null) {
				coverBitmap = getDefaultCoverBitmap();
			}

			canvas.drawBitmap(
				coverBitmap,
				(BSDrawer.SCREEN_WIDTH - coverBitmap.getWidth()) / 2,
				(BSDrawer.SCREEN_HEIGHT - coverBitmap.getHeight()) / 2,
				paint
			);
		}

		private Bitmap getDefaultCoverBitmap() {
			if (myDefaultCoverBitmap == null) {
				myDefaultCoverBitmap = BitmapFactory.decodeResource(
					getApplicationContext().getResources(), R.drawable.fbreader_256x256
				);
			}
			return myDefaultCoverBitmap;
		}
	}

	private void initBookView(final boolean refresh) {
		if (myBitmap == null) {
			myBitmap = Bitmap.createBitmap(
				BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT, Bitmap.Config.RGB_565
			);
			myCanvas = new Canvas(myBitmap);
		}
		if (Widget == null) {
			Widget = new YotaBackScreenWidget(getApplicationContext());
		}
		Widget.setLayoutParams(
			new FrameLayout.LayoutParams(BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT)
		);
		Widget.measure(BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		Widget.layout(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		Widget.draw(myCanvas);

		if (refresh) {
			getBSDrawer().drawBitmap(0, 0, myBitmap, BSDrawer.Waveform.WAVEFORM_GC_PARTIAL);
		}
	}

	@Override
	protected void onVolumeButtonsEvent(Constants.VolumeButtonsEvent event) {
		super.onVolumeButtonsEvent(event);

		String action = null;
		switch (event) {
			case VOLUME_MINUS_UP:
				action = myBindings.getBinding(KeyEvent.KEYCODE_VOLUME_DOWN, false);
				break;
			case VOLUME_PLUS_UP:
				action = myBindings.getBinding(KeyEvent.KEYCODE_VOLUME_UP, false);
				break;
			default:
				break;
		}

		if (ActionCode.VOLUME_KEY_SCROLL_FORWARD.equals(action)) {
			Widget.turnPageStatic(true);
		} else if (ActionCode.VOLUME_KEY_SCROLL_BACK.equals(action)) {
			Widget.turnPageStatic(false);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(KEY_BACK_SCREEN_IS_ACTIVE)) {
			myBackScreenIsActive = intent.getBooleanExtra(KEY_BACK_SCREEN_IS_ACTIVE, false);
		} else {
			myBackScreenIsActive = new ViewOptions().YotaDrawOnBackScreen.getValue();
		}
		myCurrentBook = FBReaderIntents.getBookExtra(intent);

		initBookView(true);
		setYotaGesturesEnabled(myBackScreenIsActive);
	}

	@Override
	public void onBSTouchEvent(BSMotionEvent event) {
		handleGesture(event.getBSAction());
	}

	public void setYotaGesturesEnabled(boolean enabled) {
		if (enabled) {
			enableGestures(
				EinkUtils.GESTURE_BACK_SINGLE_TAP |
				EinkUtils.GESTURE_BACK_SWIPE_LEFT |
				EinkUtils.GESTURE_BACK_SWIPE_RIGHT
			);
		} else {
			enableGestures(0);
		}
	}

	private void handleGesture(Constants.Gestures action) {
		if (action == Constants.Gestures.GESTURES_BS_RL) {
			Widget.turnPageStatic(true);
		} else if (action == Constants.Gestures.GESTURES_BS_LR) {
			Widget.turnPageStatic(false);
		}
	}
}
