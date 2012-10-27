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

import android.content.Intent;
import android.view.KeyEvent;

import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;

import org.geometerplus.zlibrary.text.view.style.*;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.DictionaryUtil;

public class PreferenceActivity extends ZLPreferenceActivity {
	public PreferenceActivity() {
		super("Preferences");
	}

	@Override
	protected void init(Intent intent) {
		setResult(FBReader.RESULT_REPAINT);

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		final ColorProfile profile = fbReader.getColorProfile();

		final Screen directoriesScreen = createPreferenceScreen("directories");
		directoriesScreen.addOption(Paths.BooksDirectoryOption(), "books");
		directoriesScreen.addOption(Paths.FontsDirectoryOption(), "fonts");
		directoriesScreen.addOption(Paths.WallpapersDirectoryOption(), "wallpapers");

		final Screen appearanceScreen = createPreferenceScreen("appearance");
		appearanceScreen.addPreference(new ZLStringChoicePreference(
			this, appearanceScreen.Resource, "screenOrientation",
			androidLibrary.OrientationOption, androidLibrary.allOrientations()
		));
		appearanceScreen.addPreference(new ZLBooleanPreference(
			this,
			fbReader.AllowScreenBrightnessAdjustmentOption,
			appearanceScreen.Resource,
			"allowScreenBrightnessAdjustment"
		) {
			private final int myLevel = androidLibrary.ScreenBrightnessLevelOption.getValue();

			@Override
			protected void onClick() {
				super.onClick();
				androidLibrary.ScreenBrightnessLevelOption.setValue(isChecked() ? myLevel : 0);
			}
		});
		appearanceScreen.addPreference(new BatteryLevelToTurnScreenOffPreference(
			this,
			androidLibrary.BatteryLevelToTurnScreenOffOption,
			appearanceScreen.Resource,
			"dontTurnScreenOff"
		));
		/*
		appearanceScreen.addPreference(new ZLBooleanPreference(
			this,
			androidLibrary.DontTurnScreenOffDuringChargingOption,
			appearanceScreen.Resource,
			"dontTurnScreenOffDuringCharging"
		));
		*/
		appearanceScreen.addOption(androidLibrary.ShowStatusBarOption, "showStatusBar");
		appearanceScreen.addOption(androidLibrary.DisableButtonLightsOption, "disableButtonLights");

		final Screen textScreen = createPreferenceScreen("text");

		final Screen fontPropertiesScreen = textScreen.createPreferenceScreen("fontProperties");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.AntiAliasOption, "antiAlias");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.DeviceKerningOption, "deviceKerning");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.DitheringOption, "dithering");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.SubpixelOption, "subpixel");

		final ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
		final ZLTextBaseStyle baseStyle = collection.getBaseStyle();
		textScreen.addPreference(new FontOption(
			this, textScreen.Resource, "font",
			baseStyle.FontFamilyOption, false
		));
		textScreen.addPreference(new ZLIntegerRangePreference(
			this, textScreen.Resource.getResource("fontSize"),
			baseStyle.FontSizeOption
		));
		textScreen.addPreference(new FontStylePreference(
			this, textScreen.Resource, "fontStyle",
			baseStyle.BoldOption, baseStyle.ItalicOption
		));
		final ZLIntegerRangeOption spaceOption = baseStyle.LineSpaceOption;
		final String[] spacings = new String[spaceOption.MaxValue - spaceOption.MinValue + 1];
		for (int i = 0; i < spacings.length; ++i) {
			final int val = spaceOption.MinValue + i;
			spacings[i] = (char)(val / 10 + '0') + "." + (char)(val % 10 + '0');
		}
		textScreen.addPreference(new ZLChoicePreference(
			this, textScreen.Resource, "lineSpacing",
			spaceOption, spacings
		));
		final String[] alignments = { "left", "right", "center", "justify" };
		textScreen.addPreference(new ZLChoicePreference(
			this, textScreen.Resource, "alignment",
			baseStyle.AlignmentOption, alignments
		));
		textScreen.addOption(baseStyle.AutoHyphenationOption, "autoHyphenations");

		final Screen moreStylesScreen = textScreen.createPreferenceScreen("more");

		byte styles[] = {
			FBTextKind.REGULAR,
			FBTextKind.TITLE,
			FBTextKind.SECTION_TITLE,
			FBTextKind.SUBTITLE,
			FBTextKind.H1,
			FBTextKind.H2,
			FBTextKind.H3,
			FBTextKind.H4,
			FBTextKind.H5,
			FBTextKind.H6,
			FBTextKind.ANNOTATION,
			FBTextKind.EPIGRAPH,
			FBTextKind.AUTHOR,
			FBTextKind.POEM_TITLE,
			FBTextKind.STANZA,
			FBTextKind.VERSE,
			FBTextKind.CITE,
			FBTextKind.INTERNAL_HYPERLINK,
			FBTextKind.EXTERNAL_HYPERLINK,
			FBTextKind.FOOTNOTE,
			FBTextKind.ITALIC,
			FBTextKind.EMPHASIS,
			FBTextKind.BOLD,
			FBTextKind.STRONG,
			FBTextKind.DEFINITION,
			FBTextKind.DEFINITION_DESCRIPTION,
			FBTextKind.PREFORMATTED,
			FBTextKind.CODE
		};
		for (int i = 0; i < styles.length; ++i) {
			final ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
			if (decoration == null) {
				continue;
			}
			ZLTextFullStyleDecoration fullDecoration =
				decoration instanceof ZLTextFullStyleDecoration ?
					(ZLTextFullStyleDecoration)decoration : null;

			final Screen formatScreen = moreStylesScreen.createPreferenceScreen(decoration.getName());
			formatScreen.addPreference(new FontOption(
				this, textScreen.Resource, "font",
				decoration.FontFamilyOption, true
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("fontSizeDifference"),
				decoration.FontSizeDeltaOption
			));
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "bold",
				decoration.BoldOption
			));
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "italic",
				decoration.ItalicOption
			));
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "underlined",
				decoration.UnderlineOption
			));
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "strikedThrough",
				decoration.StrikeThroughOption
			));
			if (fullDecoration != null) {
				final String[] allAlignments = { "unchanged", "left", "right", "center", "justify" };
				formatScreen.addPreference(new ZLChoicePreference(
					this, textScreen.Resource, "alignment",
					fullDecoration.AlignmentOption, allAlignments
				));
			}
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "allowHyphenations",
				decoration.AllowHyphenationsOption
			));
			if (fullDecoration != null) {
				formatScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("spaceBefore"),
					fullDecoration.SpaceBeforeOption
				));
				formatScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("spaceAfter"),
					fullDecoration.SpaceAfterOption
				));
				formatScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("leftIndent"),
					fullDecoration.LeftIndentOption
				));
				formatScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("rightIndent"),
					fullDecoration.RightIndentOption
				));
				formatScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("firstLineIndent"),
					fullDecoration.FirstLineIndentDeltaOption
				));
				final ZLIntegerOption spacePercentOption = fullDecoration.LineSpacePercentOption;
				final int[] spacingValues = new int[17];
				final String[] spacingKeys = new String[17];
				spacingValues[0] = -1;
				spacingKeys[0] = "unchanged";
				for (int j = 1; j < spacingValues.length; ++j) {
					final int val = 4 + j;
					spacingValues[j] = 10 * val;
					spacingKeys[j] = (char)(val / 10 + '0') + "." + (char)(val % 10 + '0');
				}
				formatScreen.addPreference(new ZLIntegerChoicePreference(
					this, textScreen.Resource, "lineSpacing",
					spacePercentOption, spacingValues, spacingKeys
				));
			}
		}

		final ZLPreferenceSet footerPreferences = new ZLPreferenceSet();
		final ZLPreferenceSet bgPreferences = new ZLPreferenceSet();

		final Screen cssScreen = createPreferenceScreen("css");
		cssScreen.addOption(collection.UseCSSFontSizeOption, "fontSize");
		cssScreen.addOption(collection.UseCSSTextAlignmentOption, "textAlignment");

		final Screen colorsScreen = createPreferenceScreen("colors");
		colorsScreen.addPreference(new WallpaperPreference(
			this, profile, colorsScreen.Resource, "background"
		) {
			@Override
			protected void onDialogClosed(boolean result) {
				super.onDialogClosed(result);
				bgPreferences.setEnabled("".equals(getValue()));
			}
		});
		bgPreferences.add(
			colorsScreen.addOption(profile.BackgroundOption, "backgroundColor")
		);
		bgPreferences.setEnabled("".equals(profile.WallpaperOption.getValue()));
		/*
		colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
		*/
		colorsScreen.addOption(profile.HighlightingOption, "highlighting");
		colorsScreen.addOption(profile.RegularTextOption, "text");
		colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
		colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
		colorsScreen.addOption(profile.FooterFillOption, "footer");
		colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
		colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");

		final Screen marginsScreen = createPreferenceScreen("margins");
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("left"),
			fbReader.LeftMarginOption
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("right"),
			fbReader.RightMarginOption
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("top"),
			fbReader.TopMarginOption
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("bottom"),
			fbReader.BottomMarginOption
		));

		final Screen statusLineScreen = createPreferenceScreen("scrollBar");

		final String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
		statusLineScreen.addPreference(new ZLChoicePreference(
			this, statusLineScreen.Resource, "scrollbarType",
			fbReader.ScrollbarTypeOption, scrollBarTypes
		) {
			@Override
			protected void onDialogClosed(boolean result) {
				super.onDialogClosed(result);
				footerPreferences.setEnabled(
					findIndexOfValue(getValue()) == FBView.SCROLLBAR_SHOW_AS_FOOTER
				);
			}
		});

		footerPreferences.add(statusLineScreen.addPreference(new ZLIntegerRangePreference(
			this, statusLineScreen.Resource.getResource("footerHeight"),
			fbReader.FooterHeightOption
		)));
		footerPreferences.add(statusLineScreen.addOption(profile.FooterFillOption, "footerColor"));
		footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowTOCMarksOption, "tocMarks"));

		footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowClockOption, "showClock"));
		footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowBatteryOption, "showBattery"));
		footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowProgressOption, "showProgress"));
		footerPreferences.add(statusLineScreen.addPreference(new FontOption(
			this, statusLineScreen.Resource, "font",
			fbReader.FooterFontOption, false
		)));
		footerPreferences.setEnabled(
			fbReader.ScrollbarTypeOption.getValue() == FBView.SCROLLBAR_SHOW_AS_FOOTER
		);

		/*
		final Screen colorProfileScreen = createPreferenceScreen("colorProfile");
		final ZLResource resource = colorProfileScreen.Resource;
		colorProfileScreen.setSummary(ColorProfilePreference.createTitle(resource, fbreader.getColorProfileName()));
		for (String key : ColorProfile.names()) {
			colorProfileScreen.addPreference(new ColorProfilePreference(
				this, fbreader, colorProfileScreen, key, ColorProfilePreference.createTitle(resource, key)
			));
		}
		*/

		final ScrollingPreferences scrollingPreferences = ScrollingPreferences.Instance();

		final ZLKeyBindings keyBindings = fbReader.keyBindings();

		final Screen scrollingScreen = createPreferenceScreen("scrolling");
		scrollingScreen.addOption(scrollingPreferences.FingerScrollingOption, "fingerScrolling");
		scrollingScreen.addOption(fbReader.EnableDoubleTapOption, "enableDoubleTapDetection");

		final ZLPreferenceSet volumeKeysPreferences = new ZLPreferenceSet();
		scrollingScreen.addPreference(new ZLCheckBoxPreference(
			this, scrollingScreen.Resource, "volumeKeys"
		) {
			{
				setChecked(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));
			}

			@Override
			protected void onClick() {
				super.onClick();
				if (isChecked()) {
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
				} else {
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, FBReaderApp.NoAction);
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, FBReaderApp.NoAction);
				}
				volumeKeysPreferences.setEnabled(isChecked());
			}
		});
		volumeKeysPreferences.add(scrollingScreen.addPreference(new ZLCheckBoxPreference(
			this, scrollingScreen.Resource, "invertVolumeKeys"
		) {
			{
				setChecked(ActionCode.VOLUME_KEY_SCROLL_FORWARD.equals(
					keyBindings.getBinding(KeyEvent.KEYCODE_VOLUME_UP, false)
				));
			}

			@Override
			protected void onClick() {
				super.onClick();
				if (isChecked()) {
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
				} else {
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
					keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
				}
			}
		}));
		volumeKeysPreferences.setEnabled(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));

		scrollingScreen.addOption(scrollingPreferences.AnimationOption, "animation");
		scrollingScreen.addPreference(new AnimationSpeedPreference(
			this,
			scrollingScreen.Resource,
			"animationSpeed",
			scrollingPreferences.AnimationSpeedOption
		));
		scrollingScreen.addOption(scrollingPreferences.HorizontalOption, "horizontal");

		final Screen dictionaryScreen = createPreferenceScreen("dictionary");
		dictionaryScreen.addPreference(new DictionaryPreference(
			this,
			dictionaryScreen.Resource,
			"dictionary",
			DictionaryUtil.singleWordTranslatorOption(),
			DictionaryUtil.dictionaryInfos(this, true)
		));
		dictionaryScreen.addPreference(new DictionaryPreference(
			this,
			dictionaryScreen.Resource,
			"translator",
			DictionaryUtil.multiWordTranslatorOption(),
			DictionaryUtil.dictionaryInfos(this, false)
		));
		dictionaryScreen.addPreference(new ZLBooleanPreference(
			this,
			fbReader.NavigateAllWordsOption,
			dictionaryScreen.Resource,
			"navigateOverAllWords"
		));
		dictionaryScreen.addOption(fbReader.WordTappingActionOption, "tappingAction");

		final Screen imagesScreen = createPreferenceScreen("images");
		imagesScreen.addOption(fbReader.ImageTappingActionOption, "tappingAction");
		imagesScreen.addOption(fbReader.FitImagesToScreenOption, "fitImagesToScreen");
		imagesScreen.addOption(fbReader.ImageViewBackgroundOption, "backgroundColor");

		final Screen cancelMenuScreen = createPreferenceScreen("cancelMenu");
		cancelMenuScreen.addOption(fbReader.ShowLibraryInCancelMenuOption, "library");
		cancelMenuScreen.addOption(fbReader.ShowNetworkLibraryInCancelMenuOption, "networkLibrary");
		cancelMenuScreen.addOption(fbReader.ShowPreviousBookInCancelMenuOption, "previousBook");
		cancelMenuScreen.addOption(fbReader.ShowPositionsInCancelMenuOption, "positions");
		final String[] backKeyActions =
			{ ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU };
		cancelMenuScreen.addPreference(new ZLStringChoicePreference(
			this, cancelMenuScreen.Resource, "backKeyAction",
			keyBindings.getOption(KeyEvent.KEYCODE_BACK, false), backKeyActions
		));
		final String[] backKeyLongPressActions =
			{ ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU, FBReaderApp.NoAction };
		cancelMenuScreen.addPreference(new ZLStringChoicePreference(
			this, cancelMenuScreen.Resource, "backKeyLongPressAction",
			keyBindings.getOption(KeyEvent.KEYCODE_BACK, true), backKeyLongPressActions
		));

		final Screen tipsScreen = createPreferenceScreen("tips");
		tipsScreen.addOption(TipsManager.Instance().ShowTipsOption, "showTips");

		final Screen aboutScreen = createPreferenceScreen("about");
		aboutScreen.addPreference(new InfoPreference(
			this,
			aboutScreen.Resource.getResource("version").getValue(),
			androidLibrary.getFullVersionName()
		));
		aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "site"));
		aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "email"));
		aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "twitter"));
	}
}
