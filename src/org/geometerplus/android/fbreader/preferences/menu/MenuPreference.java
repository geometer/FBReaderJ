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

package org.geometerplus.android.fbreader.preferences.menu;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;

import org.geometerplus.android.fbreader.MenuData;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class MenuPreference extends Preference {
	private final int myRequestCode;

	public MenuPreference(Activity activity, ZLResource resource, int requestCode) {
		super(activity);

		setTitle(resource.getValue());
		setSummary(resource.getResource("summary").getValue());

		myRequestCode = requestCode;
	}

	@Override
	protected void onClick() {
		((Activity)getContext()).startActivityForResult(
			new Intent(getContext(), ConfigurationActivity.class)
				.putStringArrayListExtra(ConfigurationActivity.ENABLED_MENU_IDS_KEY, MenuData.enabledCodes())
				.putStringArrayListExtra(ConfigurationActivity.DISABLED_MENU_IDS_KEY, MenuData.disabledCodes()),
			myRequestCode
		);
	}

	public void update(Intent data) {
		int i = 0;
		for (String s : data.getStringArrayListExtra(ConfigurationActivity.ENABLED_MENU_IDS_KEY)) {
			MenuData.nodeOption(s).setValue(i);
			++i;
		}
		for (String s : data.getStringArrayListExtra(ConfigurationActivity.DISABLED_MENU_IDS_KEY)) {
			MenuData.nodeOption(s).setValue(-1);
		}
	}
}
