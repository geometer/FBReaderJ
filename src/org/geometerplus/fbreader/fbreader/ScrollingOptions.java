/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.*;

public final class ScrollingOptions {
	public final ZLIntegerOption ModeOption;
	public final ZLIntegerRangeOption LinesToKeepOption;
	public final ZLIntegerRangeOption LinesToScrollOption;
	public final ZLIntegerRangeOption PercentToScrollOption;

	public ScrollingOptions(String group, int mode) {
		final String category = ZLOption.CONFIG_CATEGORY;
		ModeOption = new ZLIntegerOption(category, group, "Mode", mode);
		LinesToKeepOption = new ZLIntegerRangeOption(category, group, "LinesToKeep", 1, 100, 1);
		LinesToScrollOption = new ZLIntegerRangeOption(category, group, "LinesToScroll", 1, 100, 1);
		PercentToScrollOption = new ZLIntegerRangeOption(category, group, "PercentToScrollOption", 1, 100, 50);
	}
}
