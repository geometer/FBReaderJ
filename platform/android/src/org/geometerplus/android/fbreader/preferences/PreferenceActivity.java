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
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.fbreader.fbreader.ScrollingPreferences;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	private final ZLResource myResource = ZLResource.resource("dialog").getResource("Preferences");
	private final ArrayList<ZLPreference> myPreferences = new ArrayList<ZLPreference>();

	private class Category {
		private final ZLResource myResource;
		private final PreferenceCategory myCategory;

		Category(String resourceKey) {
			myResource = PreferenceActivity.this.myResource.getResource(resourceKey);
			myCategory = new PreferenceCategory(PreferenceActivity.this);
			myCategory.setTitle(myResource.getValue());
			myScreen.addPreference(myCategory);
		}

		void addOption(ZLBooleanOption option, String resourceKey) {
			ZLBooleanPreference preference =
				new ZLBooleanPreference(PreferenceActivity.this, option, myResource, resourceKey);
			myCategory.addPreference(preference);
			myPreferences.add(preference);
		}
	}

	private PreferenceScreen myScreen;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		myScreen = getPreferenceManager().createPreferenceScreen(this);

		final Category lookNFeelCategory = new Category("LookNFeel");
		lookNFeelCategory.addOption(ZLAndroidApplication.Instance().AutoOrientationOption, "autoOrientation");
		lookNFeelCategory.addOption(ZLAndroidApplication.Instance().ShowStatusBarOption, "showStatusBar");

		final Category scrollingCategory = new Category("Scrolling");
		final ScrollingPreferences scrollingPreferences = ScrollingPreferences.Instance();
		scrollingCategory.addOption(scrollingPreferences.FlickOption, "flick");
		scrollingCategory.addOption(scrollingPreferences.VolumeKeysOption, "volumeKeys");
		scrollingCategory.addOption(scrollingPreferences.AnimateOption, "animated");
		scrollingCategory.addOption(scrollingPreferences.HorizontalOption, "horizontal");

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
