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

import org.geometerplus.zlibrary.core.view.ZLView;

class CurlAnimationProvider extends AnimationProvider {
	private final Paint myEdgePaint = new Paint();

	CurlAnimationProvider(Paint paint) {
		super(paint);
	}

	@Override
	public void draw(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap) {
		canvas.drawBitmap(bgBitmap, 0, 0, myPaint);

		final int cornerX = myStartX > myWidth / 2 ? myWidth : 0;
		final int cornerY = myStartY > myHeight / 2 ? myHeight : 0;
		final int oppositeX = Math.abs(myWidth - cornerX);
		final int oppositeY = Math.abs(myHeight - cornerY);
		final int x, y;
		if (myDirection.IsHorizontal) {
			x = myEndX;
			//x = Math.max(1, Math.min(myWidth - 1, myEndX));
			if (cornerY == 0) {
				y = Math.max(1, Math.min(myHeight / 2, myEndY));
			} else {
				y = Math.max(myHeight / 2, Math.min(myHeight - 1, myEndY));
			}
		} else {
			y = myEndY;
			//y = Math.max(1, Math.min(myHeight - 1, myEndY));
			if (cornerX == 0) {
				x = Math.max(1, Math.min(myWidth / 2, myEndX));
			} else {
				x = Math.max(myWidth / 2, Math.min(myWidth - 1, myEndX));
			}
		}
		final int dX = Math.max(1, Math.abs(x - cornerX));
		final int dY = Math.max(1, Math.abs(y - cornerY));

		final int x1 = cornerX == 0
			? (dY * dY / dX + dX) / 2
			: cornerX - (dY * dY / dX + dX) / 2;
		final int y1 = cornerY == 0
			? (dX * dX / dY + dY) / 2
			: cornerY - (dX * dX / dY + dY) / 2;

		final Path fgPath = new Path();
		fgPath.moveTo(x1, cornerY);
		fgPath.lineTo(x, y);
		fgPath.lineTo(cornerX, y1);
		fgPath.lineTo(cornerX, oppositeY);
		fgPath.lineTo(oppositeX, oppositeY);
		fgPath.lineTo(oppositeX, cornerY);
		canvas.clipPath(fgPath);
		canvas.drawBitmap(fgBitmap, 0, 0, myPaint);
		canvas.restore();
        
		myEdgePaint.setColor(ZLAndroidPaintContext.getFillColor());
		myEdgePaint.setAntiAlias(true);
		myEdgePaint.setStyle(Paint.Style.FILL);
		myEdgePaint.setShadowLayer(25, 5, 5, 0x99000000);
        
		final Path path = new Path();
		path.moveTo(x1, cornerY);
		path.lineTo(x, y);
		path.lineTo(cornerX, y1);
		canvas.drawPath(path, myEdgePaint);
	}

	@Override
	ZLView.PageIndex getPageToScrollTo() {
		switch (myDirection) {
			case leftToRight:
				return myStartX < myWidth / 2 ? ZLView.PageIndex.next : ZLView.PageIndex.previous;
			case rightToLeft:
				return myStartX < myWidth / 2 ? ZLView.PageIndex.previous : ZLView.PageIndex.next;
			case up:
				return myStartY < myHeight / 2 ? ZLView.PageIndex.previous : ZLView.PageIndex.next;
			case down:
				return myStartY < myHeight / 2 ? ZLView.PageIndex.next : ZLView.PageIndex.previous;
		}
		return ZLView.PageIndex.current;
	}

	@Override
	void doStep() {
		if (!getMode().Auto) {
			return;
		}

		switch (myDirection) {
			case leftToRight:
				myEndX -= (int)mySpeed;
				if (myStartY < myHeight / 2) {
					myEndY += (int)Math.abs(mySpeed / 2);
				} else {
					myEndY -= (int)Math.abs(mySpeed / 2);
				}
				break;
			case rightToLeft:
				myEndX += (int)mySpeed;
				if (myStartY < myHeight / 2) {
					myEndY += (int)Math.abs(mySpeed / 2);
				} else {
					myEndY -= (int)Math.abs(mySpeed / 2);
				}
				break;
			case up:
				myEndY += (int)mySpeed;
				if (myStartX < myWidth / 2) {
					myEndX += (int)Math.abs(mySpeed / 2);
				} else {
					myEndX -= (int)Math.abs(mySpeed / 2);
				}
				break;
			case down:
				myEndY -= (int)mySpeed;
				if (myStartX < myWidth / 2) {
					myEndX += (int)Math.abs(mySpeed / 2);
				} else {
					myEndX -= (int)Math.abs(mySpeed / 2);
				}
				break;
		}
		final int bound;
		if (getMode() == Mode.AutoScrollingForward) {
			bound = myDirection.IsHorizontal ? myWidth : myHeight;
		} else {
			bound = 0;
		}
		if (mySpeed > 0) {
			if (getScrollingShift() >= 2 * bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX + 2 * bound;
				} else {
					myEndY = myStartY + 2 * bound;
				}
				terminate();
				return;
			}
		} else {
			if (getScrollingShift() <= -2 * bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX - 2 * bound;
				} else {
					myEndY = myStartY - 2 * bound;
				}
				terminate();
				return;
			}
		}
		mySpeed *= 1.5;
	}
}
