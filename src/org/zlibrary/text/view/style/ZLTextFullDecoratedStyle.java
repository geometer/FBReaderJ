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
		HyperlinkStyle hyperlinkStyle = myDecoration.getHyperlinkStyle();
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
		return getBase().getFontSize() + (int)myDecoration.FontSizeDeltaOption.getValue();
	}
	
	public boolean bold() {
		ZLBoolean3 value = myDecoration.BoldOption.getValue();	
		return (value == ZLBoolean3.B3_UNDEFINED) ?
			getBase().bold() :
			(value == ZLBoolean3.B3_TRUE);
	}

	public boolean italic() {
		ZLBoolean3 value = myDecoration.ItalicOption.getValue();
		return (value == ZLBoolean3.B3_UNDEFINED) ?
			getBase().italic() :
			(value == ZLBoolean3.B3_TRUE);
	}

	public int leftIndent() {
		return getBase().leftIndent() + myDecoration.LeftIndentOption.getValue();
	}

	public int rightIndent() {
		return getBase().rightIndent() + myDecoration.RightIndentOption.getValue();
	}

	public int firstLineIndentDelta() {
		return (alignment() == ZLTextAlignmentType.ALIGN_CENTER) ? 0 : getBase().firstLineIndentDelta() + myDecoration.FirstLineIndentDeltaOption.getValue();
	}
	
	public double lineSpace() {
		double value = myDecoration.LineSpaceOption.getValue();
		return (value != 0.0) ? value : getBase().lineSpace();
	}

	public int verticalShift() {
		return getBase().verticalShift() + myDecoration.VerticalShiftOption.getValue();
	}

	public int spaceBefore() {
		return 0;
	}

	public int spaceAfter() {
		return 0;
	}		

	public byte alignment() {
		byte value = (byte)myDecoration.AlignmentOption.getValue();
		return (value == ZLTextAlignmentType.ALIGN_UNDEFINED) ? getBase().alignment() : value;
	}
}
