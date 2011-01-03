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

package org.geometerplus.zlibrary.text.view.style;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;

public abstract class ZLTextDecoratedStyle extends ZLTextStyle {
	// fields to be cached
	private String myFontFamily;
	private int myFontSize;
	private boolean myIsItalic;
	private boolean myIsBold;
	private boolean myIsUnderline;
	private int myVerticalShift;

	private boolean myIsNotCached = true;

	protected ZLTextDecoratedStyle(ZLTextStyle base, ZLTextHyperlink hyperlink) {
		super(base, (hyperlink != null) ? hyperlink : base.Hyperlink);
	}

	private void initCache() {
		myFontFamily = getFontFamilyInternal();
		myFontSize = getFontSizeInternal();
		myIsItalic = isItalicInternal();
		myIsBold = isBoldInternal();
		myIsUnderline = isUnderlineInternal();
		myVerticalShift = getVerticalShiftInternal();

		myIsNotCached = false;
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
	public final int getFontSize() {
		if (myIsNotCached) {
			initCache();
		}
		return myFontSize;
	}
	protected abstract int getFontSizeInternal();

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
	public final int getVerticalShift() {
		if (myIsNotCached) {
			initCache();
		}
		return myVerticalShift;
	}
	protected abstract int getVerticalShiftInternal();
}
