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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class InternalUtil {
	static void installDictionaryIfNotInstalled(final Activity activity, final DictionaryUtil.PackageInfo info) {
		if (PackageUtil.canBeStarted(activity, info.getActionIntent("test"), false)) {
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

	private static void installDictionary(Activity activity, DictionaryUtil.PackageInfo dictionaryInfo) {
		if (!PackageUtil.installFromMarket(activity, dictionaryInfo.get("package"))) {
			UIMessageUtil.showErrorMessage(activity, "cannotRunAndroidMarket", dictionaryInfo.getTitle());
		}
	}

	static void startDictionaryActivity(FBReader fbreader, Intent intent, DictionaryUtil.PackageInfo info) {
		try {
			fbreader.startActivity(intent);
			fbreader.overridePendingTransition(0, 0);
		} catch (ActivityNotFoundException e) {
			installDictionaryIfNotInstalled(fbreader, info);
		}
	}
}
