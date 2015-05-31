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

package org.geometerplus.android.fbreader;

import java.io.InputStream;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.github.johnpersano.supertoasts.util.OnDismissWrapper;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Looper;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.View;

import com.abbyy.mobile.lingvo.api.MinicardContract;
import com.paragon.dictionary.fbreader.OpenDictionaryFlyout;
import com.paragon.open.dictionary.api.Dictionary;
import com.paragon.open.dictionary.api.OpenDictionaryAPI;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.XmlUtil;

import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWord;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class DictionaryUtil {
	private static int FLAG_INSTALLED_ONLY = 1;
	private static int FLAG_SHOW_AS_DICTIONARY = 2;
	private static int FLAG_SHOW_AS_TRANSLATOR = 4;

	private static final int MAX_LENGTH_FOR_TOAST = 180;

	private static ZLStringOption ourSingleWordTranslatorOption;
	private static ZLStringOption ourMultiWordTranslatorOption;

	// TODO: use StringListOption instead
	public static final ZLStringOption TargetLanguageOption = new ZLStringOption("Dictionary", "TargetLanguage", Language.ANY_CODE);

	// Map: dictionary info -> mode if package is not installed
	private static Map<PackageInfo,Integer> ourInfos =
		Collections.synchronizedMap(new LinkedHashMap<PackageInfo,Integer>());

	public static abstract class PackageInfo extends HashMap<String,String> {
		public final boolean SupportsTargetLanguageSetting;

		PackageInfo(String id, String title, boolean supportsTargetLanguageSetting) {
			put("id", id);
			put("title", title);

			SupportsTargetLanguageSetting = supportsTargetLanguageSetting;
		}

		public final String getId() {
			return get("id");
		}

		public final String getTitle() {
			return get("title");
		}

		final Intent getDictionaryIntent(String text) {
			final Intent intent = new Intent(get("action"));

			final String packageName = get("package");
			if (packageName != null) {
				final String className = get("class");
				if (className != null) {
					intent.setComponent(new ComponentName(
						packageName,
						className.startsWith(".") ? packageName + className : className
					));
				}
			}

			final String category = get("category");
			if (category != null) {
				intent.addCategory(category);
			}

			final String key = get("dataKey");
			if (key != null) {
				return intent.putExtra(key, text);
			} else {
				return intent.setData(Uri.parse(text));
			}
		}

		abstract void open(String text, ZLTextRegion.Soul soulToSelect, FBReader fbreader, PopupFrameMetric frameMetrics);
	}

	private static class PlainPackageInfo extends PackageInfo {
		PlainPackageInfo(String id, String title, boolean supportsTargetLanguageSetting) {
			super(id, title, supportsTargetLanguageSetting);
		}

		@Override
		void open(String text, ZLTextRegion.Soul soulToSelect, FBReader fbreader, PopupFrameMetric frameMetrics) {
			final Intent intent = getDictionaryIntent(text);
			try {
				final String id = getId();
				if ("ABBYY Lingvo".equals(id)) {
					intent.putExtra(MinicardContract.EXTRA_GRAVITY, frameMetrics.Gravity);
					intent.putExtra(MinicardContract.EXTRA_HEIGHT, frameMetrics.Height);
					intent.putExtra(MinicardContract.EXTRA_FORCE_LEMMATIZATION, true);
					intent.putExtra(MinicardContract.EXTRA_TRANSLATE_VARIANTS, true);
					intent.putExtra(MinicardContract.EXTRA_LIGHT_THEME, true);
					final String targetLanguage = TargetLanguageOption.getValue();
					if (!Language.ANY_CODE.equals(targetLanguage)) {
						intent.putExtra(MinicardContract.EXTRA_LANGUAGE_TO, targetLanguage);
					}
				} else if ("ColorDict".equals(id)) {
					intent.putExtra(ColorDict3.HEIGHT, frameMetrics.Height);
					intent.putExtra(ColorDict3.GRAVITY, frameMetrics.Gravity);
					final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
					intent.putExtra(ColorDict3.FULLSCREEN, !zlibrary.ShowStatusBarOption.getValue());
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				fbreader.startActivity(intent);
				fbreader.overridePendingTransition(0, 0);
			} catch (ActivityNotFoundException e) {
				installDictionaryIfNotInstalled(fbreader, this);
			}
		}
	}

	private static class DictanPackageInfo extends PackageInfo {
		DictanPackageInfo(String id, String title, boolean supportsTargetLanguageSetting) {
			super(id, title, supportsTargetLanguageSetting);
		}

		@Override
		void open(String text, ZLTextRegion.Soul soulToSelect, FBReader fbreader, PopupFrameMetric frameMetrics) {
			final Intent intent = getDictionaryIntent(text);
			try {
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				intent.putExtra("article.mode", 20);
				intent.putExtra("article.text.size.max", MAX_LENGTH_FOR_TOAST);
				fbreader.startActivityForResult(intent, FBReader.REQUEST_DICTIONARY);
				fbreader.overridePendingTransition(0, 0);
				fbreader.outlineRegion(soulToSelect);
			} catch (ActivityNotFoundException e) {
				fbreader.hideOutline();
				installDictionaryIfNotInstalled(fbreader, this);
			}
		}
	}

	private static class OpenDictionaryPackageInfo extends PackageInfo {
		final OpenDictionaryFlyout Flyout;

		OpenDictionaryPackageInfo(Dictionary dictionary) {
			super(
				dictionary.getUID(),
				dictionary.getName(),
				false
			);
			put("package", dictionary.getApplicationPackageName());
			put("class", ".Start");
			Flyout = new OpenDictionaryFlyout(dictionary);
		}

		@Override
		void open(String text, ZLTextRegion.Soul soulToSelect, FBReader fbreader, PopupFrameMetric frameMetrics) {
			Flyout.showTranslation(fbreader, text, frameMetrics);
		}
	}

	private static class InfoReader extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (!"dictionary".equals(localName)) {
				return;
			}

			final String id = attributes.getValue("id");
			final String title = attributes.getValue("title");
			final String role = attributes.getValue("role");
			int flags;
			if ("dictionary".equals(role)) {
				flags = FLAG_SHOW_AS_DICTIONARY;
			} else if ("translator".equals(role)) {
				flags = FLAG_SHOW_AS_TRANSLATOR;
			} else {
				flags = FLAG_SHOW_AS_DICTIONARY | FLAG_SHOW_AS_TRANSLATOR;
			}
			if (!"always".equals(attributes.getValue("list"))) {
				flags |= FLAG_INSTALLED_ONLY;
			}
			final PackageInfo info;
			if ("dictan".equals(id)) {
				info = new DictanPackageInfo(
					id,
					title != null ? title : id,
					false
				);
			} else {
				info = new PlainPackageInfo(
					id,
					title != null ? title : id,
					"true".equals(attributes.getValue("supportsTargetLanguage"))
				);
			}
			for (int i = attributes.getLength() - 1; i >= 0; --i) {
				info.put(attributes.getLocalName(i), attributes.getValue(i));
			}
			ourInfos.put(info, flags);
		}
	}

	private static class BitKnightsInfoReader extends DefaultHandler {
		private final Context myContext;
		private int myCounter;

		BitKnightsInfoReader(Context context) {
			myContext = context;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (!"dictionary".equals(localName)) {
				return;
			}

			final PackageInfo info = new PlainPackageInfo(
				"BK" + myCounter ++,
				attributes.getValue("title"),
				false
			);
			for (int i = attributes.getLength() - 1; i >= 0; --i) {
				info.put(attributes.getLocalName(i), attributes.getValue(i));
			}
			info.put("class", "com.bitknights.dict.ShareTranslateActivity");
			info.put("action", Intent.ACTION_VIEW);
			// TODO: other attributes
			if (PackageUtil.canBeStarted(myContext, info.getDictionaryIntent("test"), false)) {
				ourInfos.put(info, FLAG_SHOW_AS_DICTIONARY | FLAG_INSTALLED_ONLY);
			}
		}
	}

	private interface ColorDict3 {
		String ACTION = "colordict.intent.action.SEARCH";
		String QUERY = "EXTRA_QUERY";
		String HEIGHT = "EXTRA_HEIGHT";
		String WIDTH = "EXTRA_WIDTH";
		String GRAVITY = "EXTRA_GRAVITY";
		String MARGIN_LEFT = "EXTRA_MARGIN_LEFT";
		String MARGIN_TOP = "EXTRA_MARGIN_TOP";
		String MARGIN_BOTTOM = "EXTRA_MARGIN_BOTTOM";
		String MARGIN_RIGHT = "EXTRA_MARGIN_RIGHT";
		String FULLSCREEN = "EXTRA_FULLSCREEN";
	}

	private static void collectOpenDictionaries(Context context) {
		final SortedSet<Dictionary> dictionariesTreeSet =
			new TreeSet<Dictionary>(new Comparator<Dictionary>() {
				@Override
				public int compare(Dictionary lhs, Dictionary rhs) {
					return lhs.toString().compareTo(rhs.toString());
				}
			}
		);
		dictionariesTreeSet.addAll(
			new OpenDictionaryAPI(context).getDictionaries()
		);

		for (Dictionary dict : dictionariesTreeSet) {
			final PackageInfo info = new OpenDictionaryPackageInfo(dict);
			ourInfos.put(info, FLAG_SHOW_AS_DICTIONARY);
		}
	}

	private static final class Initializer implements Runnable {
		private final Activity myActivity;
		private final Runnable myPostAction;

		public Initializer(Activity activity, Runnable postAction) {
			myActivity = activity;
			myPostAction = postAction;
		}

		public void run() {
			synchronized (ourInfos) {
				if (!ourInfos.isEmpty()) {
					if (myPostAction != null) {
						myPostAction.run();
					}
					return;
				}
				XmlUtil.parseQuietly(
					ZLFile.createFileByPath("dictionaries/main.xml"),
					new InfoReader()
				);
				XmlUtil.parseQuietly(
					ZLFile.createFileByPath("dictionaries/bitknights.xml"),
					new BitKnightsInfoReader(myActivity)
				);
				myActivity.runOnUiThread(new Runnable() {
					public void run() {
						collectOpenDictionaries(myActivity);
						if (myPostAction != null) {
							myPostAction.run();
						}
					}
				});
			}
		}
	}

	public static void init(Activity activity, Runnable postAction) {
		if (ourInfos.isEmpty()) {
			final Thread initThread = new Thread(new Initializer(activity, postAction));
			initThread.setPriority(Thread.MIN_PRIORITY);
			initThread.start();
		} else if (postAction != null) {
			postAction.run();
		}
	}

	public static List<PackageInfo> dictionaryInfos(Context context, boolean dictionaryNotTranslator) {
		final LinkedList<PackageInfo> list = new LinkedList<PackageInfo>();
		final HashSet<String> installedPackages = new HashSet<String>();
		final HashSet<String> notInstalledPackages = new HashSet<String>();
		synchronized (ourInfos) {
			for (Map.Entry<PackageInfo,Integer> entry : ourInfos.entrySet()) {
				final PackageInfo info = entry.getKey();
				final int flags = entry.getValue();
				if (dictionaryNotTranslator) {
					if ((flags & FLAG_SHOW_AS_DICTIONARY) == 0) {
						continue;
					}
				} else {
					if ((flags & FLAG_SHOW_AS_TRANSLATOR) == 0) {
						continue;
					}
				}

				final String packageName = info.get("package");
				if (((flags & FLAG_INSTALLED_ONLY) == 0) ||
					installedPackages.contains(packageName)) {
					list.add(info);
				} else if (!notInstalledPackages.contains(packageName)) {
					if (PackageUtil.canBeStarted(context, info.getDictionaryIntent("test"), false)) {
						list.add(info);
						installedPackages.add(packageName);
					} else {
						notInstalledPackages.add(packageName);
					}
				}
			}
		}
		return list;
	}

	private static PackageInfo firstInfo() {
		synchronized (ourInfos) {
			for (Map.Entry<PackageInfo,Integer> entry : ourInfos.entrySet()) {
				if ((entry.getValue() & FLAG_INSTALLED_ONLY) == 0) {
					return entry.getKey();
				}
			}
		}
		throw new RuntimeException("There are no available dictionary infos");
	}

	public static ZLStringOption singleWordTranslatorOption() {
		if (ourSingleWordTranslatorOption == null) {
			ourSingleWordTranslatorOption = new ZLStringOption("Dictionary", "Id", firstInfo().getId());
		}
		return ourSingleWordTranslatorOption;
	}

	public static ZLStringOption multiWordTranslatorOption() {
		if (ourMultiWordTranslatorOption == null) {
			ourMultiWordTranslatorOption = new ZLStringOption("Translator", "Id", firstInfo().getId());
		}
		return ourMultiWordTranslatorOption;
	}

	private static PackageInfo getInfo(String id) {
		if (id == null) {
			return firstInfo();
		}

		synchronized (ourInfos) {
			for (PackageInfo info : ourInfos.keySet()) {
				if (id.equals(info.getId())) {
					return info;
				}
			}
		}
		return firstInfo();
	}

	public static PackageInfo getCurrentDictionaryInfo(boolean singleWord) {
		final ZLStringOption option = singleWord
			? singleWordTranslatorOption() : multiWordTranslatorOption();
		return getInfo(option.getValue());
	}

	public static class PopupFrameMetric {
		public final int Height;
		public final int Gravity;

		PopupFrameMetric(DisplayMetrics metrics, int selectionTop, int selectionBottom) {
			final int screenHeight = metrics.heightPixels;
			final int topSpace = selectionTop;
			final int bottomSpace = metrics.heightPixels - selectionBottom;
			final boolean showAtBottom = bottomSpace >= topSpace;
			final int space = (showAtBottom ? bottomSpace : topSpace) - metrics.densityDpi / 12;
			final int maxHeight = Math.min(metrics.densityDpi * 20 / 12, screenHeight * 2 / 3);
			final int minHeight = Math.min(metrics.densityDpi * 10 / 12, screenHeight * 2 / 3);

			Height = Math.max(minHeight, Math.min(maxHeight, space));
			Gravity = showAtBottom ? android.view.Gravity.BOTTOM : android.view.Gravity.TOP;
		}
	}

	public static void openTextInDictionary(final FBReader fbreader, String text, boolean singleWord, int selectionTop, int selectionBottom, final ZLTextRegion.Soul soulToSelect) {
		final String textToTranslate;
		if (singleWord) {
			int start = 0;
			int end = text.length();
			for (; start < end && !Character.isLetterOrDigit(text.charAt(start)); ++start);
			for (; start < end && !Character.isLetterOrDigit(text.charAt(end - 1)); --end);
			if (start == end) {
				return;
			}
			textToTranslate = text.substring(start, end);
		} else {
			textToTranslate = text;
		}

		final DisplayMetrics metrics = new DisplayMetrics();
		fbreader.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		final PopupFrameMetric frameMetrics =
			new PopupFrameMetric(metrics, selectionTop, selectionBottom);

		final PackageInfo info = getCurrentDictionaryInfo(singleWord);
		fbreader.runOnUiThread(new Runnable() {
			public void run() {
				info.open(textToTranslate, soulToSelect, fbreader, frameMetrics);
			}
		});
	}

	public static void openWordInDictionary(FBReader fbreader, ZLTextWord word, ZLTextRegion region) {
		openTextInDictionary(
			fbreader, word.toString(), true, region.getTop(), region.getBottom(), region.getSoul()
		);
	}

	public static void installDictionaryIfNotInstalled(final Activity activity, final PackageInfo info) {
		if (PackageUtil.canBeStarted(activity, info.getDictionaryIntent("test"), false)) {
			return;
		}

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource installResource = dialogResource.getResource("installDictionary");
		new AlertDialog.Builder(activity)
			.setTitle(installResource.getResource("title").getValue())
			.setMessage(installResource.getResource("message").getValue().replace("%s", info.getTitle()))
			.setIcon(0)
			.setPositiveButton(
				buttonResource.getResource("install").getValue(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						installDictionary(activity, info);
					}
				}
			)
			.setNegativeButton(buttonResource.getResource("skip").getValue(), null)
			.create().show();
	}

	private static void installDictionary(Activity activity, PackageInfo dictionaryInfo) {
		if (!PackageUtil.installFromMarket(activity, dictionaryInfo.get("package"))) {
			UIUtil.showErrorMessage(activity, "cannotRunAndroidMarket", dictionaryInfo.getTitle());
		}
	}

	static void onActivityResult(final FBReader fbreader, int resultCode, final Intent data) {
		if (data == null) {
			fbreader.hideOutline();
			return;
		}

		final int errorCode = data.getIntExtra("error.code", -1);
		if (resultCode != Activity.RESULT_OK || errorCode != -1) {
			showDictanError(fbreader, errorCode, data);
			return;
		}

		String text = data.getStringExtra("article.text");
		if (text == null) {
			showDictanError(fbreader, -1, data);
			return;
		}
		// a hack for obsolete (before 5.0 beta) dictan versions
		final int index = text.indexOf("\000");
		if (index >= 0) {
			text = text.substring(0, index);
		}
		final boolean hasExtraData;
		if (text.length() == MAX_LENGTH_FOR_TOAST) {
			text = trimArticle(text);
			hasExtraData = true;
		} else {
			hasExtraData = data.getBooleanExtra("article.resources.contains", false);
		}

		final SuperActivityToast toast;
		if (hasExtraData) {
			toast = new SuperActivityToast(fbreader, SuperToast.Type.BUTTON);
			toast.setButtonIcon(
				android.R.drawable.ic_menu_more,
				ZLResource.resource("footnoteToast").getResource("more").getValue()
			);
			toast.setOnClickWrapper(new OnClickWrapper("dict", new SuperToast.OnClickListener() {
				@Override
				public void onClick(View view, Parcelable token) {
					final String word = data.getStringExtra("article.word");
					final PackageInfo info = getInfo("dictan");
					final Intent intent = info.getDictionaryIntent(word);
					try {
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						fbreader.startActivity(intent);
						fbreader.overridePendingTransition(0, 0);
					} catch (ActivityNotFoundException e) {
						// ignore
					}
				}
			}));
		} else {
			toast = new SuperActivityToast(fbreader, SuperToast.Type.STANDARD);
		}
		toast.setText(text);
		toast.setDuration(20000);
		toast.setOnDismissWrapper(new OnDismissWrapper("ftnt", new SuperToast.OnDismissListener() {
			@Override
			public void onDismiss(View view) {
				fbreader.hideOutline();
			}
		}));
		fbreader.showToast(toast);
	}

	private static void showDictanError(final FBReader fbreader, int code, Intent data) {
		final ZLResource resource = ZLResource.resource("dictanErrors");
		String message;
		switch (code) {
			default:
				message = data.getStringExtra("error.message");
				if (message == null) {
					message = resource.getResource("unknown").getValue();
				}
				break;
			case 100:
			{
				final String word = data.getStringExtra("article.word");
				message = resource.getResource("noArticle").getValue().replaceAll("%s", word);
				break;
			}
			case 130:
				message = resource.getResource("cannotOpenDictionary").getValue();
				break;
			case 131:
				message = resource.getResource("noDictionarySelected").getValue();
				break;
		}

		final SuperActivityToast toast = new SuperActivityToast(fbreader, SuperToast.Type.STANDARD);
		toast.setText("Dictan: " + message);
		toast.setDuration(5000);
		toast.setOnDismissWrapper(new OnDismissWrapper("ftnt", new SuperToast.OnDismissListener() {
			@Override
			public void onDismiss(View view) {
				fbreader.hideOutline();
			}
		}));
		fbreader.showToast(toast);
	}

	private static String trimArticle(String text) {
		final int len = text.length();
		final int eolIndex = text.lastIndexOf("\n");
		final int spaceIndex = text.lastIndexOf(" ");
		if (spaceIndex < eolIndex || eolIndex >= len * 2 / 3) {
			return text.substring(0, eolIndex);
		} else {
			return text.substring(0, spaceIndex);
		}
	}
}
