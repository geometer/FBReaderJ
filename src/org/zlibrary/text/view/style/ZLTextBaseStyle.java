package org.zlibrary.text.view.style;

import org.zlibrary.core.options.*;
import org.zlibrary.core.util.ZLColor;
import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextBaseStyle implements ZLTextStyle {
	private final String CATEGORY = ZLOption.LOOK_AND_FEEL_CATEGORY;
	private final String COLORS = "Colors";
	private final String GROUP = "Style";
	private final String OPTIONS = "Options";

	public final ZLColorOption BackgroundColorOption =
		new ZLColorOption(CATEGORY, COLORS, "Background", new ZLColor(255, 255, 255));
	public final ZLColorOption SelectionBackgroundColorOption =
		new ZLColorOption(CATEGORY, COLORS, "SelectionBackground", new ZLColor(82, 131, 194));
	public final ZLColorOption SelectedTextColorOption =
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
