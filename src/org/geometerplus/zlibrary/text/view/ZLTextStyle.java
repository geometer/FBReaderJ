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

package org.geometerplus.zlibrary.text.view;

public abstract class ZLTextStyle {
	public final ZLTextStyle Base;
	public final ZLTextHyperlink Hyperlink;

	protected ZLTextStyle(ZLTextStyle base, ZLTextHyperlink hyperlink) {
		Base = (base != null) ? base : this;
		Hyperlink = hyperlink;
	}

	public abstract String getFontFamily();
	public abstract int getFontSize();

	public abstract boolean isBold();
	public abstract boolean isItalic();
	public abstract boolean isUnderline();

	public abstract int getLeftIndent();
	public abstract int getRightIndent();
	public abstract int getFirstLineIndentDelta();
	public abstract int getLineSpacePercent();
	public abstract int getVerticalShift();
	public abstract int getSpaceBefore();
	public abstract int getSpaceAfter();
	public abstract byte getAlignment();

	public abstract boolean allowHyphenations();
}
