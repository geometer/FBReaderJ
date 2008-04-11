package org.geometerplus.zlibrary.text.view;

import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class ZLTextIndicatorInfo {
	protected ZLTextIndicatorInfo() {
	}

	abstract public boolean isVisible();
	abstract public boolean isSensitive();
	abstract public boolean isTextPositionShown();
	abstract public boolean isTimeShown();
	abstract public ZLColor getColor();
	abstract public int getHeight();
	abstract public int getOffset();
	abstract public int getFontSize();

	final public int getFullHeight() {
		if (!isVisible()) {
			return 0;
		}
		return getHeight() + getOffset();
	}
}
