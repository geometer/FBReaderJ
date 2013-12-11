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

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity {
	public static ZLAndroidWidget Widget;
	private Canvas mCanvas;
	private Bitmap mBitmap;

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
				final FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
				if (reader.YotaDrawOnBackScreenOption.getValue()) {
					super.onDraw(canvas);
				} else {
					final Paint paint = new Paint();
					paint.setColor(0xFFFFFFFF);
					canvas.drawRect(0, 0, BSDrawer.SCREEN_WIDTH, BSDrawer.SCREEN_HEIGHT, paint);

					boolean coverUsed = false;
					if (reader.Model != null && reader.Model.Book != null) {
						final ZLImage image = BookUtil.getCover(reader.Model.Book);

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
								final Bitmap coverBitmap = data.getBitmap(BSDrawer.SCREEN_WIDTH - 20, BSDrawer.SCREEN_HEIGHT - 20);
								if (coverBitmap != null) {
									canvas.drawBitmap(
										coverBitmap,
										(BSDrawer.SCREEN_WIDTH - coverBitmap.getWidth()) / 2,
										(BSDrawer.SCREEN_HEIGHT - coverBitmap.getHeight()) / 2,
										paint
									);
									coverUsed = true;
								}
							}
						}
					}

					if (!coverUsed) {
						final Bitmap fbIcon = BitmapFactory.decodeResource(
							getApplicationContext().getResources(), R.drawable.fbreader_bw
						);
						canvas.drawBitmap(fbIcon, BSDrawer.SCREEN_WIDTH - fbIcon.getWidth() - 20, 20, paint);
					}
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
		initBookView();
		getBSDrawer().drawBitmap(0, 0, mBitmap, BSDrawer.Waveform.WAVEFORM_GC_FULL);
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

	private void handleGesture(Constants.Gestures action) {
		final FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
		if (!reader.YotaDrawOnBackScreenOption.getValue()) {
			Widget.repaint();
			return;
		}

		if (action == Constants.Gestures.GESTURES_BS_RL) {
			Widget.turnPageStatic(true);
		} else if (action == Constants.Gestures.GESTURES_BS_LR) {
			Widget.turnPageStatic(false);
		}
	}
}
