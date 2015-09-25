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
	enum ProgressDisplayType {
		dontDisplay,
		asPages,
		asPercentage,
		asPagesAndPercentage
	}

	public final ZLBooleanOption ShowTOCMarks;
	public final ZLIntegerRangeOption MaxTOCMarks;
	public final ZLBooleanOption ShowClock;
	public final ZLBooleanOption ShowBattery;
	public final ZLEnumOption<ProgressDisplayType> ShowProgress;
	public final ZLStringOption Font;

	public FooterOptions() {
		ShowTOCMarks = new ZLBooleanOption("Options", "FooterShowTOCMarks", true);
		MaxTOCMarks = new ZLIntegerRangeOption("Options", "FooterMaxTOCMarks", 10, 1000, 100);
		ShowClock = new ZLBooleanOption("Options", "ShowClockInFooter", true);
		ShowBattery = new ZLBooleanOption("Options", "ShowBatteryInFooter", true);
		ShowProgress = new ZLEnumOption<ProgressDisplayType>(
			"Options", "DisplayProgressInFooter", ProgressDisplayType.asPages
		);
		final ZLBooleanOption oldShowProgress =
			new ZLBooleanOption("Options", "ShowProgressInFooter", true);
		if (!oldShowProgress.getValue()) {
			oldShowProgress.setValue(true);
			ShowProgress.setValue(ProgressDisplayType.dontDisplay);
		}
		Font = new ZLStringOption("Options", "FooterFont", "Droid Sans");
	}

	public boolean showProgressAsPercentage() {
		switch (ShowProgress.getValue()) {
			case asPercentage:
			case asPagesAndPercentage:
				return true;
			default:
				return false;
		}
	}

	public boolean showProgressAsPages() {
		switch (ShowProgress.getValue()) {
			case asPages:
			case asPagesAndPercentage:
				return true;
			default:
				return false;
		}
	}
}
