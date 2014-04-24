/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences.fileChooser;

import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class FileChooserStringPreference extends FileChooserPreference {
	private final ZLStringOption myOption;

	FileChooserStringPreference(Context context, ZLResource rootResource, String resourceKey, ZLStringOption option, int regCode, Runnable onValueSetAction) {
		super(context, rootResource, resourceKey, true, regCode, onValueSetAction);
		myOption = option;

		setSummary(getStringValue());
	}

	@Override
	protected String getStringValue() {
		return myOption.getValue();
	}

	@Override
	protected void setValueInternal(String value) {
		final String currentValue = myOption.getValue();
		if (!currentValue.equals(value)) {
			myOption.setValue(value);
			setSummary(value);
		}
	}
}
