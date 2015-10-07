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
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextNGStyle extends ZLTextDecoratedStyle {
	private final ZLTextNGStyleDescription myDescription;

	public ZLTextNGStyle(ZLTextStyle parent, ZLTextNGStyleDescription description, ZLTextHyperlink hyperlink) {
		super(parent, hyperlink);
		myDescription = description;
	}

	@Override
	protected List<FontEntry> getFontEntriesInternal() {
		final List<FontEntry> parentEntries = Parent.getFontEntries();
		final String decoratedValue = myDescription.FontFamilyOption.getValue();
		if ("".equals(decoratedValue)) {
			return parentEntries;
		}
		final FontEntry e = FontEntry.systemEntry(decoratedValue);
		if (parentEntries.size() > 0 && e.equals(parentEntries.get(0))) {
			return parentEntries;
		}
		final List<FontEntry> entries = new ArrayList<FontEntry>(parentEntries.size() + 1);
		entries.add(e);
		entries.addAll(parentEntries);
		return entries;
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics) {
		return myDescription.getFontSize(metrics, Parent.getFontSize(metrics));
	}

	@Override
	protected boolean isBoldInternal() {
		switch (myDescription.isBold()) {
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
		switch (myDescription.isItalic()) {
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
		switch (myDescription.isUnderlined()) {
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
		switch (myDescription.isStrikedThrough()) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.isStrikeThrough();
		}
	}

	@Override
	protected int getLeftMarginInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getLeftMargin(metrics, Parent.getLeftMargin(metrics), fontSize);
	}
	@Override
	protected int getRightMarginInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getRightMargin(metrics, Parent.getRightMargin(metrics), fontSize);
	}
	@Override
	protected int getLeftPaddingInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getLeftPadding(metrics, Parent.getLeftPadding(metrics), fontSize);
	}
	@Override
	protected int getRightPaddingInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getRightPadding(metrics, Parent.getRightPadding(metrics), fontSize);
	}
	@Override
	protected int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getFirstLineIndent(metrics, Parent.getFirstLineIndent(metrics), fontSize);
	}
	@Override
	protected int getLineSpacePercentInternal() {
		final String lineHeight = myDescription.LineHeightOption.getValue();
		if (!lineHeight.matches("[1-9][0-9]*%")) {
			return Parent.getLineSpacePercent();
		}
		return Integer.valueOf(lineHeight.substring(0, lineHeight.length() - 1));
	}
	@Override
	protected int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getVerticalAlign(metrics, Parent.getVerticalAlign(metrics), fontSize);
	}
	@Override
	protected boolean isVerticallyAlignedInternal() {
		return myDescription.hasNonZeroVerticalAlign();
	}
	@Override
	protected int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getSpaceBefore(metrics, Parent.getSpaceBefore(metrics), fontSize);
	}
	@Override
	protected int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize) {
		return myDescription.getSpaceAfter(metrics, Parent.getSpaceAfter(metrics), fontSize);
	}

	@Override
	public byte getAlignment() {
		final byte defined = myDescription.getAlignment();
		if (defined != ZLTextAlignmentType.ALIGN_UNDEFINED) {
			return defined;
		}
		return Parent.getAlignment();
	}

	@Override
	public boolean allowHyphenations() {
		switch (myDescription.allowHyphenations()) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			default:
				return Parent.allowHyphenations();
		}
	}

	@Override
	public String toString() {
		return "ZLTextNGStyle[" + myDescription.Name + "]";
	}
}
