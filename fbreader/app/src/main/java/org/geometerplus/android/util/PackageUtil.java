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

package org.geometerplus.android.util;

import java.util.Map;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.CheckBox;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class PackageUtil {
	private static Uri marketUri(String pkg) {
		return Uri.parse("market://details?id=" + pkg);
	}

	public static boolean canBeStarted(Context context, Intent intent, boolean checkSignature) {
		final PackageManager manager = context.getApplicationContext().getPackageManager();
		final ResolveInfo info =
			manager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (info == null) {
			return false;
		}
		final ActivityInfo activityInfo = info.activityInfo;
		if (activityInfo == null) {
			return false;
		}
		if (!checkSignature) {
			return true;
		}
		return
			PackageManager.SIGNATURE_MATCH ==
			manager.checkSignatures(context.getPackageName(), activityInfo.packageName);
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
