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
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextIndicatorInfo;

public class FBIndicatorInfo extends ZLTextIndicatorInfo {
	private static final String INDICATOR = "Indicator";

	public final ZLBooleanOption ShowOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "Show", true);
	public final ZLBooleanOption IsSensitiveOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "TouchSensitive", true);
	public final ZLBooleanOption ShowTextPositionOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "PositionText", true);
	public final ZLBooleanOption ShowTimeOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "Time", true);
	public final ZLColorOption ColorOption =
		new ZLColorOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "Color", new ZLColor(127, 127, 127));
	public final ZLIntegerRangeOption HeightOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "Height", 1, 100, 16);
	public final ZLIntegerRangeOption OffsetOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "Offset", 0, 100, 3);
	public final ZLIntegerRangeOption FontSizeOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, INDICATOR, "FontSize", 4, 72, 14);

	public boolean isVisible() {
		return ShowOption.getValue();
	}

	public boolean isSensitive() {
		return IsSensitiveOption.getValue();
	}

	public boolean isTextPositionShown() {
		return ShowTextPositionOption.getValue();
	}

	public boolean isTimeShown() {
		return ShowTimeOption.getValue();
	}

	public ZLColor getColor() {
		return ColorOption.getValue();
	}

	public int getHeight() {
		return HeightOption.getValue();
	}

	public int getOffset() {
		return OffsetOption.getValue();
	}

	public int getFontSize() {
		return FontSizeOption.getValue();
	}
};
