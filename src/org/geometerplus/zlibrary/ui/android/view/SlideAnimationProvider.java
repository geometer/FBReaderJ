/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.view;

import android.graphics.*;

class SlideAnimationProvider extends SimpleAnimationProvider {
	private final Paint myDarkPaint = new Paint();
	private final Paint myPaint = new Paint();

	SlideAnimationProvider(BitmapManager bitmapManager) {
		super(bitmapManager);
	}

	private void setDarkFilter(int visible, int full) {
		final int color = 145 + 100 * Math.abs(visible) / full;
		myDarkPaint.setColorFilter(new PorterDuffColorFilter(
			Color.rgb(color, color, color), PorterDuff.Mode.MULTIPLY
		));
	}

	@Override
	protected void drawInternal(Canvas canvas) {
		if (myDirection.IsHorizontal) {
			final int dX = myEndX - myStartX;
			setDarkFilter(dX, myWidth);
			canvas.drawBitmap(getBitmapTo(), 0, 0, myDarkPaint);
			canvas.drawBitmap(getBitmapFrom(), dX, 0, myPaint);
		} else {
			final int dY = myEndY - myStartY;
			setDarkFilter(dY, myHeight);
			canvas.drawBitmap(getBitmapTo(), 0, 0, myDarkPaint);
			canvas.drawBitmap(getBitmapFrom(), 0, dY, myPaint);
		}
	}

	private void drawBitmapInternal(Canvas canvas, Bitmap bm, int left, int right, int height, int voffset, Paint paint) {
		canvas.drawBitmap(
			bm,
			new Rect(left, 0, right, height),
			new Rect(left, voffset, right, voffset + height),
			paint
		);
	}

	@Override
	protected void drawFooterBitmap(Canvas canvas, Bitmap footerBitmap, int voffset) {
		if (myDirection.IsHorizontal) {
			final int dX = myEndX - myStartX;
			setDarkFilter(dX, myWidth);
			final int h = footerBitmap.getHeight();
			if (dX > 0) {
				drawBitmapInternal(canvas, footerBitmap, 0, dX, h, voffset, myDarkPaint);
				drawBitmapInternal(canvas, footerBitmap, dX, myWidth, h, voffset, myPaint);
			} else {
				drawBitmapInternal(canvas, footerBitmap, myWidth + dX, myWidth, h, voffset, myDarkPaint);
				drawBitmapInternal(canvas, footerBitmap, 0, myWidth + dX, h, voffset, myPaint);
			}
		} else {
			canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
		}
	}
}
