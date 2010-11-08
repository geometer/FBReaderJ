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

import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionsDialog;
import org.geometerplus.fbreader.optionsDialog.OptionsDialog;

import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.Paths;

public class PreferenceActivity extends ZLPreferenceActivity {
	public PreferenceActivity() {
		super("Preferences");
	}

	/*private static final class ColorProfilePreference extends ZLSimplePreference {
		private final FBReaderApp myFBReader;
		private final Screen myScreen;
		private final String myKey;

		static final String createTitle(ZLResource resource, String resourceKey) {
			final ZLResource r = resource.getResource(resourceKey);
			return r.hasValue() ? r.getValue() : resourceKey;
		}

		ColorProfilePreference(Context context, FBReaderApp fbreader, Screen screen, String key, String title) {
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
			myFBReaderApp.setColorProfileName(myKey);
			myScreen.close();
		}
	}*/

	@Override
	protected void init() {
		final Category libraryCategory = createCategory("Library");
		libraryCategory.addPreference(new ZLStringOptionPreference(
			this,
			Paths.BooksDirectoryOption,
			libraryCategory.Resource,
			"path")
		);
		final Category interactionCategory = createCategory("interaction");
		final Screen keyBindingsScreen = interactionCategory.createPreferenceScreen("keyBindings");
		keyBindingsScreen.setSummary(keyBindingsScreen.Resource.getResource("summary").getValue());
		final Category keyBindingCategory = keyBindingsScreen.createCategory(null);

		ZLResource keysResource = ZLResource.resource("keys");
		ZLResource actionsResource = ZLResource.resource("actions");
		String[] actions = ZLApplication.Instance().getGetSimpleActions();
		int[] keys = {
				KeyEvent.KEYCODE_VOLUME_DOWN,
				KeyEvent.KEYCODE_VOLUME_UP,
				KeyEvent.KEYCODE_DPAD_CENTER,
				KeyEvent.KEYCODE_DPAD_UP,
				KeyEvent.KEYCODE_DPAD_DOWN,
				KeyEvent.KEYCODE_DPAD_RIGHT,
				KeyEvent.KEYCODE_DPAD_LEFT,
				KeyEvent.KEYCODE_BACK,
				KeyEvent.KEYCODE_CAMERA,
		};

		for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
			String key = ZLAndroidKeyUtil.getKeyNameByCode(keys[keyIndex]);
			keyBindingCategory.addPreference(new StringListPreference(
				this, keysResource.getResource(key), actionsResource,
				actions, ZLApplication.Instance().getBindingOption(keys[keyIndex])));
		}

		final Category lookNFeelCategory = createCategory("LookNFeel");

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ZLAndroidApplication androidApp = ZLAndroidApplication.Instance();

		final Screen appearanceScreen = lookNFeelCategory.createPreferenceScreen("appearanceSettings");
		appearanceScreen.setSummary(appearanceScreen.Resource.getResource("summary").getValue());
		final Category appearanceCategory = appearanceScreen.createCategory(null);
		final ZLOptionsDialog dlg = new OptionsDialog(fbReader).getDialog();
		final Screen marginsScreen = appearanceCategory.createPreferenceScreen("margins");
		final Category marginsCategory = marginsScreen.createCategory(null);
		marginsCategory.addPreference(new ZLIntegerRangePreference(
			this, marginsCategory.Resource.getResource("left"),
			fbReader.LeftMarginOption)
		);
		marginsCategory.addPreference(new ZLIntegerRangePreference(
			this, marginsCategory.Resource.getResource("right"),
			fbReader.RightMarginOption)
		);
		marginsCategory.addPreference(new ZLIntegerRangePreference(
			this, marginsCategory.Resource.getResource("top"),
			fbReader.TopMarginOption)
		);
		marginsCategory.addPreference(new ZLIntegerRangePreference(
			this, marginsCategory.Resource.getResource("bottom"),
			fbReader.BottomMarginOption)
		);
		final Screen formatScreen = appearanceCategory.createPreferenceScreen("format");
		final Screen stylesScreen = appearanceCategory.createPreferenceScreen("styles");
		final Screen colorsScreen = appearanceCategory.createPreferenceScreen("colors");
		formatScreen.setOnPreferenceClickListener(
				new PreferenceScreen.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						dlg.run(0);
						return true;
					}
				}
		);
		stylesScreen.setOnPreferenceClickListener(
				new PreferenceScreen.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						dlg.run(1);
						return true;
					}
				}
		);
		colorsScreen.setOnPreferenceClickListener(
				new PreferenceScreen.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						dlg.run(2);
						return true;
					}
				}
		);

		final Screen statusLineScreen = lookNFeelCategory.createPreferenceScreen("scrollBar");
		statusLineScreen.setSummary(statusLineScreen.Resource.getResource("summary").getValue());
		final Category statusLineCategory = statusLineScreen.createCategory(null);

		String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
		statusLineCategory.addPreference(new ZLChoicePreference(
			this, statusLineCategory.Resource, "scrollbarType",
			fbReader.ScrollbarTypeOption, scrollBarTypes)
		);

		statusLineCategory.addPreference(new ZLIntegerRangePreference(
			this, statusLineCategory.Resource.getResource("footerHeight"),
			fbReader.FooterHeightOption)
		);

		String[] footerLongTaps = {"longTapRevert", "longTapNavigate"};
		statusLineCategory.addPreference(new ZLChoicePreference(
			this, statusLineCategory.Resource, "footerLongTap",
			fbReader.FooterLongTapOption, footerLongTaps
		));

		statusLineCategory.addOption(fbReader.FooterShowClockOption, "showClock");
		statusLineCategory.addOption(fbReader.FooterShowBatteryOption, "showBattery");
		statusLineCategory.addOption(fbReader.FooterShowProgressOption, "showProgress");
		statusLineCategory.addOption(fbReader.FooterIsSensitiveOption, "isSensitive");
		statusLineCategory.addPreference(new FontOption(
			this, statusLineCategory.Resource, "font",
			fbReader.FooterFontOption)
		);

		lookNFeelCategory.addOption(androidApp.AutoOrientationOption, "autoOrientation");
		if (!androidApp.isAlwaysShowStatusBar()) {
			lookNFeelCategory.addOption(androidApp.ShowStatusBarOption, "showStatusBar");
		}
		lookNFeelCategory.addPreference(new BatteryLevelToTurnScreenOffPreference(
			this,
			androidApp.BatteryLevelToTurnScreenOffOption,
			lookNFeelCategory.Resource,
			"dontTurnScreenOff"
		));
		lookNFeelCategory.addPreference(new ZLBooleanPreference(
			this,
			fbReader.AllowScreenBrightnessAdjustmentOption,
			lookNFeelCategory.Resource,
			"allowScreenBrightnessAdjustment"
		) {
			public void onAccept() {
				super.onAccept();
				if (!isChecked()) {
					androidApp.ScreenBrightnessLevelOption.setValue(0);
				}
			}
		});

		/*
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
		scrollingCategory.addOption(scrollingPreferences.AnimateOption, "animated");
		scrollingCategory.addOption(scrollingPreferences.HorizontalOption, "horizontal");
	}
}
