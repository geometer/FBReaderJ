/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLChoicePreference extends ZLStringListPreference {
	private final ZLIntegerRangeOption myOption;

	ZLChoicePreference(Context context, ZLResource resource, String resourceKey, ZLIntegerRangeOption option, String[] valueResourceKeys) {
		super(context, resource, resourceKey);

		assert(option.MaxValue - option.MinValue + 1 == valueResourceKeys.length);

		myOption = option;
		setList(valueResourceKeys);

		setInitialValue(valueResourceKeys[option.getValue() - option.MinValue]);
	}

	public void onAccept() {
		myOption.setValue(findIndexOfValue(getValue()));
	}
}
