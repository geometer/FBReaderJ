package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextBaseStyle implements ZLTextStyle {	
	public String fontFamily() {
		return "SomeFamily";
	}

	public int fontSize() {
		return 20;
	}
	
	public boolean bold() {
		return true;
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
}
