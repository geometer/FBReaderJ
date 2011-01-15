/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import android.app.*;
import android.content.*;
import android.net.Uri;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class DictionaryUtil {
	// Map: dictionary info -> hide if package is not installed
	private static LinkedHashMap<PackageInfo,Boolean> ourDictionaryInfos =
		new LinkedHashMap<PackageInfo,Boolean>();
	private static ZLStringOption ourDictionaryOption;

	private static Map<PackageInfo,Boolean> infos() {
		if (ourDictionaryInfos.isEmpty()) {
			ourDictionaryInfos.put(new PackageInfo(
				"ColorDict",										// Id
				"com.socialnmobile.colordict",						// Package
				"com.socialnmobile.colordict.activity.Main",		// Class
				"ColorDict",										// Title
				Intent.ACTION_SEARCH,
				"%s"
			), false);
			ourDictionaryInfos.put(new PackageInfo(
				"Fora Dictionary",									// Id
				"com.ngc.fora",										// Package
				"com.ngc.fora.ForaDictionary",						// Class
				"Fora Dictionary",									// Title
				Intent.ACTION_SEARCH,
				"%s"
			), false);
			ourDictionaryInfos.put(new PackageInfo(
				"Free Dictionary . org",							// Id
				"org.freedictionary",								// Package
				"org.freedictionary.MainActivity",					// Class
				"Free Dictionary . org",							// Title
				Intent.ACTION_VIEW,
				"%s"
			), false);
			ourDictionaryInfos.put(new PackageInfo(
				"SlovoEd Deluxe German->English",					// Id
				"com.slovoed.noreg.english_german.deluxe",			// Package
				"com.slovoed.noreg.english_german.deluxe.Start",	// Class
				"SlovoEd Deluxe German->English",					// Title
				Intent.ACTION_VIEW,
				"%s/808464950"
			), true);
			ourDictionaryInfos.put(new PackageInfo(
				"SlovoEd Deluxe English->German",					// Id
				"com.slovoed.noreg.english_german.deluxe",			// Package
				"com.slovoed.noreg.english_german.deluxe.Start",	// Class
				"SlovoEd Deluxe English->German",					// Title
				Intent.ACTION_VIEW,
				"%s/808464949"
			), true);
		}
		return ourDictionaryInfos;
	}

	public static List<PackageInfo> dictionaryInfos(Context context) {
		final LinkedList<PackageInfo> list = new LinkedList<PackageInfo>();
		for (Map.Entry<PackageInfo,Boolean> entry : infos().entrySet()) {
			final PackageInfo info = entry.getKey();
			if (!entry.getValue() ||
				PackageUtil.canBeStarted(context, getDictionaryIntent(info, "test"))) {
				list.add(info);
			}
		}
		return list;
	}

	private static PackageInfo firstInfo() {
		for (Map.Entry<PackageInfo,Boolean> entry : infos().entrySet()) {
			if (!entry.getValue()) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("There are no available dictionary infos");
	}

	public static ZLStringOption dictionaryOption() {
		if (ourDictionaryOption == null) {
			ourDictionaryOption = new ZLStringOption("Dictionary", "Id", firstInfo().Id);
		}
		return ourDictionaryOption;
	}

	private static PackageInfo getCurrentDictionaryInfo() {
		final String id = dictionaryOption().getValue();
		for (PackageInfo info : infos().keySet()) {
			if (info.Id.equals(id)) {
				return info;
			}
		}
		return firstInfo();
	}

	private static Intent getDictionaryIntent(String text) {
		return getDictionaryIntent(getCurrentDictionaryInfo(), text);
	}

	public static Intent getDictionaryIntent(PackageInfo dictionaryInfo, String text) {
		final Intent intent = new Intent(dictionaryInfo.IntentAction)
			.setComponent(new ComponentName(
				dictionaryInfo.PackageName,
				dictionaryInfo.ClassName
			))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		text = dictionaryInfo.IntentDataPattern.replace("%s", text);
		if (Intent.ACTION_SEARCH.equals(dictionaryInfo.IntentAction)) {
			return intent.putExtra(SearchManager.QUERY, text);
		} else {
			return intent.setData(Uri.parse(text));
		}			
	}

	public static void openWordInDictionary(Activity activity, String text) { 
		if (text == null) {
			return;
		}

		int start = 0;
		int end = text.length();
		for (; start < end && !Character.isLetterOrDigit(text.charAt(start)); ++start);
		for (; start < end && !Character.isLetterOrDigit(text.charAt(end - 1)); --end);
		if (start == end) {
			return;
		}

		final Intent intent = DictionaryUtil.getDictionaryIntent(text.substring(start, end));
		try {
			activity.startActivity(intent);
		} catch(ActivityNotFoundException e){
			DictionaryUtil.installDictionaryIfNotInstalled(activity);
		}
	}

	public static void installDictionaryIfNotInstalled(final Activity activity) {
		if (PackageUtil.canBeStarted(activity, getDictionaryIntent("test"))) {
			return;
		}
		final PackageInfo dictionaryInfo = getCurrentDictionaryInfo();

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource installResource = dialogResource.getResource("installDictionary");
		new AlertDialog.Builder(activity)
			.setTitle(installResource.getResource("title").getValue())
			.setMessage(installResource.getResource("message").getValue().replace("%s", dictionaryInfo.Title))
			.setIcon(0)
			.setPositiveButton(
				buttonResource.getResource("install").getValue(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						installDictionary(activity, dictionaryInfo);
					}
				}
			)
			.setNegativeButton(buttonResource.getResource("skip").getValue(), null)
			.create().show();
	}

	private static void installDictionary(Activity activity, PackageInfo dictionaryInfo) {
		if (!PackageUtil.installFromMarket(activity, dictionaryInfo.PackageName)) {
			UIUtil.showErrorMessage(activity, "cannotRunAndroidMarket", dictionaryInfo.Title);
		}
	}
}
