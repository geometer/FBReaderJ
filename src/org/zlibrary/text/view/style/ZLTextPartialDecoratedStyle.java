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
		return getBase().getFontSize() + (int)myDecoration.FontSizeDeltaOption.getValue();
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
	
	public boolean bold() {
//		if (myDecoration != null && myDecoration.BoldOption != null) {
		ZLBoolean3 b = myDecoration.BoldOption.getValue();	
//			if (b != null) {
		return (b == ZLBoolean3.B3_UNDEFINED) ? getBase().bold() : (b == ZLBoolean3.B3_TRUE);
//			}
//		}
//		return false;
	}

	public boolean italic() {
		ZLBoolean3 b = myDecoration.ItalicOption.getValue();
		return (b == ZLBoolean3.B3_UNDEFINED) ? getBase().italic() : (b == ZLBoolean3.B3_TRUE);
	}

	public int leftIndent() {
		return getBase().leftIndent();
	}

	public int rightIndent() {
		return getBase().rightIndent();
	}

	public int firstLineIndentDelta() {
		return getBase().firstLineIndentDelta();
	}	
	
	public double lineSpace() {
		return getBase().lineSpace();
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
		return getBase().alignment();
	}
}
