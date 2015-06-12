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

package org.geometerplus.android.fbreader.dict;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.util.DisplayMetrics;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.options.ZLEnumOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.XmlUtil;

import org.geometerplus.fbreader.fbreader.DurationEnum;
import org.geometerplus.android.fbreader.FBReaderMainActivity;
import org.geometerplus.android.util.PackageUtil;

public abstract class DictionaryUtil {
	public static final ZLEnumOption<DurationEnum> TranslationToastDurationOption =
		new ZLEnumOption<DurationEnum>("Dictionary", "TranslationToastDuration", DurationEnum.duration40);
	public static final ZLEnumOption<DurationEnum> ErrorToastDurationOption =
		new ZLEnumOption<DurationEnum>("Dictionary", "ErrorToastDuration", DurationEnum.duration5);

	private static int FLAG_INSTALLED_ONLY = 1;
	static int FLAG_SHOW_AS_DICTIONARY = 2;
	private static int FLAG_SHOW_AS_TRANSLATOR = 4;

	private static ZLStringOption ourSingleWordTranslatorOption;
	private static ZLStringOption ourMultiWordTranslatorOption;

	// TODO: use StringListOption instead
	public static final ZLStringOption TargetLanguageOption = new ZLStringOption("Dictionary", "TargetLanguage", Language.ANY_CODE);

	// Map: dictionary info -> mode if package is not installed
	private static Map<PackageInfo,Integer> ourInfos =
		Collections.synchronizedMap(new LinkedHashMap<PackageInfo,Integer>());

	public static abstract class PackageInfo extends HashMap<String,String> {
		public final boolean SupportsTargetLanguageSetting;

		PackageInfo(String id, String title) {
			this(id, title, false);
		}

		PackageInfo(String id, String title, boolean supportsTargetLanguageSetting) {
			put("id", id);
			put("title", title != null ? title : id);

			SupportsTargetLanguageSetting = supportsTargetLanguageSetting;
		}

		public final String getId() {
			return get("id");
		}

		public final String getTitle() {
			return get("title");
		}

		final Intent getActionIntent(String text) {
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

		void onActivityResult(FBReaderMainActivity fbreader, int resultCode, final Intent data) {
			// does nothing; implement in subclasses
		}

		abstract void open(String text, Runnable outliner, FBReaderMainActivity fbreader, PopupFrameMetric frameMetrics);
	}

	private static class PlainPackageInfo extends PackageInfo {
		PlainPackageInfo(String id, String title) {
			super(id, title);
		}

		@Override
		void open(String text, Runnable outliner, FBReaderMainActivity fbreader, PopupFrameMetric frameMetrics) {
			final Intent intent = getActionIntent(text);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			InternalUtil.startDictionaryActivity(fbreader, intent, this);
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
				info = new Dictan(id, title);
			} else if ("ABBYY Lingvo".equals(id)) {
				info = new Lingvo(id, title);
			} else if ("ColorDict".equals(id)) {
				info = new ColorDict(id, title);
			} else {
				info = new PlainPackageInfo(id, title);
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
				attributes.getValue("title")
			);
			for (int i = attributes.getLength() - 1; i >= 0; --i) {
				info.put(attributes.getLocalName(i), attributes.getValue(i));
			}
			info.put("class", "com.bitknights.dict.ShareTranslateActivity");
			info.put("action", Intent.ACTION_VIEW);
			// TODO: other attributes
			if (PackageUtil.canBeStarted(myContext, info.getActionIntent("test"), false)) {
				ourInfos.put(info, FLAG_SHOW_AS_DICTIONARY | FLAG_INSTALLED_ONLY);
			}
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
						OpenDictionary.collect(myActivity, ourInfos);
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
					if (PackageUtil.canBeStarted(context, info.getActionIntent("test"), false)) {
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

	private static PackageInfo getDictionaryInfo(String id) {
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
		return getDictionaryInfo(option.getValue());
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

	public static void openTextInDictionary(final FBReaderMainActivity fbreader, String text, boolean singleWord, int selectionTop, int selectionBottom, final Runnable outliner) {
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
				info.open(textToTranslate, outliner, fbreader, frameMetrics);
			}
		});
	}

	public static void onActivityResult(final FBReaderMainActivity fbreader, int resultCode, final Intent data) {
		getDictionaryInfo("dictan").onActivityResult(fbreader, resultCode, data);
	}
}
