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

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;

import org.geometerplus.android.fbreader.MenuData;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class MenuPreference extends Preference {
	
	public static final String ENABLED_MENU_IDS_KEY = "enabledMenuIds";
	public static final String DISABLED_MENU_IDS_KEY = "disabledMenuIds";
	
	private Activity myActivity;

	MenuPreference(Activity a) {
		super(a);
		myActivity = a;

		setTitle(ZLResource.resource("Preferences").getResource("menu").getValue());
		setSummary(ZLResource.resource("Preferences").getResource("menu").getResource("summary").getValue());
	}
	
	@Override
	protected void onClick() {
		Intent intent = new Intent(myActivity, MenuConfigurationActivity.class);
		intent.putStringArrayListExtra(ENABLED_MENU_IDS_KEY, MenuData.enabledCodes());
		intent.putStringArrayListExtra(DISABLED_MENU_IDS_KEY, MenuData.disabledCodes());

		myActivity.startActivityForResult(intent, PreferenceActivity.MENU_REQUEST_CODE);
	}

	public void update(Intent data) {
		List<String> eIds = data.getStringArrayListExtra(ENABLED_MENU_IDS_KEY);
		List<String> dIds = data.getStringArrayListExtra(DISABLED_MENU_IDS_KEY);
		int i = 0;
		for (String s : eIds) {
			MenuData.nodeOption(s).setValue(i);
			++i;
		}
		for (String s : dIds) {
			MenuData.nodeOption(s).setValue(-1);
		}
	}

}
