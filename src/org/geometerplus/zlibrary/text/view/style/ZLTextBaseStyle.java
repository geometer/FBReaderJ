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

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextBaseStyle implements ZLTextStyle {
	private static final String CATEGORY = ZLOption.LOOK_AND_FEEL_CATEGORY;
	private static final String COLORS = "Colors";
	private static final String GROUP = "Style";
	private static final String OPTIONS = "Options";

	public final ZLColorOption BackgroundColorOption =
		new ZLColorOption(CATEGORY, COLORS, "Background", new ZLColor(255, 255, 255));
	public final ZLColorOption SelectionBackgroundColorOption =
		new ZLColorOption(CATEGORY, COLORS, "SelectionBackground", new ZLColor(82, 131, 194));
	public final ZLColorOption HighlightedTextColorOption =
		new ZLColorOption(CATEGORY, COLORS, "SelectedText", new ZLColor(60, 139, 255));
	public final ZLColorOption RegularTextColorOption =
		new ZLColorOption(CATEGORY, COLORS, "Text", new ZLColor(0, 0, 0));
	public final ZLColorOption InternalHyperlinkTextColorOption =
		new ZLColorOption(CATEGORY, COLORS, "Hyperlink", new ZLColor(33, 96, 180));
	public final ZLColorOption ExternalHyperlinkTextColorOption =
		new ZLColorOption(CATEGORY, COLORS, "ExternalHyperlink", new ZLColor(98, 174, 26));
	public final ZLColorOption TreeLinesColorOption =
		new ZLColorOption(CATEGORY, COLORS, "TreeLines", new ZLColor(127, 127, 127));

	public final ZLBooleanOption AutoHyphenationOption =
		new ZLBooleanOption(CATEGORY, OPTIONS, "AutoHyphenation", true);

	public final ZLBooleanOption BoldOption =
		new ZLBooleanOption(CATEGORY, GROUP, "Base:bold", false);
	public final ZLBooleanOption ItalicOption =
		new ZLBooleanOption(CATEGORY, GROUP, "Base:italic", false);
	public final ZLIntegerOption AlignmentOption =
		new ZLIntegerOption(CATEGORY, GROUP, "Base:alignment", ZLTextAlignmentType.ALIGN_JUSTIFY);
	public final ZLIntegerOption LineSpacePercentOption =
		new ZLIntegerOption(CATEGORY, GROUP, "Base:lineSpacingPercent", 120);

	public final ZLStringOption FontFamilyOption;
	public final ZLIntegerRangeOption FontSizeOption;
	
	public ZLTextBaseStyle(String fontFamily, int fontSize) {
		FontFamilyOption = new ZLStringOption(CATEGORY, GROUP, "Base:fontFamily", fontFamily);
		FontSizeOption = new ZLIntegerRangeOption(CATEGORY, GROUP, "Base:fontSize", 0, 72, fontSize);
	}
	
	public String getFontFamily() {
		return FontFamilyOption.getValue();
	}

	public int getFontSize() {
		return FontSizeOption.getValue();
	}

	public ZLColor getColor() {
		return RegularTextColorOption.getValue();
	}
	
	public boolean isBold() {
		return BoldOption.getValue();
	}

	public boolean isItalic() {
		return ItalicOption.getValue();
	}

	public int getLeftIndent() {
		return 0;
	}

	public int getRightIndent() {
		return 0;
	}

	public int getFirstLineIndentDelta() {
		return 0;
	}
	
	public int getLineSpacePercent() {
		return LineSpacePercentOption.getValue();
	}

	public int getVerticalShift() {
		return 0;
	}

	public int getSpaceBefore() {
		return 0;
	}

	public int getSpaceAfter() {
		return 0;
	}

	public byte getAlignment() {
		return (byte)AlignmentOption.getValue();
	}

	public ZLTextStyle getBase() {
		return this;
	}

	public boolean allowHyphenations() {
		return true;
	}
}
