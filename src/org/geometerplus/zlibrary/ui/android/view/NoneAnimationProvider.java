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

class NoneAnimationProvider extends AnimationProvider {
	private final Paint myPaint = new Paint();

	NoneAnimationProvider(BitmapManager bitmapManager) {
		super(bitmapManager);
	}

	@Override
	protected void drawInternal(Canvas canvas) {
		canvas.drawBitmap(getBitmapFrom(), 0, 0, myPaint);
	}

	@Override
	void doStep() {
		if (getMode().Auto) {
			terminate();
		}
	}

	private ZLView.PageIndex myPageToScrollTo = ZLView.PageIndex.current;

	@Override
	protected void startAutoScrollingInternal(boolean forward, float startSpeed, ZLView.Direction direction, int w, int h, Integer x, Integer y, int speed) {
		super.startAutoScrollingInternal(forward, startSpeed, direction, w, h, x, y, speed);
		switch (direction) {
			case rightToLeft:
			case up:
				myPageToScrollTo =
					startSpeed > 0 ? ZLView.PageIndex.previous : ZLView.PageIndex.next;
				break;
			case leftToRight:
			case down:
				myPageToScrollTo =
					startSpeed > 0 ? ZLView.PageIndex.next : ZLView.PageIndex.previous;
				break;
		}
	}

	@Override
	ZLView.PageIndex getPageToScrollTo() {
		return myPageToScrollTo;
	}
}
