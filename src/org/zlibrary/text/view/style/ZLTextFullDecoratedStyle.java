package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;
import org.zlibrary.text.model.ZLTextAlignmentType;

import org.zlibrary.core.util.*;

public class ZLTextFullDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyle {
	private final ZLTextFullStyleDecoration myDecoration;
	
	ZLTextFullDecoratedStyle(ZLTextStyle base, ZLTextFullStyleDecoration decoration) {
		super(base);
		myDecoration = decoration;	
	}

	public String getFontFamily() {
		String decoratedValue = myDecoration.FontFamilyOption.getValue();
		return (decoratedValue.length() != 0) ? decoratedValue : getBase().getFontFamily();
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

	public int getFontSize() {
		return getBase().getFontSize() + myDecoration.FontSizeDeltaOption.getValue();
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
		return getBase().getLeftIndent() + myDecoration.LeftIndentOption.getValue();
	}

	public int getRightIndent() {
		return getBase().getRightIndent() + myDecoration.RightIndentOption.getValue();
	}

	public int getFirstLineIndentDelta() {
		return (getAlignment() == ZLTextAlignmentType.ALIGN_CENTER) ? 0 : getBase().getFirstLineIndentDelta() + myDecoration.FirstLineIndentDeltaOption.getValue();
	}
	
	public double getLineSpace() {
		double value = myDecoration.LineSpaceOption.getValue();
		return (value != 0.0) ? value : getBase().getLineSpace();
	}

	public int getVerticalShift() {
		return getBase().getVerticalShift() + myDecoration.VerticalShiftOption.getValue();
	}

	public int getSpaceBefore() {
		return myDecoration.SpaceBeforeOption.getValue();
	}

	public int getSpaceAfter() {
		return myDecoration.SpaceAfterOption.getValue();
	}		

	public byte getAlignment() {
		byte value = (byte)myDecoration.AlignmentOption.getValue();
		return (value == ZLTextAlignmentType.ALIGN_UNDEFINED) ? getBase().getAlignment() : value;
	}
}
