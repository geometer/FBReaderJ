package org.zlibrary.text.view.style;

import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextDecoratedStyle {
	private ZLTextStyle myBase;
	
	protected ZLTextDecoratedStyle(ZLTextStyle base) {
		myBase = base;
	}

	public boolean isDecorated() {
		return true;
	}

	public ZLTextStyle getBase() {
		return myBase;
	}
}
