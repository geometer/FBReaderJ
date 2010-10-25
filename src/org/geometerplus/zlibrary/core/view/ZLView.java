/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

abstract public class ZLView {
	protected ZLPaintContext myContext = new DummyPaintContext();

	public final ZLPaintContext getContext() {
		return myContext;
	}

	abstract public interface FooterArea {
		int getHeight();
		void paint(ZLPaintContext context);
	}

	abstract public FooterArea getFooterArea();

	public static final int PAGE_CENTRAL = 0;
	public static final int PAGE_LEFT = 1;
	public static final int PAGE_RIGHT = 2;
	public static final int PAGE_TOP = 3;
	public static final int PAGE_BOTTOM = 4;

	abstract public void paint(ZLPaintContext context, int viewPage);
	abstract public void onScrollingFinished(int viewPage);

	public boolean onStylusPress(int x, int y) {
		return false;
	}

	public boolean onStylusRelease(int x, int y) {
		return false;
	}

	public boolean onStylusMove(int x, int y) {
		return false;
	}

	public boolean onStylusMovePressed(int x, int y) {
		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		return false;
	}

	public abstract boolean isScrollbarShown();
	public abstract int getScrollbarFullSize();
	public abstract int getScrollbarThumbPosition(int viewPage);
	public abstract int getScrollbarThumbLength(int viewPage);
}
