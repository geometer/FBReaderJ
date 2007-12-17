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

	public ZLBooleanOption AutoHyphenationOption;

	public final ZLStringOption FontFamilyOption;
	public final ZLIntegerRangeOption FontSizeOption;
	public ZLBooleanOption BoldOption;
	public ZLBooleanOption ItalicOption;
	public final ZLIntegerOption AlignmentOption;
	public final ZLDoubleOption LineSpaceOption;
	
	public ZLTextBaseStyle(String fontFamily, int fontSize) {
		//TODO
/*
		AutoHyphenationOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "AutoHyphenation", true);
		
		BoldOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:bold", false);
		ItalicOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:italic", false);
	*/
		final String category = ZLOption.LOOK_AND_FEEL_CATEGORY;
		AlignmentOption = new ZLIntegerOption(category, GROUP, "Base:alignment", ZLTextAlignmentType.ALIGN_JUSTIFY);
		LineSpaceOption = new ZLDoubleOption(category, GROUP, "Base:lineSpacing", 1.2);
		FontFamilyOption = new ZLStringOption(category, GROUP, "Base:fontFamily", fontFamily);
		FontSizeOption = new ZLIntegerRangeOption(category, GROUP, "Base:fontSize", 0, 72, fontSize);
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
	
	public boolean bold() {
		return false;
	}

	public boolean italic() {
		return false;
	}

	public int leftIndent() {
		return 0;
	}

	public int rightIndent() {
		return 0;
	}

	public int firstLineIndentDelta() {
		return 0;
	}
	
	public double lineSpace() {
		return LineSpaceOption.getValue();
	}

	public int verticalShift() {
		return 0;
	}

	public int spaceBefore() {
		return 0;
	}

	public int spaceAfter() {
		return 0;
	}

	public boolean isDecorated() {
		return false;
	}

	public byte alignment() {
		return (byte)AlignmentOption.getValue();
	}
}
