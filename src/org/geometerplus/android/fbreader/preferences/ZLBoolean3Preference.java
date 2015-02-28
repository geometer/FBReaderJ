/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.options.ZLBoolean3Option;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLBoolean3Preference extends ZLStringListPreference {
	private static final String ON = "summaryOn";
	private static final String OFF = "summaryOff";
	private static final String UNCHANGED = "unchanged";

	private final ZLBoolean3Option myOption;

	ZLBoolean3Preference(Context context, ZLResource resource, ZLBoolean3Option option) {
		super(context, resource);

		myOption = option;
		setList(new String[] { ON, OFF, UNCHANGED });

		switch (option.getValue()) {
			case B3_TRUE:
				setInitialValue(ON);
				break;
			case B3_FALSE:
				setInitialValue(OFF);
				break;
			case B3_UNDEFINED:
				setInitialValue(UNCHANGED);
				break;
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		final String value = getValue();
		if (ON.equals(value)) {
			myOption.setValue(ZLBoolean3.B3_TRUE);
		} else if (OFF.equals(value)) {
			myOption.setValue(ZLBoolean3.B3_FALSE);
		} else {
			myOption.setValue(ZLBoolean3.B3_UNDEFINED);
		}
	}
}
