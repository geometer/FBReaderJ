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
import android.preference.ListPreference;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;

class ZLIntegerRangePreference extends ListPreference {
	private final ZLIntegerRangeOption myOption;

	ZLIntegerRangePreference(Context context, ZLResource resource, ZLIntegerRangeOption option) {
		super(context);
		myOption = option;
		setTitle(resource.getValue());
		String[] entries = new String[option.MaxValue - option.MinValue + 1];
		for (int i = 0; i < entries.length; ++i) {
			entries[i] = ((Integer)(i + option.MinValue)).toString();
		}
		setEntries(entries);
		setEntryValues(entries);
		setValueIndex(option.getValue() - option.MinValue);
		setSummary(getValue());
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			setSummary(value);
			myOption.setValue(myOption.MinValue + findIndexOfValue(value));
		}
	}
}
