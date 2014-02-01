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

import java.util.*;

import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.fbreader.WallpapersUtil;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;

class WallpaperPreference extends ZLStringListPreference {
	private final ZLStringOption myOption;

	WallpaperPreference(Context context, ColorProfile profile, ZLResource resource, String resourceKey) {
		super(context, resource, resourceKey);

		myOption = profile.WallpaperOption;
		final List<ZLFile> predefined = WallpapersUtil.predefinedWallpaperFiles();
		final List<ZLFile> external = WallpapersUtil.externalWallpaperFiles();

		final int size = 1 + predefined.size() + external.size();
		final String[] values = new String[size];
		final String[] texts = new String[size];

		final ZLResource optionResource = resource.getResource(resourceKey);
		values[0] = "";
		texts[0] = optionResource.getResource("solidColor").getValue();
		int index = 1;
		for (ZLFile f : predefined) {
			values[index] = f.getPath();
			final String name = f.getShortName();
			texts[index] = optionResource.getResource(
				name.substring(0, name.indexOf("."))
			).getValue();
			++index;
		}
		for (ZLFile f : external) {
			values[index] = f.getPath();
			texts[index] = f.getShortName();
			++index;
		}
		setLists(values, texts);

		setInitialValue(myOption.getValue());
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		myOption.setValue(getValue());
	}
}
