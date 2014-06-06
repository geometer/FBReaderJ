/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import java.text.DecimalFormatSymbols;
import java.util.*;

import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.text.view.style.*;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.fbreader.options.*;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.DictionaryUtil;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.preferences.fileChooser.FileChooserCollection;

import org.geometerplus.android.util.DeviceType;

public class PreferenceActivity extends ZLPreferenceActivity {
	private final FileChooserCollection myChooserCollection = new FileChooserCollection(this);

	public PreferenceActivity() {
		super("Preferences");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			myChooserCollection.update(requestCode, data);
		}
	}

	@Override
	protected void init(Intent intent) {
		final Config config = Config.Instance();
		config.requestAllValuesForGroup("Style");
		config.requestAllValuesForGroup("Options");
		config.requestAllValuesForGroup("LookNFeel");
		config.requestAllValuesForGroup("Fonts");
		config.requestAllValuesForGroup("Files");
		config.requestAllValuesForGroup("Scrolling");
		config.requestAllValuesForGroup("Colors");
		setResult(FBReader.RESULT_REPAINT);

		final ViewOptions viewOptions = new ViewOptions();
		final MiscOptions miscOptions = new MiscOptions();
		final FooterOptions footerOptions = viewOptions.getFooterOptions();
		final PageTurningOptions pageTurningOptions = new PageTurningOptions();
		final ImageOptions imageOptions = new ImageOptions();
		final ColorProfile profile = viewOptions.getColorProfile();
		final ZLTextStyleCollection collection = viewOptions.getTextStyleCollection();
		final ZLKeyBindings keyBindings = new ZLKeyBindings();

		final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		// TODO: use user-defined locale, not the default one,
		// or set user-defined locale as default
		final String decimalSeparator =
			String.valueOf(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator());

		final Screen directoriesScreen = createPreferenceScreen("directories");
		final Runnable libraryUpdater = new Runnable() {
			public void run() {
				final BookCollectionShadow bookCollection = new BookCollectionShadow();
				bookCollection.bindToService(PreferenceActivity.this, new Runnable() {
					public void run() {
						bookCollection.reset(false);
						bookCollection.unbind();
					}
				});
			}
		};
		directoriesScreen.addPreference(myChooserCollection.createPreference(
			directoriesScreen.Resource, "bookPath", Paths.BookPathOption, libraryUpdater
		));
		directoriesScreen.addPreference(myChooserCollection.createPreference(
			directoriesScreen.Resource, "downloadDir", Paths.DownloadsDirectoryOption, libraryUpdater
		));
		final PreferenceSet fontReloader = new PreferenceSet.Reloader();
		directoriesScreen.addPreference(myChooserCollection.createPreference(
			directoriesScreen.Resource, "fontPath", Paths.FontPathOption, fontReloader
		));
		final PreferenceSet wallpaperReloader = new PreferenceSet.Reloader();
		directoriesScreen.addPreference(myChooserCollection.createPreference(
			directoriesScreen.Resource, "wallpaperPath", Paths.WallpaperPathOption, wallpaperReloader
		));
		directoriesScreen.addPreference(myChooserCollection.createPreference(
			directoriesScreen.Resource, "tempDir", Paths.TempDirectoryOption, null
		));

		final Screen appearanceScreen = createPreferenceScreen("appearance");
		appearanceScreen.addPreference(new LanguagePreference(
			this, appearanceScreen.Resource, "language", ZLResource.interfaceLanguages()
		) {
			@Override
			protected void init() {
				setInitialValue(ZLResource.getLanguageOption().getValue());
			}

			@Override
			protected void setLanguage(String code) {
				final ZLStringOption languageOption = ZLResource.getLanguageOption();
				if (!code.equals(languageOption.getValue())) {
					languageOption.setValue(code);
					finish();
					startActivity(new Intent(
						Intent.ACTION_VIEW, Uri.parse("fbreader-action:preferences#appearance")
					));
				}
			}
		});
		appearanceScreen.addPreference(new ZLStringChoicePreference(
			this, appearanceScreen.Resource, "screenOrientation",
			androidLibrary.getOrientationOption(), androidLibrary.allOrientations()
		));
		appearanceScreen.addPreference(new ZLBooleanPreference(
			this,
			viewOptions.TwoColumnView,
			appearanceScreen.Resource,
			"twoColumnView"
		));
		appearanceScreen.addPreference(new ZLBooleanPreference(
			this,
			miscOptions.AllowScreenBrightnessAdjustment,
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

		if (DeviceType.Instance().isEInk()) {
			final EInkOptions einkOptions = new EInkOptions();
			final Screen einkScreen = createPreferenceScreen("eink");
			final PreferenceSet einkPreferences = new PreferenceSet.Enabler() {
				@Override
				protected Boolean detectState() {
					return einkOptions.EnableFastRefresh.getValue();
				}
			};

			einkScreen.addPreference(new ZLBooleanPreference(
				this, einkOptions.EnableFastRefresh, einkScreen.Resource, "enableFastRefresh"
			) {
				@Override
				protected void onClick() {
					super.onClick();
					einkPreferences.run();
				}
			});

			final ZLIntegerRangePreference updateIntervalPreference = new ZLIntegerRangePreference(
				this, einkScreen.Resource.getResource("interval"), einkOptions.UpdateInterval
			);
			einkScreen.addPreference(updateIntervalPreference);

			einkPreferences.add(updateIntervalPreference);
			einkPreferences.run();
		}

		final Screen textScreen = createPreferenceScreen("text");

		final Screen fontPropertiesScreen = textScreen.createPreferenceScreen("fontProperties");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.AntiAliasOption, "antiAlias");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.DeviceKerningOption, "deviceKerning");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.DitheringOption, "dithering");
		fontPropertiesScreen.addOption(ZLAndroidPaintContext.SubpixelOption, "subpixel");

		final ZLTextBaseStyle baseStyle = collection.getBaseStyle();

		final FontPreference fontPreference = new FontPreference(
			this, textScreen.Resource, "font",
			baseStyle.FontFamilyOption, false
		);
		textScreen.addPreference(fontPreference);
		fontReloader.add(fontPreference);

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
			spacings[i] = (char)(val / 10 + '0') + decimalSeparator + (char)(val % 10 + '0');
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
			//FBTextKind.XHTML_TAG_P,
			//FBTextKind.TITLE,
			//FBTextKind.SECTION_TITLE,
			//FBTextKind.SUBTITLE,
			//FBTextKind.H1,
			//FBTextKind.H2,
			//FBTextKind.H3,
			//FBTextKind.H4,
			//FBTextKind.H5,
			//FBTextKind.H6,
			FBTextKind.ANNOTATION,
			FBTextKind.EPIGRAPH,
			FBTextKind.AUTHOR,
			FBTextKind.POEM_TITLE,
			//FBTextKind.STANZA,
			FBTextKind.VERSE,
			//FBTextKind.CITE,
		};
		for (int i = 0; i < styles.length; ++i) {
			final ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
			if (decoration == null) {
				continue;
			}

			final Screen formatScreen = moreStylesScreen.createPreferenceScreen(decoration.Name);
			formatScreen.addPreference(new FontPreference(
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
			final String[] allAlignments = { "unchanged", "left", "right", "center", "justify" };
			formatScreen.addPreference(new ZLChoicePreference(
				this, textScreen.Resource, "alignment",
				decoration.AlignmentOption, allAlignments
			));
			formatScreen.addPreference(new ZLBoolean3Preference(
				this, textScreen.Resource, "allowHyphenations",
				decoration.AllowHyphenationsOption
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("spaceBefore"),
				decoration.SpaceBeforeOption
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("spaceAfter"),
				decoration.SpaceAfterOption
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("leftIndent"),
				decoration.LeftIndentOption
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("rightIndent"),
				decoration.RightIndentOption
			));
			formatScreen.addPreference(new ZLIntegerRangePreference(
				this, textScreen.Resource.getResource("firstLineIndent"),
				decoration.FirstLineIndentDeltaOption
			));
			final ZLIntegerOption spacePercentOption = decoration.LineSpacePercentOption;
			final int[] spacingValues = new int[17];
			final String[] spacingKeys = new String[17];
			spacingValues[0] = -1;
			spacingKeys[0] = "unchanged";
			for (int j = 1; j < spacingValues.length; ++j) {
				final int val = 4 + j;
				spacingValues[j] = 10 * val;
				spacingKeys[j] = (char)(val / 10 + '0') + decimalSeparator + (char)(val % 10 + '0');
			}
			formatScreen.addPreference(new ZLIntegerChoicePreference(
				this, textScreen.Resource, "lineSpacing",
				spacePercentOption, spacingValues, spacingKeys
			));
		}

		for (ZLTextNGStyleDescription description : collection.getDescriptionList()) {
			final Screen ngScreen = moreStylesScreen.createPreferenceScreen(description.Name);
			ngScreen.addPreference(new FontPreference(
				this, textScreen.Resource, "font",
				description.FontFamilyOption, true
			));
		}

		final PreferenceSet footerPreferences = new PreferenceSet.Enabler() {
			@Override
			protected Boolean detectState() {
				return viewOptions.ScrollbarType.getValue() == FBView.SCROLLBAR_SHOW_AS_FOOTER;
			}
		};
		final PreferenceSet bgPreferences = new PreferenceSet.Enabler() {
			@Override
			protected Boolean detectState() {
				return "".equals(profile.WallpaperOption.getValue());
			}
		};

		final Screen cssScreen = createPreferenceScreen("css");
		cssScreen.addOption(baseStyle.UseCSSFontFamilyOption, "fontFamily");
		cssScreen.addOption(baseStyle.UseCSSFontSizeOption, "fontSize");
		cssScreen.addOption(baseStyle.UseCSSTextAlignmentOption, "textAlignment");
		cssScreen.addOption(baseStyle.UseCSSMarginsOption, "margins");

		final Screen colorsScreen = createPreferenceScreen("colors");

		final WallpaperPreference wallpaperPreference = new WallpaperPreference(
			this, profile, colorsScreen.Resource, "background"
		) {
			@Override
			protected void onDialogClosed(boolean result) {
				super.onDialogClosed(result);
				bgPreferences.run();
			}
		};
		colorsScreen.addPreference(wallpaperPreference);
		wallpaperReloader.add(wallpaperPreference);

		bgPreferences.add(
			colorsScreen.addOption(profile.BackgroundOption, "backgroundColor")
		);
		bgPreferences.run();
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
			viewOptions.LeftMargin
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("right"),
			viewOptions.RightMargin
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("top"),
			viewOptions.TopMargin
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("bottom"),
			viewOptions.BottomMargin
		));
		marginsScreen.addPreference(new ZLIntegerRangePreference(
			this, marginsScreen.Resource.getResource("spaceBetweenColumns"),
			viewOptions.SpaceBetweenColumns
		));

		final Screen statusLineScreen = createPreferenceScreen("scrollBar");

		final String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
		statusLineScreen.addPreference(new ZLChoicePreference(
			this, statusLineScreen.Resource, "scrollbarType",
			viewOptions.ScrollbarType, scrollBarTypes
		) {
			@Override
			protected void onDialogClosed(boolean result) {
				super.onDialogClosed(result);
				footerPreferences.run();
			}
		});

		footerPreferences.add(statusLineScreen.addPreference(new ZLIntegerRangePreference(
			this, statusLineScreen.Resource.getResource("footerHeight"),
			viewOptions.FooterHeight
		)));
		footerPreferences.add(statusLineScreen.addOption(profile.FooterFillOption, "footerColor"));
		footerPreferences.add(statusLineScreen.addOption(footerOptions.ShowTOCMarks, "tocMarks"));

		footerPreferences.add(statusLineScreen.addOption(footerOptions.ShowProgress, "showProgress"));
		footerPreferences.add(statusLineScreen.addOption(footerOptions.ShowClock, "showClock"));
		footerPreferences.add(statusLineScreen.addOption(footerOptions.ShowBattery, "showBattery"));
		footerPreferences.add(statusLineScreen.addPreference(new FontPreference(
			this, statusLineScreen.Resource, "font",
			footerOptions.Font, false
		)));
		footerPreferences.run();

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

		final Screen scrollingScreen = createPreferenceScreen("scrolling");
		scrollingScreen.addOption(pageTurningOptions.FingerScrolling, "fingerScrolling");
		scrollingScreen.addOption(miscOptions.EnableDoubleTap, "enableDoubleTapDetection");

		final PreferenceSet volumeKeysPreferences = new PreferenceSet.Enabler() {
			@Override
			protected Boolean detectState() {
				return keyBindings.hasBinding(KeyEvent.KEYCODE_VOLUME_UP, false);
			}
		};
		scrollingScreen.addPreference(new ZLCheckBoxPreference(
			this, scrollingScreen.Resource, "volumeKeys"
		) {
			{
				setChecked(keyBindings.hasBinding(KeyEvent.KEYCODE_VOLUME_UP, false));
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
				volumeKeysPreferences.run();
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
		volumeKeysPreferences.run();

		scrollingScreen.addOption(pageTurningOptions.Animation, "animation");
		scrollingScreen.addPreference(new AnimationSpeedPreference(
			this,
			scrollingScreen.Resource,
			"animationSpeed",
			pageTurningOptions.AnimationSpeed
		));
		scrollingScreen.addOption(pageTurningOptions.Horizontal, "horizontal");

		final Screen dictionaryScreen = createPreferenceScreen("dictionary");

		final List<String> langCodes = ZLResource.languageCodes();
		final ArrayList<Language> languages = new ArrayList<Language>(langCodes.size() + 1);
		for (String code : langCodes) {
			languages.add(new Language(code));
		}
		Collections.sort(languages);
		languages.add(0, new Language(
			Language.ANY_CODE, dictionaryScreen.Resource.getResource("targetLanguage")
		));
		final LanguagePreference targetLanguagePreference = new LanguagePreference(
			this, dictionaryScreen.Resource, "targetLanguage", languages
		) {
			@Override
			protected void init() {
				setInitialValue(DictionaryUtil.TargetLanguageOption.getValue());
			}

			@Override
			protected void setLanguage(String code) {
				DictionaryUtil.TargetLanguageOption.setValue(code);
			}
		};

		DictionaryUtil.init(this, new Runnable() {
			public void run() {
				dictionaryScreen.addPreference(new DictionaryPreference(
					PreferenceActivity.this,
					dictionaryScreen.Resource,
					"dictionary",
					DictionaryUtil.singleWordTranslatorOption(),
					DictionaryUtil.dictionaryInfos(PreferenceActivity.this, true)
				) {
					@Override
					protected void onDialogClosed(boolean result) {
						super.onDialogClosed(result);
						targetLanguagePreference.setEnabled(
							DictionaryUtil.getCurrentDictionaryInfo(true).SupportsTargetLanguageSetting
						);
					}
				});
				dictionaryScreen.addPreference(new DictionaryPreference(
					PreferenceActivity.this,
					dictionaryScreen.Resource,
					"translator",
					DictionaryUtil.multiWordTranslatorOption(),
					DictionaryUtil.dictionaryInfos(PreferenceActivity.this, false)
				));
				dictionaryScreen.addPreference(new ZLBooleanPreference(
					PreferenceActivity.this,
					miscOptions.NavigateAllWords,
					dictionaryScreen.Resource,
					"navigateOverAllWords"
				));
				dictionaryScreen.addOption(miscOptions.WordTappingAction, "tappingAction");
				dictionaryScreen.addPreference(targetLanguagePreference);
				targetLanguagePreference.setEnabled(
					DictionaryUtil.getCurrentDictionaryInfo(true).SupportsTargetLanguageSetting
				);
			}
		});

		final Screen imagesScreen = createPreferenceScreen("images");
		imagesScreen.addOption(imageOptions.TapAction, "tappingAction");
		imagesScreen.addOption(imageOptions.FitToScreen, "fitImagesToScreen");
		imagesScreen.addOption(imageOptions.ImageViewBackground, "backgroundColor");
		imagesScreen.addOption(imageOptions.MatchBackground, "matchBackground");

		final CancelMenuHelper cancelMenuHelper = new CancelMenuHelper();
		final Screen cancelMenuScreen = createPreferenceScreen("cancelMenu");
		cancelMenuScreen.addOption(cancelMenuHelper.ShowLibraryItemOption, "library");
		cancelMenuScreen.addOption(cancelMenuHelper.ShowNetworkLibraryItemOption, "networkLibrary");
		cancelMenuScreen.addOption(cancelMenuHelper.ShowPreviousBookItemOption, "previousBook");
		cancelMenuScreen.addOption(cancelMenuHelper.ShowPositionItemsOption, "positions");
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
		aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "facebook"));
		aboutScreen.addPreference(new ThirdPartyLibrariesPreference(this, aboutScreen.Resource, "thirdParty"));
	}
}
