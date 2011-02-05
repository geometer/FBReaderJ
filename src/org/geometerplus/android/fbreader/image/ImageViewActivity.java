/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.image;

import android.app.Activity;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

public class ImageViewActivity extends Activity {
	Bitmap myBitmap;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		final boolean showStatusBar = application.ShowStatusBarOption.getValue();
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			showStatusBar ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
		);

		Thread.setDefaultUncaughtExceptionHandler(
			new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this)
		);

		setContentView(new ImageView());

		final Uri uri = getIntent().getData();
		if ("imagefile".equals(uri.getScheme())) {
			try {
				final String[] data = uri.getPath().split("\000");
				final ZLFileImage image = new ZLFileImage(
					"image/auto",
					ZLFile.createFileByPath(data[0]),
					Integer.parseInt(data[1]),
					Integer.parseInt(data[2])
				);
				final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
				myBitmap = ((ZLAndroidImageData)imageData).getFullSizeBitmap();
			} catch (Exception e) {
				// TODO: error message (?)
				finish();
			}
		} else {
			// TODO: error message (?)
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myBitmap != null) {
			myBitmap.recycle();
		}
		myBitmap = null;
	}

	private class ImageView extends View {
		private final Paint myPaint = new Paint();

		private int myDx;
		private int myDy;

		ImageView() {
			super(ImageViewActivity.this);
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			myPaint.setColor(Color.rgb(128, 128, 128));
			final int w = getWidth();
			final int h = getHeight();
			canvas.drawRect(0, 0, w, h, myPaint);
			if (myBitmap == null || myBitmap.isRecycled()) {
				return;
			}

			final int bw = myBitmap.getWidth();
			final int bh = myBitmap.getHeight();
			
			final int left, top;
			if (bw <= w) {
				left = (w - bw) / 2;
			} else {
				left = Math.max(w - bw, Math.min(0, (w - bw) / 2 + myDx));
			}
			if (bh <= h) {
				top = (h - bh) / 2;
			} else {
				top = Math.max(h - bh, Math.min(0, (h - bh) / 2 + myDy));
			}
			canvas.drawBitmap(myBitmap, left, top, myPaint);
		}

		private void shift(int dx, int dy) {
			if (myBitmap == null || myBitmap.isRecycled()) {
				return;
			}

			final int w = getWidth();
			final int h = getHeight();
			final int bw = myBitmap.getWidth();
			final int bh = myBitmap.getHeight();

			final int newDx, newDy;

			if (w < bw) {
				final int delta = bw - w;
				newDx = Math.max(-delta, Math.min(delta, myDx + dx));
			} else {
				newDx = myDx;
			}
			if (h < bh) {
				final int delta = bh - h;
				newDy = Math.max(-delta, Math.min(delta, myDy + dy));
			} else {
				newDy = myDy;
			}

			if (newDx != myDx || newDy != myDy) {
				myDx = newDx;
				myDy = newDy;
				postInvalidate();
			}
		}

		private boolean myMotionControl;
		private int mySavedX;
		private int mySavedY;
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int x = (int)event.getX();
			int y = (int)event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					myMotionControl = false;
					break;
				case MotionEvent.ACTION_DOWN:
					myMotionControl = true;
					mySavedX = x;
					mySavedY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					if (myMotionControl) {
						shift(x - mySavedX, y - mySavedY);
					}
					myMotionControl = true;
					mySavedX = x;
					mySavedY = y;
					break;
			}
			return true;
		}
	}
}
