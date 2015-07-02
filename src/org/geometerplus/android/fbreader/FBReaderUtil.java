/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.os.Build;
import android.view.View;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

public abstract class FBReaderUtil {
	public static ZLAndroidLibrary getZLibrary(Activity activity) {
		return ((ZLAndroidApplication)activity.getApplication()).library();
	}

	public static void ensureFullscreen(Activity activity, View view) {
		if (view == null) {
			return;
		}

		final ZLAndroidLibrary zlibrary = getZLibrary(activity);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
				&& zlibrary.EnableFullscreenModeOption.getValue()) {
			view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE |
				View.SYSTEM_UI_FLAG_IMMERSIVE |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
				View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			);
		} else if (zlibrary.DisableButtonLightsOption.getValue()) {
			view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE
			);
		}
	}
}
