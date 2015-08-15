/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.ui.android.view.animation;

import android.graphics.*;
import android.util.FloatMath;

import org.geometerplus.zlibrary.core.util.BitmapUtil;
import org.geometerplus.zlibrary.core.view.ZLViewEnums;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;
import org.geometerplus.zlibrary.ui.android.view.ViewUtil;

public final class CurlAnimationProvider extends AnimationProvider {
	private final Paint myPaint = new Paint();
	private final Paint myBackPaint = new Paint();
	private final Paint myEdgePaint = new Paint();

	final Path myFgPath = new Path();
	final Path myEdgePath = new Path();
	final Path myQuadPath = new Path();

	private float mySpeedFactor = 1;

	public CurlAnimationProvider(BitmapManager bitmapManager) {
		super(bitmapManager);

		myBackPaint.setAntiAlias(true);
		myBackPaint.setAlpha(0x40);

		myEdgePaint.setAntiAlias(true);
		myEdgePaint.setStyle(Paint.Style.FILL);
		myEdgePaint.setShadowLayer(15, 0, 0, 0xC0000000);
	}

	private Bitmap myBuffer;
	private volatile boolean myUseCanvasHack = false;

	@Override
	protected void drawInternal(Canvas canvas) {
		if (myUseCanvasHack) {
			// This is a hack that disables hardware acceleration
			//   1) for GLES20Canvas we got an UnsupportedOperationException in clipPath
			//   2) View.setLayerType(LAYER_TYPE_SOFTWARE) does not work properly in some cases
			if (myBuffer == null ||
				myBuffer.getWidth() != myWidth ||
				myBuffer.getHeight() != myHeight) {
				myBuffer = BitmapUtil.createBitmap(myWidth, myHeight, getBitmapTo().getConfig());
			}
			final Canvas softCanvas = new Canvas(myBuffer);
			drawInternalNoHack(softCanvas);
			canvas.drawBitmap(myBuffer, 0, 0, myPaint);
		} else {
			try {
				drawInternalNoHack(canvas);
			} catch (UnsupportedOperationException e) {
				myUseCanvasHack = true;
				drawInternal(canvas);
			}
		}
	}

	private void drawInternalNoHack(Canvas canvas) {
		drawBitmapTo(canvas, 0, 0, myPaint);

		final int cornerX = myStartX > myWidth / 2 ? myWidth : 0;
		final int cornerY = myStartY > myHeight / 2 ? myHeight : 0;
		final int oppositeX = Math.abs(myWidth - cornerX);
		final int oppositeY = Math.abs(myHeight - cornerY);
		final int x, y;
		if (myDirection.IsHorizontal) {
			x = myEndX;
			if (getMode().Auto) {
				y = myEndY;
			} else {
				if (cornerY == 0) {
					y = Math.max(1, Math.min(myHeight / 2, myEndY));
				} else {
					y = Math.max(myHeight / 2, Math.min(myHeight - 1, myEndY));
				}
			}
		} else {
			y = myEndY;
			if (getMode().Auto) {
				x = myEndX;
			} else {
				if (cornerX == 0) {
					x = Math.max(1, Math.min(myWidth / 2, myEndX));
				} else {
					x = Math.max(myWidth / 2, Math.min(myWidth - 1, myEndX));
				}
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

		float sX, sY;
		{
			float d1 = x - x1;
			float d2 = y - cornerY;
			sX = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
			if (cornerX == 0) {
				sX = -sX;
			}
		}
		{
			float d1 = x - cornerX;
			float d2 = y - y1;
			sY = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
			if (cornerY == 0) {
				sY = -sY;
			}
		}

		myFgPath.rewind();
		myFgPath.moveTo(x, y);
		myFgPath.lineTo((x + cornerX) / 2, (y + y1) / 2);
		myFgPath.quadTo(cornerX, y1, cornerX, y1 - sY);
		if (Math.abs(y1 - sY - cornerY) < myHeight) {
			myFgPath.lineTo(cornerX, oppositeY);
		}
		myFgPath.lineTo(oppositeX, oppositeY);
		if (Math.abs(x1 - sX - cornerX) < myWidth) {
			myFgPath.lineTo(oppositeX, cornerY);
		}
		myFgPath.lineTo(x1 - sX, cornerY);
		myFgPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2);

		myQuadPath.moveTo(x1 - sX, cornerY);
		myQuadPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2);
		canvas.drawPath(myQuadPath, myEdgePaint);
		myQuadPath.rewind();
		myQuadPath.moveTo((x + cornerX) / 2, (y + y1) / 2);
		myQuadPath.quadTo(cornerX, y1, cornerX, y1 - sY);
		canvas.drawPath(myQuadPath, myEdgePaint);
		myQuadPath.rewind();

		canvas.save();
		canvas.clipPath(myFgPath);
		drawBitmapFrom(canvas, 0, 0, myPaint);
		canvas.restore();

		myEdgePaint.setColor(ZLAndroidColorUtil.rgb(ZLAndroidColorUtil.getAverageColor(getBitmapFrom())));

		myEdgePath.rewind();
		myEdgePath.moveTo(x, y);
		myEdgePath.lineTo(
			(x + cornerX) / 2,
			(y + y1) / 2
		);
		myEdgePath.quadTo(
			(x + 3 * cornerX) / 4,
			(y + 3 * y1) / 4,
			(x + 7 * cornerX) / 8,
			(y + 7 * y1 - 2 * sY) / 8
		);
		myEdgePath.lineTo(
			(x + 7 * x1 - 2 * sX) / 8,
			(y + 7 * cornerY) / 8
		);
		myEdgePath.quadTo(
			(x + 3 * x1) / 4,
			(y + 3 * cornerY) / 4,
			(x + x1) / 2,
			(y + cornerY) / 2
		);

		canvas.drawPath(myEdgePath, myEdgePaint);
		/*
		canvas.save();
		canvas.clipPath(myEdgePath);
		final Matrix m = new Matrix();
		m.postScale(1, -1);
		m.postTranslate(x - cornerX, y + cornerY);
		final float angle;
		if (cornerY == 0) {
			angle = -180 / 3.1416f * (float)Math.atan2(x - cornerX, y - y1);
		} else {
			angle = 180 - 180 / 3.1416f * (float)Math.atan2(x - cornerX, y - y1);
		}
		m.postRotate(angle, x, y);
		canvas.drawBitmap(getBitmapFrom(), m, myBackPaint);
		canvas.restore();
		*/
	}

	@Override
	public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
		if (myDirection == null) {
			return ZLViewEnums.PageIndex.current;
		}

		switch (myDirection) {
			case leftToRight:
				return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
			case rightToLeft:
				return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
			case up:
				return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
			case down:
				return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
		}
		return ZLViewEnums.PageIndex.current;
	}

	@Override
	protected void startAnimatedScrollingInternal(int speed) {
		mySpeedFactor = (float)Math.pow(2.0, 0.25 * speed);
		mySpeed *= 1.5;
		doStep();
	}

	@Override
	protected void setupAnimatedScrollingStart(Integer x, Integer y) {
		if (x == null || y == null) {
			if (myDirection.IsHorizontal) {
				x = mySpeed < 0 ? myWidth - 3 : 3;
				y = 1;
			} else {
				x = 1;
				y = mySpeed < 0 ? myHeight - 3 : 3;
			}
		} else {
			final int cornerX = x > myWidth / 2 ? myWidth : 0;
			final int cornerY = y > myHeight / 2 ? myHeight : 0;
			int deltaX = Math.min(Math.abs(x - cornerX), myWidth / 5);
			int deltaY = Math.min(Math.abs(y - cornerY), myHeight / 5);
			if (myDirection.IsHorizontal) {
				deltaY = Math.min(deltaY, deltaX / 3);
			} else {
				deltaX = Math.min(deltaX, deltaY / 3);
			}
			x = Math.abs(cornerX - deltaX);
			y = Math.abs(cornerY - deltaY);
		}
		myEndX = myStartX = x;
		myEndY = myStartY = y;
	}

	@Override
	public void doStep() {
		if (!getMode().Auto) {
			return;
		}

		final int speed = (int)Math.abs(mySpeed);
		mySpeed *= mySpeedFactor;

		final int cornerX = myStartX > myWidth / 2 ? myWidth : 0;
		final int cornerY = myStartY > myHeight / 2 ? myHeight : 0;

		final int boundX, boundY;
		if (getMode() == Mode.AnimatedScrollingForward) {
			boundX = cornerX == 0 ? 2 * myWidth : -myWidth;
			boundY = cornerY == 0 ? 2 * myHeight : -myHeight;
		} else {
			boundX = cornerX;
			boundY = cornerY;
		}

		final int deltaX = Math.abs(myEndX - cornerX);
		final int deltaY = Math.abs(myEndY - cornerY);
		final int speedX, speedY;
		if (deltaX == 0 || deltaY == 0) {
			speedX = speed;
			speedY = speed;
		} else if (deltaX < deltaY) {
			speedX = speed;
			speedY = speed * deltaY / deltaX;
		} else {
			speedX = speed * deltaX / deltaY;
			speedY = speed;
		}

		final boolean xSpeedIsPositive, ySpeedIsPositive;
		if (getMode() == Mode.AnimatedScrollingForward) {
			xSpeedIsPositive = cornerX == 0;
			ySpeedIsPositive = cornerY == 0;
		} else {
			xSpeedIsPositive = cornerX != 0;
			ySpeedIsPositive = cornerY != 0;
		}

		if (xSpeedIsPositive) {
			myEndX += speedX;
			if (myEndX >= boundX) {
				terminate();
			}
		} else {
			myEndX -= speedX;
			if (myEndX <= boundX) {
				terminate();
			}
		}

		if (ySpeedIsPositive) {
			myEndY += speedY;
			if (myEndY >= boundY) {
				terminate();
			}
		} else {
			myEndY -= speedY;
			if (myEndY <= boundY) {
				terminate();
			}
		}
	}

	@Override
	public void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
		canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
	}

	@Override
	protected void setFilter() {
		ViewUtil.setColorLevel(myPaint, myColorLevel);
		ViewUtil.setColorLevel(myBackPaint, myColorLevel);
		ViewUtil.setColorLevel(myEdgePaint, myColorLevel);
	}
}
