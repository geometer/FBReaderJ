/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

class ZLTextPartialDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyle {
	private final ZLTextStyleDecoration myDecoration;

	ZLTextPartialDecoratedStyle(ZLTextStyle base, ZLTextStyleDecoration decoration) {
		super(base);
		myDecoration = decoration;		
	}

	protected String getFontFamilyInternal() {
		String decoratedValue = myDecoration.FontFamilyOption.getValue();
		return (decoratedValue.length() != 0) ? decoratedValue : getBase().getFontFamily();
	}

	protected int getFontSizeInternal() {
		return getBase().getFontSize() + myDecoration.FontSizeDeltaOption.getValue();
	}

	protected ZLColor getColorInternal() {
		byte hyperlinkStyle = myDecoration.getHyperlinkStyle();
		if (hyperlinkStyle == HyperlinkStyle.NONE) {
			return getBase().getColor();
		}
		ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().getBaseStyle();
		if (hyperlinkStyle == HyperlinkStyle.INTERNAL) {
			return baseStyle.InternalHyperlinkTextColorOption.getValue();
		} else {
			return baseStyle.ExternalHyperlinkTextColorOption.getValue();
		}
	}
	
	protected boolean isBoldInternal() {
		switch (myDecoration.BoldOption.getValue()) {
			case ZLBoolean3.B3_TRUE:
				return true;
			case ZLBoolean3.B3_FALSE:
				return false;
			default:
				return getBase().isBold();
		}
	}

	protected boolean isItalicInternal() {
		switch (myDecoration.ItalicOption.getValue()) {
			case ZLBoolean3.B3_TRUE:
				return true;
			case ZLBoolean3.B3_FALSE:
				return false;
			default:
				return getBase().isItalic();
		}
	}

	public int getLeftIndent() {
		return getBase().getLeftIndent();
	}

	public int getRightIndent() {
		return getBase().getRightIndent();
	}

	public int getFirstLineIndentDelta() {
		return getBase().getFirstLineIndentDelta();
	}	
	
	public int getLineSpacePercent() {
		return getBase().getLineSpacePercent();
	}

	protected int getVerticalShiftInternal() {
		return getBase().getVerticalShift() + myDecoration.VerticalShiftOption.getValue();
	}

	public int getSpaceBefore() {
		return getBase().getSpaceBefore();
	}

	public int getSpaceAfter() {
		return getBase().getSpaceAfter();
	}		

	public byte getAlignment() {
		return getBase().getAlignment();
	}

	public boolean allowHyphenations() {
		int a = myDecoration.AllowHyphenationsOption.getValue();
		return (a == ZLBoolean3.B3_UNDEFINED) ? getBase().allowHyphenations() : (a == ZLBoolean3.B3_TRUE);
	}
}
