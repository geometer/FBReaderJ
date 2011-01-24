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

package org.geometerplus.android.util;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.CheckBox;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

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

	public static boolean canBeStarted(Context context, Intent intent) {
		return context.getApplicationContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
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
			final ZLBooleanOption doNotInstallOption = new ZLBooleanOption("doNotInstall", plugin, false);
			if (!doNotInstallOption.getValue()) {
				final String pluginVersion = pluginData.get("androidPluginVersion");
            
				String message = null;
				String positiveButtonKey = null;
				String titleResourceKey = null;
				
				if (!PackageUtil.isPluginInstalled(activity, plugin)) {
					message = pluginData.get("androidPluginInstallMessage");
					positiveButtonKey = "install";
					titleResourceKey = "installTitle";
				} else if (!PackageUtil.isPluginInstalled(activity, plugin, pluginVersion)) {
					message = pluginData.get("androidPluginUpdateMessage");
					positiveButtonKey = "update";
					titleResourceKey = "updateTitle";
				}
				if (message != null) {
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource pluginDialogResource = dialogResource.getResource("plugin");
					final ZLResource buttonResource = dialogResource.getResource("button");
					final View view = activity.getLayoutInflater().inflate(R.layout.plugin_dialog, null, false);
					((TextView)view.findViewById(R.id.plugin_dialog_text)).setText(message);
					final CheckBox checkBox = (CheckBox)view.findViewById(R.id.plugin_dialog_checkbox);
					checkBox.setText(pluginDialogResource.getResource("dontAskAgain").getValue());
					final AlertDialog dialog = new AlertDialog.Builder(activity)
						.setTitle(pluginDialogResource.getResource(titleResourceKey).getValue())
						.setView(view)
						.setIcon(0)
						.setPositiveButton(
							buttonResource.getResource(positiveButtonKey).getValue(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if (!PackageUtil.installFromMarket(activity, plugin)) {
										UIUtil.showErrorMessage(activity, "cannotRunAndroidMarket", "plugin");
									}
								}
							}
						)
						.setNegativeButton(
							buttonResource.getResource("skip").getValue(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									doNotInstallOption.setValue(checkBox.isChecked());
									postRunnable.run();
								}
							}
						)
						.create();
					dialog.show();
					return;
				}
			}
		}
		postRunnable.run();
	}
}
