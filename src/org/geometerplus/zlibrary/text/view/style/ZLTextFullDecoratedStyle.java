package org.geometerplus.zlibrary.text.view.style;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

import org.geometerplus.zlibrary.core.util.*;

public class ZLTextFullDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyle {
	private final ZLTextFullStyleDecoration myDecoration;
	
	ZLTextFullDecoratedStyle(ZLTextStyle base, ZLTextFullStyleDecoration decoration) {
		super(base);
		myDecoration = decoration;	
	}

	protected String getFontFamilyInternal() {
		String decoratedValue = myDecoration.FontFamilyOption.getValue();
		return (decoratedValue.length() != 0) ? decoratedValue : getBase().getFontFamily();
	}

	protected ZLColor getColorInternal() {
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

	protected int getFontSizeInternal() {
		return getBase().getFontSize() + myDecoration.FontSizeDeltaOption.getValue();
	}
	
	protected boolean isBoldInternal() {
		switch (myDecoration.BoldOption.getValue()) {
			case ZLBoolean3.B3_TRUE:
				return true;
			case ZLBoolean3.B3_FALSE:
				return false;
			default:
				return getBase().isBold();
		}
	}

	protected boolean isItalicInternal() {
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
	
	public int getLineSpacePercent() {
		int value = myDecoration.LineSpacePercentOption.getValue();
		return (value != -1) ? value : getBase().getLineSpacePercent();
	}

	protected int getVerticalShiftInternal() {
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

	public boolean allowHyphenations() {
		int a = myDecoration.AllowHyphenationsOption.getValue();
	      	return (a == ZLBoolean3.B3_UNDEFINED) ? getBase().allowHyphenations() : (a == ZLBoolean3.B3_TRUE);	
	}
}
