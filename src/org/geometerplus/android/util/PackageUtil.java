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

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class PackageUtil {
	private static Uri marketUri(String pkg) {
		return Uri.parse("market://details?id=" + pkg);
	}

	private static Uri homeUri(String pkg) {
		return Uri.parse("http://data.fbreader.org/android/packages/" + pkg + ".apk");
	}

	private static Uri homeUri(String pkg, String version) {
		return Uri.parse("http://data.fbreader.org/android/packages/" + pkg + ".apk_version_" + version);
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

	public static void runInstallPluginDialog(final Activity activity, Map<String,String> pluginData, final Runnable postRunnable) {
		final String plugin = pluginData.get("androidPlugin");
		if (plugin != null) {
			final String pluginVersion = pluginData.get("androidPluginVersion");

			String dialogKey = null;
			String message = null;
			String positiveButtonKey = null;
			
			if (!PackageUtil.isPluginInstalled(activity, plugin)) {
				dialogKey = "installPlugin";
				message = pluginData.get("androidPluginInstallMessage");
				positiveButtonKey = "install";
			} else if (!PackageUtil.isPluginInstalled(activity, plugin, pluginVersion)) {
				dialogKey = "updatePlugin";
				message = pluginData.get("androidPluginUpdateMessage");
				positiveButtonKey = "update";
			}
			if (dialogKey != null) {
				final ZLResource dialogResource = ZLResource.resource("dialog");
				final ZLResource buttonResource = dialogResource.getResource("button");
				new AlertDialog.Builder(activity)
					.setTitle(dialogResource.getResource(dialogKey).getResource("title").getValue())
					.setMessage(message)
					.setIcon(0)
					.setPositiveButton(
						buttonResource.getResource(positiveButtonKey).getValue(),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								PackageUtil.installFromMarket(activity, plugin);
							}
						}
					)
					.setNegativeButton(
						buttonResource.getResource("skip").getValue(),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								postRunnable.run();
							}
						}
					)
					.create().show();
				return;
			}
		}
		postRunnable.run();
	}
}
