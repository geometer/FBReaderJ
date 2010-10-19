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

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.Paths;

public class PreferenceActivity extends ZLPreferenceActivity {
	public PreferenceActivity() {
		super("Preferences");
	}

	/*private static final class ColorProfilePreference extends ZLSimplePreference {
		private final FBReader myFBReader;
		private final Screen myScreen;
		private final String myKey;

		static final String createTitle(ZLResource resource, String resourceKey) {
			final ZLResource r = resource.getResource(resourceKey);
			return r.hasValue() ? r.getValue() : resourceKey;
		}

		ColorProfilePreference(Context context, FBReader fbreader, Screen screen, String key, String title) {
			super(context);
			myFBReader = fbreader;
			myScreen = screen;
			myKey = key;
			setTitle(title);
		}

		@Override
		public void onAccept() {
		}

		@Override
		public void onClick() {
			myScreen.setSummary(getTitle());
			myFBReader.setColorProfileName(myKey);
			myScreen.close();
		}
	}*/

	@Override
	protected void init() {
		final Category libraryCategory = createCategory("Library");
		/*
		libraryCategory.addPreference(new InfoPreference(
			this,
			libraryCategory.Resource.getResource("path").getValue(),
			Constants.BOOKS_DIRECTORY)
		);
		*/
		libraryCategory.addPreference(new ZLStringOptionPreference(
			this,
			Paths.BooksDirectoryOption,
			libraryCategory.Resource,
			"path")
		);
		final Category lookNFeelCategory = createCategory("LookNFeel");

		final Screen appearanceScreen = lookNFeelCategory.createPreferenceScreen("appearanceSettings");
		appearanceScreen.setSummary( appearanceScreen.Resource.getResource("summary").getValue() );
		appearanceScreen.setOnPreferenceClickListener(
				new PreferenceScreen.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						((FBReader) FBReader.Instance()).showOptionsDialog();
						return true;
					}
				}
		);

		final Screen statusLineScreen = lookNFeelCategory.createPreferenceScreen("scrollBar");
		statusLineScreen.setSummary(statusLineScreen.Resource.getResource("summary").getValue());
		final Category statusLineCategory = statusLineScreen.createCategory(null);

		String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
		statusLineCategory.addPreference(new StringListPreference(
			this, statusLineCategory.Resource, "scrollbarType",
			scrollBarTypes, ((FBReader)FBReader.Instance()).ScrollbarTypeOption));

		statusLineCategory.addPreference(new ZLIntegerRangePreference(
			this, statusLineCategory.Resource.getResource("footerHeight"),
			((FBReader)FBReader.Instance()).FooterHeightOption)
		);

		String[] footerLongTaps = {"longTapRevert", "longTapNavigate"};
		statusLineCategory.addPreference(new StringListPreference(
			this, statusLineCategory.Resource, "footerLongTap",
			footerLongTaps, ((FBReader)FBReader.Instance()).FooterLongTap));

		statusLineCategory.addOption(ZLAndroidApplication.Instance().FooterShowClock, "showClock");
		statusLineCategory.addOption(ZLAndroidApplication.Instance().FooterShowBattery, "showBattery");
		statusLineCategory.addOption(ZLAndroidApplication.Instance().FooterShowProgress, "showProgress");

		lookNFeelCategory.addOption(ZLAndroidApplication.Instance().AutoOrientationOption, "autoOrientation");
		if (!ZLAndroidApplication.Instance().isAlwaysShowStatusBar()) {
			lookNFeelCategory.addOption(ZLAndroidApplication.Instance().ShowStatusBarOption, "showStatusBar");
		}
		lookNFeelCategory.addOption(ZLAndroidApplication.Instance().DontTurnScreenOffOption, "dontTurnScreenOff");

		/*
		final FBReader fbreader = (FBReader)FBReader.Instance();
		final Screen colorProfileScreen = lookNFeelCategory.createPreferenceScreen("colorProfile");
		final Category colorProfileCategory = colorProfileScreen.createCategory(null);
		final ZLResource resource = colorProfileCategory.Resource;
		colorProfileScreen.setSummary(ColorProfilePreference.createTitle(resource, fbreader.getColorProfileName()));
		for (String key : ColorProfile.names()) {
			colorProfileCategory.addPreference(new ColorProfilePreference(
				this, fbreader, colorProfileScreen, key, ColorProfilePreference.createTitle(resource, key)
			));
		}
		*/

		final Category scrollingCategory = createCategory("Scrolling");
		final ScrollingPreferences scrollingPreferences = ScrollingPreferences.Instance();
		scrollingCategory.addOption(scrollingPreferences.FlickOption, "flick");
		scrollingCategory.addOption(scrollingPreferences.VolumeKeysOption, "volumeKeys");
		scrollingCategory.addOption(scrollingPreferences.InvertVolumeKeysOption, "invertVolumeKeys");
		scrollingCategory.addOption(scrollingPreferences.AnimateOption, "animated");
		scrollingCategory.addOption(scrollingPreferences.HorizontalOption, "horizontal");
	}
}

class StringListPreference extends ZLStringListPreference {
	private ZLOption myOption;

	StringListPreference(Context context, ZLResource resource, String resourceKey, String[] codes, ZLOption option) {
		super(context, resource, resourceKey);
		myCodes = codes;
		myOption = option;

		setList(codes);

		if (myOption instanceof ZLIntegerRangeOption) {
			setInitialValue(codes[
				Math.max(0, Math.min(myCodes.length - 1, ((ZLIntegerRangeOption)myOption).getValue()))
			]);
		}

		if (myOption instanceof ZLStringOption) {
			String initVal = ((ZLStringOption)myOption).getValue();
			for (int i = 0; i < codes.length; ++i) {
				if (codes[i].equals(initVal)){
					setInitialValue(codes[i]);
					break;
				}
			}
		}
	}

	public void onAccept() {
		if (myOption instanceof ZLIntegerRangeOption) {
			((ZLIntegerRangeOption)myOption).setValue(findIndexOfValue(getValue()));
		} else if (myOption instanceof ZLStringOption) {
			((ZLStringOption)myOption).setValue(getValue);
		}
	}
}
