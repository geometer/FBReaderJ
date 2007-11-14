package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextBaseStyle implements ZLTextStyle {	
	public String fontFamily() {
		return "SomeFamily";
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
}
