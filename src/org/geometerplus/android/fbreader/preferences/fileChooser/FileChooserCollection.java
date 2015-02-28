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

package org.geometerplus.android.fbreader.preferences.fileChooser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class FileChooserCollection {
	private final Context myContext;
	private final int myBaseRequestCode;
	private final List<FileChooserPreference> myPreferences = new ArrayList<FileChooserPreference>();

	public FileChooserCollection(Context context, int baseRequestCode) {
		myContext = context;
		myBaseRequestCode = baseRequestCode;
	}

	public FileChooserPreference createPreference(ZLResource rootResource, String resourceKey, ZLStringListOption option, Runnable onValueSetAction) {
		final FileChooserPreference preference = new FileChooserMultiPreference(
			myContext, rootResource, resourceKey, option, myBaseRequestCode + myPreferences.size(), onValueSetAction
		);
		myPreferences.add(preference);
		return preference;
	}

	public FileChooserPreference createPreference(ZLResource rootResource, String resourceKey, ZLStringOption option, Runnable onValueSetAction) {
		final FileChooserPreference preference = new FileChooserSinglePreference(
			myContext, rootResource, resourceKey, option, myBaseRequestCode + myPreferences.size(), onValueSetAction
		);
		myPreferences.add(preference);
		return preference;
	}

	public void update(int requestCode, Intent data) {
		try {
			myPreferences.get(requestCode - myBaseRequestCode).setValueFromIntent(data);
		} catch (Exception e) {
			// ignore
		}
	}
}
