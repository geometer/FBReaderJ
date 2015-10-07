/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.ArrayList;
import java.util.List;

import org.fbreader.util.Boolean3;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextExplicitlyDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyleEntry.Feature, ZLTextStyleEntry.FontModifier {
	private final ZLTextStyleEntry myEntry;

	public ZLTextExplicitlyDecoratedStyle(ZLTextStyle parent, ZLTextStyleEntry entry) {
		super(parent, parent.Hyperlink);
		myEntry = entry;
	}

	@Override
	protected List<FontEntry> getFontEntriesInternal() {
		final List<FontEntry> parentEntries = Parent.getFontEntries();
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSFontFamilyOption.getValue()) {
			return parentEntries;
		}

		if (!myEntry.isFeatureSupported(FONT_FAMILY)) {
			return parentEntries;
		}

		final List<FontEntry> entries = myEntry.getFontEntries();
		final int lSize = entries.size();
		if (lSize == 0) {
			return parentEntries;
		}

		final int pSize = parentEntries.size();
		if (pSize > lSize && entries.equals(parentEntries.subList(0, lSize))) {
			return parentEntries;
		}

		final List<FontEntry> allEntries = new ArrayList<FontEntry>(pSize + lSize);
		allEntries.addAll(entries);
		allEntries.addAll(parentEntries);
		return allEntries;
	}

	private ZLTextStyle myTreeParent;
	private ZLTextStyle computeTreeParent() {
		if (myEntry.Depth == 0) {
			return Parent.Parent;
		}
		int count = 0;
		ZLTextStyle p = Parent;
		for (; p != p.Parent; p = p.Parent) {
			if (p instanceof ZLTextExplicitlyDecoratedStyle) {
				if (((ZLTextExplicitlyDecoratedStyle)p).myEntry.Depth != myEntry.Depth) {
					return p;
				}
			} else {
				if (++count > 1) {
					return p;
				}
			}
		}
		return p;
	}
	private ZLTextStyle getTreeParent() {
		if (myTreeParent == null) {
			myTreeParent = computeTreeParent();
		}
		return myTreeParent;
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSFontSizeOption.getValue()) {
			return Parent.getFontSize(metrics);
		}

		final int baseFontSize = getTreeParent().getFontSize(metrics);
		if (myEntry.isFeatureSupported(FONT_STYLE_MODIFIER)) {
			if (myEntry.getFontModifier(FONT_MODIFIER_INHERIT) == Boolean3.TRUE) {
				return baseFontSize;
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_LARGER) == Boolean3.TRUE) {
				return baseFontSize * 120 / 100;
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_SMALLER) == Boolean3.TRUE) {
				return baseFontSize * 100 / 120;
			}
		}
		if (myEntry.isFeatureSupported(LENGTH_FONT_SIZE)) {
			return myEntry.getLength(LENGTH_FONT_SIZE, metrics, baseFontSize);
		}
		return Parent.getFontSize(metrics);
	}

	@Override
	protected boolean isBoldInternal() {
		switch (myEntry.getFontModifier(FONT_MODIFIER_BOLD)) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.isBold();
		}
	}
	@Override
	protected boolean isItalicInternal() {
		switch (myEntry.getFontModifier(FONT_MODIFIER_ITALIC)) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.isItalic();
		}
	}
	@Override
	protected boolean isUnderlineInternal() {
		switch (myEntry.getFontModifier(FONT_MODIFIER_UNDERLINED)) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.isUnderline();
		}
	}
	@Override
	protected boolean isStrikeThroughInternal() {
		switch (myEntry.getFontModifier(FONT_MODIFIER_STRIKEDTHROUGH)) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.isStrikeThrough();
		}
	}

	@Override
	public int getLeftMarginInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getLeftMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_LEFT)) {
			return Parent.getLeftMargin(metrics);
		}
		return getTreeParent().getLeftMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_LEFT, metrics, fontSize);
	}
	@Override
	public int getRightMarginInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getRightMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_RIGHT)) {
			return Parent.getRightMargin(metrics);
		}
		return getTreeParent().getRightMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_RIGHT, metrics, fontSize);
	}
	@Override
	public int getLeftPaddingInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getLeftPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_LEFT)) {
			return Parent.getLeftPadding(metrics);
		}
		return getTreeParent().getLeftPadding(metrics) + myEntry.getLength(LENGTH_PADDING_LEFT, metrics, fontSize);
	}
	@Override
	public int getRightPaddingInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getRightPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_RIGHT)) {
			return Parent.getRightPadding(metrics);
		}
		return getTreeParent().getRightPadding(metrics) + myEntry.getLength(LENGTH_PADDING_RIGHT, metrics, fontSize);
	}
	@Override
	protected int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getFirstLineIndent(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_FIRST_LINE_INDENT)) {
			return Parent.getFirstLineIndent(metrics);
		}
		return myEntry.getLength(LENGTH_FIRST_LINE_INDENT, metrics, fontSize);
	}
	@Override
	protected int getLineSpacePercentInternal() {
		// TODO: implement
		return Parent.getLineSpacePercent();
	}
	@Override
	protected int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize) {
		// TODO: implement
		if (myEntry.isFeatureSupported(LENGTH_VERTICAL_ALIGN)) {
			return myEntry.getLength(LENGTH_VERTICAL_ALIGN, metrics, fontSize);
		} else if (myEntry.isFeatureSupported(NON_LENGTH_VERTICAL_ALIGN)) {
			switch (myEntry.getVerticalAlignCode()) {
				default:
					return Parent.getVerticalAlign(metrics);
				case 0: // sub
					return ZLTextStyleEntry.compute(
						new ZLTextStyleEntry.Length((short)-50, ZLTextStyleEntry.SizeUnit.EM_100),
						metrics, fontSize, LENGTH_VERTICAL_ALIGN
					);
				case 1: // super
					return ZLTextStyleEntry.compute(
						new ZLTextStyleEntry.Length((short)50, ZLTextStyleEntry.SizeUnit.EM_100),
						metrics, fontSize, LENGTH_VERTICAL_ALIGN
					);
				/*
				case 2: // top
					return 0;
				case 3: // text-top
					return 0;
				case 4: // middle
					return 0;
				case 5: // bottom
					return 0;
				case 6: // text-bottom
					return 0;
				case 7: // initial
					return 0;
				case 8: // inherit
					return 0;
				*/
			}
		} else {
			return Parent.getVerticalAlign(metrics);
		}
	}
	@Override
	protected boolean isVerticallyAlignedInternal() {
		if (myEntry.isFeatureSupported(LENGTH_VERTICAL_ALIGN)) {
			return myEntry.hasNonZeroLength(LENGTH_VERTICAL_ALIGN);
		} else if (myEntry.isFeatureSupported(NON_LENGTH_VERTICAL_ALIGN)) {
			switch (myEntry.getVerticalAlignCode()) {
				default:
					return false;
				case 0: // sub
				case 1: // super
					return true;
			}
		} else {
			return false;
		}
	}

	@Override
	protected int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getSpaceBefore(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_SPACE_BEFORE)) {
			return Parent.getSpaceBefore(metrics);
		}
		return myEntry.getLength(LENGTH_SPACE_BEFORE, metrics, fontSize);
	}
	@Override
	protected int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize) {
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getSpaceAfter(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_SPACE_AFTER)) {
			return Parent.getSpaceAfter(metrics);
		}
		return myEntry.getLength(LENGTH_SPACE_AFTER, metrics, fontSize);
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
