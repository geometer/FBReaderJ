/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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
import android.content.Intent;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class ZLPreferenceActivity extends android.preference.PreferenceActivity {
	private final ArrayList<ZLPreference> myPreferences = new ArrayList<ZLPreference>();

	protected class Screen {
		public final ZLResource Resource;
		private final PreferenceScreen myScreen;

		private Screen(ZLResource root, String resourceKey) {
			Resource = root.getResource(resourceKey);
			myScreen = getPreferenceManager().createPreferenceScreen(ZLPreferenceActivity.this);
			myScreen.setTitle(Resource.getValue());
			myScreen.setSummary(Resource.getResource("summary").getValue());
		}

		public void setSummary(CharSequence summary) {
			myScreen.setSummary(summary);
		}

		public Screen createPreferenceScreen(String resourceKey) {
			Screen screen = new Screen(Resource, resourceKey);
			myScreen.addPreference(screen.myScreen);
			return screen;
		}

		public ZLPreference addPreference(ZLPreference preference) {
			myScreen.addPreference((Preference)preference);
			myPreferences.add(preference);
			return preference;
		}

		public ZLPreference addOption(ZLBooleanOption option, String resourceKey) {
			ZLBooleanPreference preference =
				new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey);
			myScreen.addPreference(preference);
			myPreferences.add(preference);
			return preference;
		}

		public void close() {
			myScreen.getDialog().dismiss();
			ZLPreferenceActivity.this.getListView().invalidateViews();
		}

		public void setOnPreferenceClickListener(PreferenceScreen.OnPreferenceClickListener onPreferenceClickListener) {
			myScreen.setOnPreferenceClickListener(onPreferenceClickListener);
		}
	}

	private PreferenceScreen myScreen;
	final ZLResource Resource;

	ZLPreferenceActivity(String resourceKey) {
		Resource = ZLResource.resource("dialog").getResource(resourceKey);
	}

	Screen createPreferenceScreen(String resourceKey) {
		Screen screen = new Screen(Resource, resourceKey);
		myScreen.addPreference(screen.myScreen);
		return screen;
	}

	public ZLPreference addPreference(ZLPreference preference) {
		myScreen.addPreference((Preference)preference);
		myPreferences.add(preference);
		return preference;
	}

	public ZLPreference addOption(ZLBooleanOption option, String resourceKey) {
		ZLBooleanPreference preference =
			new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey);
		myScreen.addPreference(preference);
		myPreferences.add(preference);
		return preference;
	}

	/*
	protected Category createCategory() {
		return new CategoryImpl(myScreen, Resource);
	}
	*/

	protected abstract void init(Intent intent);

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		myScreen = getPreferenceManager().createPreferenceScreen(this);

		init(getIntent());

		setPreferenceScreen(myScreen);
	}

	@Override
	protected void onPause() {
		for (ZLPreference preference : myPreferences) {
			preference.onAccept();
		}
		super.onPause();
	}
}
