/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

public abstract class OrientationUtil {
	private static final String KEY = "fbreader.orientation";

	public static void startActivity(Activity current, Intent intent) {
		current.startActivity(intent.putExtra(KEY, current.getRequestedOrientation()));
	}

	public static void startActivityForResult(Activity current, Intent intent, int requestCode) {
		current.startActivityForResult(intent.putExtra(KEY, current.getRequestedOrientation()), requestCode);
	}

	public static void setOrientation(Activity activity, Intent intent) {
		if (intent == null) {
			return;
		}
		final int orientation = intent.getIntExtra(KEY, Integer.MIN_VALUE);
		if (orientation != Integer.MIN_VALUE) {
			activity.setRequestedOrientation(orientation);
		}
	}

	private OrientationUtil() {
	}
}
