/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

public class ZLAndroidOptionsDialog extends ZLOptionsDialog {
	private final Activity myMainActivity;

	public ZLAndroidOptionsDialog(Activity activity, ZLResource resource) {
		super(resource);
		myMainActivity = activity;
	}

	private static void runDialog(Activity activity, ZLAndroidDialogInterface dialog) {
		((ZLAndroidApplication)activity.getApplication()).putData(
			DialogActivity.DIALOG_KEY, dialog
		);
		Intent intent = new Intent();
		intent.setClass(activity, DialogActivity.class);
		activity.startActivity(intent);
	}

	public void run(int index) {
		final ZLAndroidDialogContent tab = (ZLAndroidDialogContent)myTabs.get(index);
		runDialog(myMainActivity, tab);
	}

	public ZLDialogContent createTab(String key) {
		final Context context = myMainActivity;
		final ZLDialogContent tab =
			new ZLAndroidDialogContent(context, getTabResource(key));
		myTabs.add(tab);
		return tab;
	}
}
