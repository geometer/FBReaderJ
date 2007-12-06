package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;
import org.zlibrary.text.model.ZLTextAlignmentType;

import org.zlibrary.core.options.util.*;

public class ZLTextFullDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyle {
	private final ZLTextFullStyleDecoration myDecoration;
	
	/*package*/ ZLTextFullDecoratedStyle(ZLTextStyle base, ZLTextFullStyleDecoration decoration) {
		super(base);
		myDecoration = decoration;	
	}

	public String fontFamily() {
		return "default";
	}

	public int fontSize() {
//		if (myDecoration.FontSizeDeltaOption != null && getBase() != null) {
//			System.out.println("Delta = " + myDecoration.FontSizeDeltaOption.getValue());
			return getBase().fontSize() + (int) myDecoration.FontSizeDeltaOption.getValue();
//		} 
//		return 12;
	}
	
	public boolean bold() {
//		if (myDecoration != null && myDecoration.BoldOption != null) {
		ZLBoolean3 value  = myDecoration.BoldOption.getValue();	
//			if (value != null) {
		return (value  == ZLBoolean3.B3_UNDEFINED) ? getBase().bold() : (value == ZLBoolean3.B3_TRUE);
//			}
//		}
//		return false;
	}

	public boolean italic() {
		ZLBoolean3 value = myDecoration.ItalicOption.getValue();
		return (value  == ZLBoolean3.B3_UNDEFINED) ? getBase().italic() : (value == ZLBoolean3.B3_TRUE);
	}

	public int leftIndent() {
		return 0;
	}

	public int rightIndent() {
		return 0;
	}

	public int firstLineIndentDelta() {
		return (alignment() == ZLTextAlignmentType.ALIGN_CENTER) ? 0 : getBase().firstLineIndentDelta() + myDecoration.FirstLineIndentDeltaOption.getValue();
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
		int value = (int) myDecoration.AlignmentOption.getValue();
		return (value == ZLTextAlignmentType.ALIGN_UNDEFINED) ? getBase().alignment() : value;
	}
}
