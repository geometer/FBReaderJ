/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

import android.content.*;
import android.preference.Preference;

import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLDirectoryPreference extends Preference {
	private String myOptionName;

	ZLDirectoryPreference(Context context, String optionName, ZLResource rootResource, String resourceKey) {
		super(context);
		myOptionName = optionName;

		ZLResource resource = rootResource.getResource(resourceKey);
		setTitle(resource.getValue());
	}

	@Override
	protected void onClick() {
		final Intent intent = new Intent();
		intent.setClass(getContext(), EditableStringListActivity.class);
		intent.putExtra(EditableStringListActivity.OPTION_NAME, myOptionName);
		intent.putExtra(EditableStringListActivity.TITLE, getTitle());

		getContext().startActivity(intent);
	}

}
