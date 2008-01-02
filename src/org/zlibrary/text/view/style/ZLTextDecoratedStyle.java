package org.zlibrary.text.view.style;

import org.zlibrary.core.util.ZLColor;

import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextDecoratedStyle {
	private ZLTextStyle myBase;
	
	protected ZLTextDecoratedStyle(ZLTextStyle base) {
		myBase = base;
	}

	public ZLTextStyle getBase() {
		return myBase;
	}
}
