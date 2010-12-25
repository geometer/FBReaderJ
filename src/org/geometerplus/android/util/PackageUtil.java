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

package org.geometerplus.android.util;

import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.net.Uri;

public abstract class PackageUtil {
	private static Uri marketUri(String pkg) {
		return Uri.parse("market://details?id=" + pkg);
	}

	private static Uri homeUri(String pkg) {
		return Uri.parse("http://data.fbreader.org/android/packages/" + pkg + ".apk");
	}

	private static Uri homeUri(String pkg, String version) {
		return Uri.parse("http://data.fbreader.org/android/packages/" + pkg + ".apk_" + version);
	}

	public static boolean isPluginInstalled(Activity activity, String pkg) {
		return canBeStarted(
			activity,
			new Intent("android.fbreader.action.TEST", homeUri(pkg))
		);
	}

	public static boolean isPluginInstalled(Activity activity, String pkg, String version) {
		return canBeStarted(
			activity,
			new Intent("android.fbreader.action.TEST", homeUri(pkg, version))
		);
	}

	public static boolean canBeStarted(Activity activity, Intent intent) {
		return activity.getApplicationContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
	}

	public static boolean installFromMarket(Activity activity, String pkg) {
		try {
			activity.startActivity(new Intent(
				Intent.ACTION_VIEW, marketUri(pkg)
			));
			return true;
		} catch (ActivityNotFoundException e) {
			return false;
		}
	}
}
