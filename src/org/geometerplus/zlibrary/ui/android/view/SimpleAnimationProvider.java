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

import android.graphics.Paint;

import org.geometerplus.zlibrary.core.view.ZLView;

abstract class SimpleAnimationProvider extends AnimationProvider {
	SimpleAnimationProvider(Paint paint) {
		super(paint);
	}

	@Override
	ZLView.PageIndex getPageToScrollTo() {
		switch (myDirection) {
			case rightToLeft:
				return myStartX < myEndX ? ZLView.PageIndex.previous : ZLView.PageIndex.next;
			case leftToRight:
				return myStartX < myEndX ? ZLView.PageIndex.next : ZLView.PageIndex.previous;
			case up:
				return myStartY < myEndY ? ZLView.PageIndex.previous : ZLView.PageIndex.next;
			case down:
				return myStartY < myEndY ? ZLView.PageIndex.next : ZLView.PageIndex.previous;
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
				break;
			case rightToLeft:
				myEndX += (int)mySpeed;
				break;
			case up:
				myEndY += (int)mySpeed;
				break;
			case down:
				myEndY -= (int)mySpeed;
				break;
		}
		final int bound;
		if (getMode() == Mode.AutoScrollingForward) {
			bound = myDirection.IsHorizontal ? myWidth : myHeight;
		} else {
			bound = 0;
		}
		if (mySpeed > 0) {
			if (getScrollingShift() >= bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX + bound;
				} else {
					myEndY = myStartY + bound;
				}
				terminate();
				return;
			}
		} else {
			if (getScrollingShift() <= -bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX - bound;
				} else {
					myEndY = myStartY - bound;
				}
				terminate();
				return;
			}
		}
		mySpeed *= 1.5;
	}
}
