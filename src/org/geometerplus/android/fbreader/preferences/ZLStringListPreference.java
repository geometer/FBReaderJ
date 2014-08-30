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
import android.preference.ListPreference;

import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class ZLStringListPreference extends ListPreference {
	protected final ZLResource myValuesResource;

	ZLStringListPreference(Context context, ZLResource resource) {
		this(context, resource, resource);
	}

	ZLStringListPreference(Context context, ZLResource resource, ZLResource valuesResource) {
		super(context);
		setTitle(resource.getValue());
		myValuesResource = valuesResource;

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	protected final void setList(String[] values) {
		String[] texts = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			final ZLResource resource = myValuesResource.getResource(values[i]);
			texts[i] = resource.hasValue() ? resource.getValue() : values[i];
		}
		setLists(values, texts);
	}

	protected final void setLists(String[] values, String[] texts) {
		assert(values.length == texts.length);
		setEntries(texts);
		setEntryValues(values);
	}

	protected final boolean setInitialValue(String value) {
		int index = 0;
		boolean found = false;
		final CharSequence[] entryValues = getEntryValues();
		if (value != null) {
			for (int i = 0; i < entryValues.length; ++i) {
				if (value.equals(entryValues[i])) {
					index = i;
					found = true;
					break;
				}
			}
		}
		setValueIndex(index);
		setSummary(getEntry());
		return found;
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			setSummary(getEntry());
		}
	}
}
