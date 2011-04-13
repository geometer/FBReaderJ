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

package org.geometerplus.zlibrary.core.view;

import org.geometerplus.zlibrary.core.application.ZLApplication;

abstract public class ZLView {
	public final ZLApplication Application;
	protected ZLPaintContext myContext = new DummyPaintContext();

	protected ZLView(ZLApplication application) {
		Application = application;
	}

	public final ZLPaintContext getContext() {
		return myContext;
	}

	abstract public interface FooterArea {
		int getHeight();
		void paint(ZLPaintContext context);
	}

	abstract public FooterArea getFooterArea();

	public static enum PageIndex {
		previous, current, next;

		public PageIndex getNext() {
			switch (this) {
				case previous:
					return current;
				case current:
					return next;
				default:
					return null;
			}
		}

		public PageIndex getPrevious() {
			switch (this) {
				case next:
					return current;
				case current:
					return previous;
				default:
					return null;
			}
		}
	};
	public static enum Direction {
		leftToRight(true), rightToLeft(true), up(false), down(false);

		public final boolean IsHorizontal;

		Direction(boolean isHorizontal) {
			IsHorizontal = isHorizontal;
		}
	};
	public static enum Animation {
		none, curl, slide, shift
	}

	public abstract Animation getAnimationType();

	abstract public void paint(ZLPaintContext context, PageIndex pageIndex);
	abstract public void onScrollingFinished(PageIndex pageIndex);

	public boolean onFingerPress(int x, int y) {
		return false;
	}

	public boolean onFingerRelease(int x, int y) {
		return false;
	}

	public boolean onFingerMove(int x, int y) {
		return false;
	}

	public boolean onFingerLongPress(int x, int y) {
		return false;
	}

	public boolean onFingerReleaseAfterLongPress(int x, int y) {
		return false;
	}

	public boolean onFingerMoveAfterLongPress(int x, int y) {
		return false;
	}

	public boolean onFingerSingleTap(int x, int y) {
		return false;
	}

	public boolean onFingerDoubleTap(int x, int y) {
		return false;
	}

	public boolean isDoubleTapSupported() {
		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		return false;
	}

	public abstract boolean isScrollbarShown();
	public abstract int getScrollbarFullSize();
	public abstract int getScrollbarThumbPosition(PageIndex pageIndex);
	public abstract int getScrollbarThumbLength(PageIndex pageIndex);

	public abstract boolean canScroll(PageIndex index);
}
