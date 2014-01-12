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

package org.geometerplus.zlibrary.text.view.style;

import org.geometerplus.zlibrary.text.model.ZLTextMetrics;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;

public abstract class ZLTextDecoratedStyle extends ZLTextStyle {
	// fields to be cached
	protected final ZLTextBaseStyle BaseStyle;

	private String myFontFamily;
	private boolean myIsItalic;
	private boolean myIsBold;
	private boolean myIsUnderline;
	private boolean myIsStrikeThrough;
	private int myVerticalShift;

	private boolean myIsNotCached = true;

	private int myFontSize;
	private ZLTextMetrics myMetrics;

	protected ZLTextDecoratedStyle(ZLTextStyle base, ZLTextHyperlink hyperlink) {
		super(base, (hyperlink != null) ? hyperlink : base.Hyperlink);
		BaseStyle = base instanceof ZLTextBaseStyle
			? (ZLTextBaseStyle)base
			: ((ZLTextDecoratedStyle)base).BaseStyle;
	}

	private void initCache() {
		myFontFamily = getFontFamilyInternal();
		myIsItalic = isItalicInternal();
		myIsBold = isBoldInternal();
		myIsUnderline = isUnderlineInternal();
		myIsStrikeThrough = isStrikeThroughInternal();
		myVerticalShift = getVerticalShiftInternal();

		myIsNotCached = false;
	}

	private void initMetricsCache(ZLTextMetrics metrics) {
		myMetrics = metrics;
		myFontSize = getFontSizeInternal(metrics);
	}

	@Override
	public final String getFontFamily() {
		if (myIsNotCached) {
			initCache();
		}
		return myFontFamily;
	}
	protected abstract String getFontFamilyInternal();

	@Override
	public final int getFontSize(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myFontSize;
	}
	protected abstract int getFontSizeInternal(ZLTextMetrics metrics);

	@Override
	public final boolean isItalic() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsItalic;
	}
	protected abstract boolean isItalicInternal();

	@Override
	public final boolean isBold() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsBold;
	}
	protected abstract boolean isBoldInternal();

	@Override
	public final boolean isUnderline() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsUnderline;
	}
	protected abstract boolean isUnderlineInternal();

	@Override
	public final boolean isStrikeThrough() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsStrikeThrough;
	}
	protected abstract boolean isStrikeThroughInternal();

	@Override
	public final int getVerticalShift() {
		if (myIsNotCached) {
			initCache();
		}
		return myVerticalShift;
	}
	protected abstract int getVerticalShiftInternal();
}
