/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;

class BatteryLevelToTurnScreenOffPreference extends ZLStringListPreference {
	private final ZLIntegerRangeOption myOption;

	BatteryLevelToTurnScreenOffPreference(Context context, ZLIntegerRangeOption option, ZLResource resource) {
		super(context, resource);
		myOption = option;
		String[] entries = { "0", "25", "50", "100" };
		setList(entries);

		int value = option.getValue();
		if (value <= 0) {
			setInitialValue("0");
		} else if (value <= 25) {
			setInitialValue("25");
		} else if (value <= 50) {
			setInitialValue("50");
		} else {
			setInitialValue("100");
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		try {
			myOption.setValue(Integer.parseInt(getValue()));
		} catch (NumberFormatException e) {
		}
	}
}
