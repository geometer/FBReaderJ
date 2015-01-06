/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLIntegerChoicePreference extends ZLStringListPreference {
	private final ZLIntegerOption myOption;
	private final int[] myValues;

	ZLIntegerChoicePreference(Context context, ZLResource resource, ZLIntegerOption option, int[] values, String[] valueResourceKeys) {
		super(context, resource);
		assert(values.length == valueResourceKeys.length);

		myOption = option;
		myValues = values;
		setList(valueResourceKeys);

		final int initialValue = option.getValue();
		int index = 0;
		int minDiff = Math.abs(values[0] - initialValue);
		for (int i = 1; i < values.length; ++i) {
			final int diff = Math.abs(values[i] - initialValue);
			if (diff < minDiff) {
				minDiff = diff;
				index = i;
			}
		}
		setInitialValue(valueResourceKeys[index]);
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		myOption.setValue(myValues[findIndexOfValue(getValue())]);
	}
}
