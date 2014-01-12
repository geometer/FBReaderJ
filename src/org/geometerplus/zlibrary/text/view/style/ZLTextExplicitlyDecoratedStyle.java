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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextExplicitlyDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyleEntry.Feature, ZLTextStyleEntry.FontModifier {
	private final ZLTextStyleEntry myEntry;

	public ZLTextExplicitlyDecoratedStyle(ZLTextStyle parent, ZLTextStyleEntry entry) {
		super(parent, parent.Hyperlink);
		myEntry = entry;
	}

	@Override
	protected String getFontFamilyInternal() {
		if (myEntry.isFeatureSupported(FONT_FAMILY)) {
			// TODO: implement
		}
		return Parent.getFontFamily();
	}
	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSFontSizeOption.getValue()) {
			return Parent.getFontSize(metrics);
		}
		if (myEntry.isFeatureSupported(FONT_STYLE_MODIFIER)) {
			if (myEntry.getFontModifier(FONT_MODIFIER_INHERIT) == ZLBoolean3.B3_TRUE) {
				return Parent.Parent.getFontSize(metrics);
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_LARGER) == ZLBoolean3.B3_TRUE) {
				return Parent.Parent.getFontSize(metrics) * 120 / 100;
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_SMALLER) == ZLBoolean3.B3_TRUE) {
				return Parent.Parent.getFontSize(metrics) * 100 / 120;
			}
		}
		if (myEntry.isFeatureSupported(LENGTH_FONT_SIZE)) {
			return myEntry.getLength(LENGTH_FONT_SIZE, metrics);
		}
		return Parent.getFontSize(metrics);
	}

	@Override
	protected boolean isBoldInternal() {
		switch (myEntry.getFontModifier(FONT_MODIFIER_BOLD)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_ITALIC)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_UNDERLINED)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_STRIKEDTHROUGH)) {
			case B3_TRUE:
				return true;
			case B3_FALSE:
				return false;
			default:
				return Parent.isStrikeThrough();
		}
	}

	public int getLeftIndent() {
		// TODO: implement
		return Parent.getLeftIndent();
	}
	public int getRightIndent() {
		// TODO: implement
		return Parent.getRightIndent();
	}
	public int getFirstLineIndentDelta() {
		// TODO: implement
		return Parent.getFirstLineIndentDelta();
	}
	public int getLineSpacePercent() {
		// TODO: implement
		return Parent.getLineSpacePercent();
	}
	@Override
	protected int getVerticalShiftInternal() {
		// TODO: implement
		return Parent.getVerticalShift();
	}
	public int getSpaceBefore() {
		// TODO: implement
		return Parent.getSpaceBefore();
	}
	public int getSpaceAfter() {
		// TODO: implement
		return Parent.getSpaceAfter();
	}
	public byte getAlignment() {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSTextAlignmentOption.getValue()) {
			return Parent.getAlignment();
		}
		return
			myEntry.isFeatureSupported(ALIGNMENT_TYPE)
				? myEntry.getAlignmentType()
				: Parent.getAlignment();
	}

	public boolean allowHyphenations() {
		// TODO: implement
		return Parent.allowHyphenations();
	}
}
