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

class ZLTextPartiallyDecoratedStyle extends ZLTextDecoratedStyle {
	private final ZLTextStyleDecoration myDecoration;

	ZLTextPartiallyDecoratedStyle(ZLTextStyle parent, ZLTextStyleDecoration decoration, ZLTextHyperlink hyperlink) {
		super(parent, hyperlink);
		myDecoration = decoration;
	}

	@Override
	protected String getFontFamilyInternal() {
		String decoratedValue = myDecoration.FontFamilyOption.getValue();
		return (decoratedValue.length() != 0) ? decoratedValue : Parent.getFontFamily();
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics) {
		return Parent.getFontSize(metrics) + myDecoration.FontSizeDeltaOption.getValue();
	}

	@Override
	protected boolean isBoldInternal() {
		switch (myDecoration.BoldOption.getValue()) {
			case B3_TRUE:
				return true;
			case B3_FALSE:
				return false;
			default:
				return Parent.isBold();
		}
	}

	@Override
	protected boolean isItalicInternal() {
		switch (myDecoration.ItalicOption.getValue()) {
			case B3_TRUE:
				return true;
			case B3_FALSE:
				return false;
			default:
				return Parent.isItalic();
		}
	}

	@Override
	protected boolean isUnderlineInternal() {
		switch (myDecoration.UnderlineOption.getValue()) {
			case B3_TRUE:
				return true;
			case B3_FALSE:
				return false;
			default:
				return Parent.isUnderline();
		}
	}

	@Override
	protected boolean isStrikeThroughInternal() {
		switch (myDecoration.StrikeThroughOption.getValue()) {
			case B3_TRUE:
				return true;
			case B3_FALSE:
				return false;
			default:
				return Parent.isStrikeThrough();
		}
	}

	@Override
	public int getLeftIndent() {
		return Parent.getLeftIndent();
	}

	@Override
	public int getRightIndent() {
		return Parent.getRightIndent();
	}

	@Override
	public int getFirstLineIndentDelta() {
		return Parent.getFirstLineIndentDelta();
	}

	@Override
	public int getLineSpacePercent() {
		return Parent.getLineSpacePercent();
	}

	@Override
	protected int getVerticalShiftInternal() {
		return Parent.getVerticalShift() + myDecoration.VerticalShiftOption.getValue();
	}

	@Override
	public int getSpaceBefore() {
		return Parent.getSpaceBefore();
	}

	@Override
	public int getSpaceAfter() {
		return Parent.getSpaceAfter();
	}

	@Override
	public byte getAlignment() {
		return Parent.getAlignment();
	}

	@Override
	public boolean allowHyphenations() {
		switch (myDecoration.AllowHyphenationsOption.getValue()) {
			case B3_FALSE:
				return false;
			case B3_TRUE:
				return true;
			default:
				return Parent.allowHyphenations();
		}
	}
}
