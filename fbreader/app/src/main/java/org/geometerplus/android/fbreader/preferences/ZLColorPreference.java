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

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

class ZLColorPreference extends ColorPreference {
	private final ZLColorOption myOption;
	private final String myTitle;

	ZLColorPreference(Context context, ZLResource resource, String resourceKey, ZLColorOption option) {
		super(context);
		myOption = option;
		setWidgetLayoutResource(R.layout.color_preference);

		myTitle = resource.getResource(resourceKey).getValue();
	}

	@Override
	public String getTitle() {
		return myTitle;
	}

	@Override
	protected ZLColor getSavedColor() {
		return myOption.getValue();
	}

	@Override
	protected void saveColor(ZLColor color) {
		myOption.setValue(color);
	}
}
