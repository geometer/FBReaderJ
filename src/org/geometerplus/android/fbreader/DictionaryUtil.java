/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;


import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

abstract class DictionaryUtil {
	public static Intent getDictionaryIntent(String text) {
		return new Intent(Intent.ACTION_SEARCH)
			.setComponent(new ComponentName(
				"com.socialnmobile.colordict",
				"com.socialnmobile.colordict.activity.Main"
			))
			.putExtra(SearchManager.QUERY, text);
	}

	public static void installDictionaryIfNotInstalled(final Activity activity) {
		if (PackageUtil.canBeStarted(activity, getDictionaryIntent("test"))) {
			return;
		}

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource installResource = dialogResource.getResource("installDictionary");
		new AlertDialog.Builder(activity)
			.setTitle(installResource.getResource("title").getValue())
			.setMessage(installResource.getResource("message").getValue().replace("%s", "ColorDict"))
			.setIcon(0)
			.setPositiveButton(
				buttonResource.getResource("install").getValue(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						installDictionary(activity);
					}
				}
			)
			.setNegativeButton(buttonResource.getResource("skip").getValue(), null)
			.create().show();
	}

	private static void installDictionary(Activity activity) {
		if (!PackageUtil.installFromMarket(activity, "com.socialnmobile.colordict")) {
			UIUtil.showErrorMessage(activity, "cannotRunAndroidMarket", "ColorDict");
		}
	}
}
