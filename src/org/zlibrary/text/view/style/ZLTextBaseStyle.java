package org.zlibrary.text.view.style;

import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLColorOption;
import org.zlibrary.core.options.ZLDoubleOption;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextBaseStyle implements ZLTextStyle {
	private final String GROUP = "Style";
	private final String OPTIONS = "Options";

	public ZLColorOption BackgroundColorOption;
	public ZLColorOption SelectionBackgroundColorOption;
	public ZLColorOption SelectedTextColorOption;
	public ZLColorOption RegularTextColorOption;
	public ZLColorOption InternalHyperlinkTextColorOption;
	public ZLColorOption ExternalHyperlinkTextColorOption;
	public ZLColorOption TreeLinesColorOption;

	public ZLBooleanOption AutoHyphenationOption;

	public ZLStringOption FontFamilyOption;
	public ZLIntegerRangeOption FontSizeOption;
	public ZLBooleanOption BoldOption;
	public ZLBooleanOption ItalicOption;
	public final ZLIntegerOption AlignmentOption =
		new ZLIntegerOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:alignment", ZLTextAlignmentType.ALIGN_JUSTIFY);
	public final ZLDoubleOption LineSpaceOption =
		new ZLDoubleOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:lineSpacing", 1.2);
	
	public ZLTextBaseStyle(String fontFamily, int fontSize) {
		//TODO
		final String COLORS = "Colors";
/*		
		BackgroundColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "Background", new ZLColor(255, 255, 255));
		SelectionBackgroundColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "SelectionBackground", new ZLColor(82, 131, 194));
		SelectedTextColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "SelectedText", new ZLColor(60, 139, 255));
		RegularTextColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "Text", new ZLColor(0, 0, 0));
		InternalHyperlinkTextColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "Hyperlink", new ZLColor(33, 96, 180));
		ExternalHyperlinkTextColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "ExternalHyperlink", new ZLColor(98, 174, 26));
		TreeLinesColorOption = new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, COLORS, "TreeLines", new ZLColor(127, 127, 127));
		
		AutoHyphenationOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "AutoHyphenation", true);
		
		FontFamilyOption = new ZLStringOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:fontFamily", fontFamily);
		FontSizeOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:fontSize", 0, 72, fontSize);
		BoldOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:bold", false);
		ItalicOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, GROUP, "Base:italic", false);
	*/
	}
	
	public String fontFamily() {
		return "default";
	}

	public int fontSize() {
		return 12;
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
	
	public int lineSpace() {
		return 1;
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

	public int alignment() {
		return AlignmentOption.getValue();
	}
}
