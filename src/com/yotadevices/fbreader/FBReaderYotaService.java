/***********************************************************************************
 *
 *  Copyright 2012 Yota Devices LLC, Russia
 *
 ************************************************************************************/

package com.yotadevices.fbreader;

import android.content.Intent;
import android.graphics.*;
import android.widget.FrameLayout;

import com.yotadevices.sdk.*;
import com.yotadevices.sdk.utils.EinkUtils;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.*;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity {
	public static final String KEY_BACK_SCREEN_IS_ACTIVE =
		"com.yotadevices.fbreader.backScreenIsActive";
	public static final String KEY_CURRENT_BOOK =
		"com.yotadevices.fbreader.currentBook";

	public static ZLAndroidWidget Widget;
	private Canvas mCanvas;
	private Bitmap mBitmap;

	private volatile boolean myBackScreenIsActive;
	private Book myCurrentBook;

	@Override
	public void onBSResume() {
		super.onBSResume();
		initBookView();
		getBSDrawer().drawBitmap(0, 0, mBitmap, BSDrawer.Waveform.WAVEFORM_GC_FULL);
	}

	@Override
	public void onBSCreate() {
		super.onBSCreate();
		initBookView();
	}

	private void initBookView() {
		mBitmap = Bitmap.createBitmap(BSDrawer.SCREEN_WIDTH,
				BSDrawer.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		Widget = new ZLAndroidWidget(getApplicationContext()) {
			@Override
			public void repaint() {
				Widget.draw(mCanvas);
				getBSDrawer().drawBitmap(0, 0, mBitmap, BSDrawer.Waveform.WAVEFORM_GC_FULL);
			}

			@Override
			protected void onDraw(final Canvas canvas) {
				if (myBackScreenIsActive) {
					super.onDraw(canvas);
				} else {
					final Paint paint = new Paint();
					paint.setColor(0xFFFFFFFF);
					canvas.drawRect(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT, paint);

					Bitmap coverBitmap = null;
					if (myCurrentBook != null) {
						final ZLImage image = BookUtil.getCover(myCurrentBook);

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
						coverBitmap = BitmapFactory.decodeResource(
							getApplicationContext().getResources(), R.drawable.fbreader_256x256
						);
					}

					canvas.drawBitmap(
						coverBitmap,
						(BSDrawer.SCREEN_WIDTH - coverBitmap.getWidth()) / 2,
						(BSDrawer.SCREEN_HEIGHT - coverBitmap.getHeight()) / 2,
						paint
					);
				}
			}
		};
		Widget.setLayoutParams(
			new FrameLayout.LayoutParams(BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT)
		);
		Widget.measure(BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		Widget.layout(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT);
		Widget.draw(mCanvas);
	}

	@Override
	protected void onVolumeButtonsEvent(Constants.VolumeButtonsEvent event) {
		super.onVolumeButtonsEvent(event);
		switch (event) {
			case VOLUME_MINUS_UP:
				handleGesture(Constants.Gestures.GESTURES_BS_RL);
				break;
			case VOLUME_PLUS_UP:
				handleGesture(Constants.Gestures.GESTURES_BS_LR);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		myBackScreenIsActive = intent.getBooleanExtra(KEY_BACK_SCREEN_IS_ACTIVE, false);
		myCurrentBook = SerializerUtil.deserializeBook(intent.getStringExtra(KEY_CURRENT_BOOK));

		initBookView();
		getBSDrawer().drawBitmap(0, 0, mBitmap, BSDrawer.Waveform.WAVEFORM_GC_FULL);
		setYotaGesturesEnabled(myBackScreenIsActive);
	}

	@Override
	public void onBSTouchEvent(BSMotionEvent event) {
		handleGesture(event.getBSAction());
	}

	@Override
	public void onBSDestroy() {
		Widget = null;
		super.onBSDestroy();
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
