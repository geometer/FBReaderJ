/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

import android.os.Bundle;
import android.preference.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class ZLPreferenceActivity extends android.preference.PreferenceActivity {
	private final ArrayList<ZLPreference> myPreferences = new ArrayList<ZLPreference>();

	protected class Category {
		private final ZLResource myResource;
		private final PreferenceGroup myGroup;

		Category(String resourceKey) {
			if (resourceKey != null) {
				myResource = ZLPreferenceActivity.this.myResource.getResource(resourceKey);
				myGroup = new PreferenceCategory(ZLPreferenceActivity.this);
				myGroup.setTitle(myResource.getValue());
				myScreen.addPreference(myGroup);
			} else {
				myResource = ZLPreferenceActivity.this.myResource;
				myGroup = myScreen;
			}
		}

		ZLResource getResource() {
			return myResource;
		}

		void addPreference(ZLStringPreference preference) {
			myGroup.addPreference(preference);
			myPreferences.add(preference);
		}

		void addPreference(ZLStringListPreference preference) {
			myGroup.addPreference(preference);
			myPreferences.add(preference);
		}

		void addOption(ZLBooleanOption option, String resourceKey) {
			ZLBooleanPreference preference =
				new ZLBooleanPreference(ZLPreferenceActivity.this, option, myResource, resourceKey);
			myGroup.addPreference(preference);
			myPreferences.add(preference);
		}
	}

	private PreferenceScreen myScreen;
	private final ZLResource myResource;

	ZLPreferenceActivity(String resourceKey) {
		myResource = ZLResource.resource("dialog").getResource(resourceKey);
	}

	protected abstract void init();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		myScreen = getPreferenceManager().createPreferenceScreen(this);

		init();

		setPreferenceScreen(myScreen);
	}

	@Override
	protected void onPause() {
		for (ZLPreference preference : myPreferences) {
			preference.accept();
		}
		super.onPause();
	}
}
