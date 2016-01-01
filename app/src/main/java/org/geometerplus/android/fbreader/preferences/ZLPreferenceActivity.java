/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.HashMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.android.util.OrientationUtil;

abstract class ZLPreferenceActivity extends android.preference.PreferenceActivity {
	public static String SCREEN_KEY = "screen";

	private final HashMap<String,Screen> myScreenMap = new HashMap<String,Screen>();

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

		public Preference addPreference(Preference preference) {
			myScreen.addPreference(preference);
			return preference;
		}

		public Preference addOption(ZLBooleanOption option, String resourceKey) {
			return addPreference(new ZLBooleanPreference(
				ZLPreferenceActivity.this, option, Resource.getResource(resourceKey)
			));
		}

		public Preference addOption(ZLColorOption option, String resourceKey) {
			return addPreference(new ZLColorPreference(
				ZLPreferenceActivity.this, Resource, resourceKey, option
			));
		}

		public Preference addOption(ZLIntegerRangeOption option, String resourceKey) {
			return addPreference(new ZLIntegerRangePreference(
				ZLPreferenceActivity.this, Resource.getResource(resourceKey), option
			));
		}

		public <T extends Enum<T>> Preference addOption(ZLEnumOption<T> option, String key) {
			return addPreference(
				new ZLEnumPreference<T>(ZLPreferenceActivity.this, option, Resource.getResource(key))
			);
		}

		public <T extends Enum<T>> Preference addOption(ZLEnumOption<T> option, String key, String valuesKey) {
			return addPreference(
				new ZLEnumPreference<T>(ZLPreferenceActivity.this, option, Resource.getResource(key), Resource.getResource(valuesKey))
			);
		}
	}

	private PreferenceScreen myScreen;
	final ZLResource Resource;

	ZLPreferenceActivity(String resourceKey) {
		Resource = ZLResource.resource(resourceKey);
	}

	Screen createPreferenceScreen(String resourceKey) {
		final Screen screen = new Screen(Resource, resourceKey);
		myScreenMap.put(resourceKey, screen);
		myScreen.addPreference(screen.myScreen);
		return screen;
	}

	public Preference addPreference(Preference preference) {
		myScreen.addPreference(preference);
		return preference;
	}

	public Preference addOption(ZLBooleanOption option, String resourceKey) {
		ZLBooleanPreference preference =
			new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource.getResource(resourceKey));
		myScreen.addPreference(preference);
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

		SQLiteCookieDatabase.init(this);

		myScreen = getPreferenceManager().createPreferenceScreen(this);

		final Intent intent = getIntent();
		final Uri data = intent.getData();
		final String screenId;
		if (Intent.ACTION_VIEW.equals(intent.getAction())
				&& data != null && "fbreader-preferences".equals(data.getScheme())) {
			screenId = data.getEncodedSchemeSpecificPart();
		} else {
			screenId = intent.getStringExtra(SCREEN_KEY);
		}

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				init(intent);
				final Screen screen = myScreenMap.get(screenId);
				setPreferenceScreen(screen != null ? screen.myScreen : myScreen);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}
}
