package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.core.options.util.*;

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
		return 0;
	}

	public int rightIndent() {
		return 0;
	}

	public int firstLineIndentDelta() {
		return getBase().firstLineIndentDelta();
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

	public int alignment() {
		return getBase().alignment();
	}
}
