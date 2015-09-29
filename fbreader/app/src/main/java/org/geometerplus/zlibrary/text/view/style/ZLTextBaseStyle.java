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

import java.util.Collections;
import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;

public class ZLTextBaseStyle extends ZLTextStyle {
	private static final String GROUP = "Style";
	private static final String OPTIONS = "Options";

	public final ZLBooleanOption UseCSSTextAlignmentOption =
		new ZLBooleanOption("Style", "css:textAlignment", true);
	public final ZLBooleanOption UseCSSMarginsOption =
		new ZLBooleanOption("Style", "css:margins", true);
	public final ZLBooleanOption UseCSSFontSizeOption =
		new ZLBooleanOption("Style", "css:fontSize", true);
	public final ZLBooleanOption UseCSSFontFamilyOption =
		new ZLBooleanOption("Style", "css:fontFamily", true);

	public final ZLBooleanOption AutoHyphenationOption =
		new ZLBooleanOption(OPTIONS, "AutoHyphenation", true);

	public final ZLBooleanOption BoldOption;
	public final ZLBooleanOption ItalicOption;
	public final ZLBooleanOption UnderlineOption;
	public final ZLBooleanOption StrikeThroughOption;
	public final ZLIntegerRangeOption AlignmentOption;
	public final ZLIntegerRangeOption LineSpaceOption;

	public final ZLStringOption FontFamilyOption;
	public final ZLIntegerRangeOption FontSizeOption;

	public ZLTextBaseStyle(String prefix, String fontFamily, int fontSize) {
		super(null, ZLTextHyperlink.NO_LINK);
		FontFamilyOption = new ZLStringOption(GROUP, prefix + ":fontFamily", fontFamily);
		fontSize = fontSize * ZLibrary.Instance().getDisplayDPI() / 160;
		FontSizeOption = new ZLIntegerRangeOption(GROUP, prefix + ":fontSize", 5, Math.max(144, fontSize * 2), fontSize);
		BoldOption = new ZLBooleanOption(GROUP, prefix + ":bold", false);
		ItalicOption = new ZLBooleanOption(GROUP, prefix + ":italic", false);
		UnderlineOption = new ZLBooleanOption(GROUP, prefix + ":underline", false);
		StrikeThroughOption = new ZLBooleanOption(GROUP, prefix + ":strikeThrough", false);
		AlignmentOption = new ZLIntegerRangeOption(GROUP, prefix + ":alignment", 1, 4, ZLTextAlignmentType.ALIGN_JUSTIFY);
		LineSpaceOption = new ZLIntegerRangeOption(GROUP, prefix + ":lineSpacing", 5, 20, 12);
	}

	private String myFontFamily;
	private List<FontEntry> myFontEntries;
	@Override
	public List<FontEntry> getFontEntries() {
		final String family = FontFamilyOption.getValue();
		if (myFontEntries == null || !family.equals(myFontFamily)) {
			myFontEntries = Collections.singletonList(FontEntry.systemEntry(family));
		}
		return myFontEntries;
	}

	public int getFontSize() {
		return FontSizeOption.getValue();
	}

	@Override
	public int getFontSize(ZLTextMetrics metrics) {
		return getFontSize();
	}

	@Override
	public boolean isBold() {
		return BoldOption.getValue();
	}

	@Override
	public boolean isItalic() {
		return ItalicOption.getValue();
	}

	@Override
	public boolean isUnderline() {
		return UnderlineOption.getValue();
	}

	@Override
	public boolean isStrikeThrough() {
		return StrikeThroughOption.getValue();
	}

	@Override
	public int getLeftMargin(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getRightMargin(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getLeftPadding(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getRightPadding(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getFirstLineIndent(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getLineSpacePercent() {
		return LineSpaceOption.getValue() * 10;
	}

	@Override
	public int getVerticalAlign(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public boolean isVerticallyAligned() {
		return false;
	}

	@Override
	public int getSpaceBefore(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getSpaceAfter(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public byte getAlignment() {
		return (byte)AlignmentOption.getValue();
	}

	@Override
	public boolean allowHyphenations() {
		return true;
	}
}
