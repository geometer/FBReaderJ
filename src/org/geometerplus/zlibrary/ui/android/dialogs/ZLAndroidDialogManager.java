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

import java.util.*;

import android.app.*;
import android.content.*;

import org.geometerplus.zlibrary.core.dialogs.*;

import org.geometerplus.android.util.AndroidUtil;

public class ZLAndroidDialogManager extends ZLDialogManager {
	private Activity myActivity;
	
	public ZLAndroidDialogManager() {
	}

	public void setActivity(Activity activity) {
		myActivity = activity;
	}
	
	public ZLOptionsDialog createOptionsDialog(String key, Runnable exitAction, Runnable applyAction, boolean showApplyButton) {
		return new ZLAndroidOptionsDialog(myActivity, getResource().getResource(key), exitAction, applyAction);
	}

	public void wait(String key, Runnable action) {
		AndroidUtil.wait(key, action, myActivity);
	}
}
