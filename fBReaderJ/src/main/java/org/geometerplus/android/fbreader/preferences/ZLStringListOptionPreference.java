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

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.Context;

class ZLStringListOptionPreference extends ZLStringPreference {
	private final ZLStringListOption myOption;

	ZLStringListOptionPreference(Context context, ZLStringListOption option, ZLResource rootResource, String resourceKey) {
		super(context, rootResource, resourceKey);
		myOption = option;
		final List<String> optionValues = myOption.getValue();
		super.setValue(optionValues.isEmpty() ? "" : optionValues.get(0));
	}

	@Override
	protected void setValue(String value) {
		super.setValue(value);
		final List<String> optionValues = new ArrayList<String>(myOption.getValue());
		if (optionValues.isEmpty()) {
			optionValues.add(value);
		} else {
			optionValues.set(0, value);
		}
		myOption.setValue(optionValues);
	}
}
