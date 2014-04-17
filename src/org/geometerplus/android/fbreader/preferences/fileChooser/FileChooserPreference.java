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

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.util.FileChooserUtil;

abstract class FileChooserPreference extends Preference {
	private final int myRegCode;
	private final ZLResource myResource;
	private final boolean myChooseWritableDirectoriesOnly;

	FileChooserPreference(Context context, ZLResource rootResource, String resourceKey, boolean chooseWritableDirectoriesOnly, int regCode) {
		super(context);

		myRegCode = regCode;
		myChooseWritableDirectoriesOnly = chooseWritableDirectoriesOnly;
		myResource = rootResource.getResource(resourceKey);
		setTitle(myResource.getValue());
	}

	@Override
	protected void onClick() {
		FileChooserUtil.runDirectoryChooser(
			(Activity)getContext(),
			myRegCode,
			myResource.getResource("chooserTitle").getValue(),
			getStringValue(),
			myChooseWritableDirectoriesOnly
		);
	}

	protected abstract String getStringValue();
	protected abstract void setValue(String value);
}
