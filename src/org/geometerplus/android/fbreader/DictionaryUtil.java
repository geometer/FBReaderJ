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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class DictionaryUtil {
	private static LinkedList<PackageInfo> ourDictionaryInfos = new LinkedList<PackageInfo>();
	private static ZLStringOption ourDictionaryOption;

	public static List<PackageInfo> dictionaryInfos() {
		if (ourDictionaryInfos.isEmpty()) {
			ourDictionaryInfos.add(new PackageInfo(
				"com.socialnmobile.colordict",
				"com.socialnmobile.colordict.activity.Main",
				"ColorDict",
				null,
				true
			));
			ourDictionaryInfos.add(new PackageInfo(
				"com.ngc.fora",
				"com.ngc.fora.ForaDictionary",
				"Fora Dictionary",
				null,
				true
			));
			ourDictionaryInfos.add(new PackageInfo(
				"com.slovoed.noreg.english_german.deluxe",
				"com.slovoed.noreg.english_german.deluxe.Start",
				"SlovoEd Deluxe German->English",
				"/808464950",
				false
			));
			ourDictionaryInfos.add(new PackageInfo(
				"com.slovoed.noreg.english_german.deluxe",
				"com.slovoed.noreg.english_german.deluxe.Start",
				"SlovoEd Deluxe English->German",
				"/808464949",
				false
			));
		}
		return ourDictionaryInfos;
	}

	public static ZLStringOption dictionaryOption() {
		if (ourDictionaryOption == null) {
			ourDictionaryOption =
				new ZLStringOption("Dictionary", "Title", dictionaryInfos().get(0).Title);
		}
		return ourDictionaryOption;
	}

	private static PackageInfo getCurrentDictionaryInfo() {
		final String title = dictionaryOption().getValue();
		for (PackageInfo info : dictionaryInfos()) {
			if (info.Title.equals(title)) {
				return info;
			}
		}
		return dictionaryInfos().get(0);
	}

	public static Intent getDictionaryIntent(String text) {
		final PackageInfo dictionaryInfo = getCurrentDictionaryInfo();
		if (dictionaryInfo.UseSearchIntend) {
			return new Intent(Intent.ACTION_SEARCH)
				.setComponent(new ComponentName(
					dictionaryInfo.PackageName,
					dictionaryInfo.ClassName
				))
				.putExtra(SearchManager.QUERY, text);
		} else {
			return new Intent(Intent.ACTION_VIEW)
				.setComponent(new ComponentName(
					dictionaryInfo.PackageName,
					dictionaryInfo.ClassName
				))
				.setData(Uri.parse(text + dictionaryInfo.Argument))
				.addFlags(0x14000000);
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
