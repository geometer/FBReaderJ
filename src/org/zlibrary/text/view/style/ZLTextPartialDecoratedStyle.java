package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.core.util.*;

import org.zlibrary.text.model.ZLTextAlignmentType;

class ZLTextPartialDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyle {
	private final ZLTextStyleDecoration myDecoration;

	ZLTextPartialDecoratedStyle(ZLTextStyle base, ZLTextStyleDecoration decoration) {
		super(base);
		myDecoration = decoration;		
	}

	public String getFontFamily() {
		String decoratedValue = myDecoration.FontFamilyOption.getValue();
		return (decoratedValue.length() != 0) ? decoratedValue : getBase().getFontFamily();
	}

	public int getFontSize() {
		return getBase().getFontSize() + myDecoration.FontSizeDeltaOption.getValue();
	}

	public ZLColor getColor() {
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
	
	public boolean isBold() {
		switch (myDecoration.BoldOption.getValue()) {
			case ZLBoolean3.B3_TRUE:
				return true;
			case ZLBoolean3.B3_FALSE:
				return false;
			default:
				return getBase().isBold();
		}
	}

	public boolean isItalic() {
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
	
	public double getLineSpace() {
		return getBase().getLineSpace();
	}

	public int getVerticalShift() {
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
}
