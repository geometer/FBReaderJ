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

import java.util.List;
import java.util.ArrayList;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextSimpleDecoratedStyle extends ZLTextDecoratedStyle {
	protected final ZLTextStyleDecoration myDecoration;

	public ZLTextSimpleDecoratedStyle(ZLTextStyle parent, ZLTextStyleDecoration decoration, ZLTextHyperlink hyperlink) {
		super(parent, hyperlink);
		myDecoration = decoration;
	}

	@Override
	protected List<FontEntry> getFontEntriesInternal() {
		final List<FontEntry> parentEntries = Parent.getFontEntries();
		final String decoratedValue = myDecoration.FontFamilyOption.getValue();
		if ("".equals(decoratedValue)) {
			return parentEntries;
		}
		final FontEntry e = FontEntry.systemEntry(decoratedValue);
		if (e.equals(parentEntries.get(parentEntries.size() - 1))) {
			return parentEntries;
		}
		final List<FontEntry> entries = new ArrayList<FontEntry>(parentEntries.size() + 1);
		entries.add(e);
		entries.addAll(parentEntries);
		return entries;
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics) {
		return Parent.getFontSize(metrics) + myDecoration.FontSizeDeltaOption.getValue();
	}

	@Override
	protected int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize) {
		return Parent.getVerticalAlign(metrics) + myDecoration.VerticalAlignOption.getValue();
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

	@Override
	public int getLeftIndentInternal(ZLTextMetrics metrics, int fontSize) {
		return Parent.getLeftIndent(metrics) + myDecoration.LeftIndentOption.getValue();
	}

	@Override
	public int getRightIndentInternal(ZLTextMetrics metrics, int fontSize) {
		return Parent.getRightIndent(metrics) + myDecoration.RightIndentOption.getValue();
	}

	@Override
	public int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize) {
		return getAlignment() == ZLTextAlignmentType.ALIGN_CENTER ? 0 : Parent.getFirstLineIndent(metrics) + myDecoration.FirstLineIndentDeltaOption.getValue();
	}

	@Override
	public int getLineSpacePercent() {
		int value = myDecoration.LineSpacePercentOption.getValue();
		return (value != -1) ? value : Parent.getLineSpacePercent();
	}

	@Override
	protected int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize) {
		return myDecoration.SpaceBeforeOption.getValue();
	}

	@Override
	protected int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize) {
		return myDecoration.SpaceAfterOption.getValue();
	}

	@Override
	public byte getAlignment() {
		byte value = (byte)myDecoration.AlignmentOption.getValue();
		return (value == ZLTextAlignmentType.ALIGN_UNDEFINED) ? Parent.getAlignment() : value;
	}
}
