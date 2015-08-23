/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.fbreader.options;

import org.geometerplus.zlibrary.core.options.*;

public class FooterOptions {
	public final ZLBooleanOption ShowTOCMarks;
	public final ZLBooleanOption ShowClock;
	public final ZLBooleanOption ShowBattery;

	public final ZLIntegerRangeOption ShowProgressType;

	public final ZLStringOption Font;

	public FooterOptions() {
		ShowTOCMarks = new ZLBooleanOption("Options", "FooterShowTOCMarks", true);
		ShowClock = new ZLBooleanOption("Options", "ShowClockInFooter", true);
		ShowBattery = new ZLBooleanOption("Options", "ShowBatteryInFooter", true);
		Font = new ZLStringOption("Options", "FooterFont", "Droid Sans");

		ShowProgressType = new ZLIntegerRangeOption("Options", "ShowProgressType", 0, 4, ProgressTypes.showProgressAsPages.ordinal());
	}

	public boolean showProgressAsPercentage() {
		return ShowProgressType.getValue() == ProgressTypes.showProgressAsPercentage.ordinal() ||
				ShowProgressType.getValue() == ProgressTypes.showProgressAsBoth.ordinal();
	}

	public boolean showProgressAsPages() {
		return ShowProgressType.getValue() == ProgressTypes.showProgressAsPages.ordinal() ||
				ShowProgressType.getValue() == ProgressTypes.showProgressAsBoth.ordinal();
	}


	public String[] getProgressValueResourceKeys() {
		ProgressTypes[] progressTypes = ProgressTypes.values();
		String[] resourceKeys = new String[progressTypes.length];

		for (int i = 0; i < progressTypes.length; i++) {
			resourceKeys[i] = progressTypes[i].name();
		}

		return resourceKeys;
	}
}

enum ProgressTypes {
	hide,
	showProgressAsPages,
	showProgressAsPercentage,
	showProgressAsBoth
}