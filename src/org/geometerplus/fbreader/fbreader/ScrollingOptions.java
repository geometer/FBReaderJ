package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.*;

public final class ScrollingOptions {
	public final ZLIntegerRangeOption DelayOption;
	public final ZLIntegerOption ModeOption;
	public final ZLIntegerRangeOption LinesToKeepOption;
	public final ZLIntegerRangeOption LinesToScrollOption;
	public final ZLIntegerRangeOption PercentToScrollOption;

	public ScrollingOptions(String group, int delay, int mode) {
		final String category = ZLOption.CONFIG_CATEGORY;
		DelayOption = new ZLIntegerRangeOption(category, group, "ScrollingDelay", 0, 5000, delay);
		ModeOption = new ZLIntegerOption(category, group, "Mode", mode);
		LinesToKeepOption = new ZLIntegerRangeOption(category, group, "LinesToKeep", 1, 100, 1);
		LinesToScrollOption = new ZLIntegerRangeOption(category, group, "LinesToScroll", 1, 100, 1);
		PercentToScrollOption = new ZLIntegerRangeOption(category, group, "PercentToScrollOption", 1, 100, 50);
	}
}
