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
import android.os.Bundle;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

public class DialogActivity extends Activity {
	static final Object DIALOG_KEY = new Object();

	private ZLAndroidDialogInterface myDialog;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		myDialog = (ZLAndroidDialogInterface)application.getData(DIALOG_KEY);
		myDialog.setActivity(this);
	}

	protected void onDestroy() {
		myDialog.endActivity();
		super.onDestroy();
	}
}
