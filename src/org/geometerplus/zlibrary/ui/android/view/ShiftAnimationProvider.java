/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

class ShiftAnimationProvider extends AnimationProvider {
	ShiftAnimationProvider(Paint paint) {
		super(paint);
	}

	@Override
	public void draw(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap, int startX, int startY, int endX, int endY, boolean horizontal) {
		final int w = fgBitmap.getWidth();
		final int h = fgBitmap.getHeight();
		myPaint.setColor(Color.rgb(127, 127, 127));
		if (horizontal) {
			final int dX = endX - startX;
			canvas.drawBitmap(bgBitmap, dX > 0 ? dX - w : dX + w, 0, myPaint);
			canvas.drawBitmap(fgBitmap, dX, 0, myPaint);
			if (dX > 0 && dX < w) {
				canvas.drawLine(dX, 0, dX, h + 1, myPaint);
			} else if (dX < 0 && dX > -w) {
				canvas.drawLine(dX + w, 0, dX + w, h + 1, myPaint);
			}
		} else {
			final int dY = endY - startY;
			canvas.drawBitmap(bgBitmap, 0, dY > 0 ? dY - h : dY + h, myPaint);
			canvas.drawBitmap(fgBitmap, 0, dY, myPaint);
			if (dY > 0 && dY < h) {
				canvas.drawLine(0, dY, w + 1, dY, myPaint);
			} else if (dY < 0 && dY > -h) {
				canvas.drawLine(0, dY + h, w + 1, dY + h, myPaint);
			}
		}
	}
}
