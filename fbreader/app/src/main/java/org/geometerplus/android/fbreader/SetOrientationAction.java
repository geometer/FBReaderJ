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

package org.geometerplus.android.fbreader;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import org.fbreader.util.Boolean3;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class SetScreenOrientationAction extends FBAndroidAction {
	static void setOrientation(Activity activity, String optionValue) {
		int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		if (ZLibrary.SCREEN_ORIENTATION_SENSOR.equals(optionValue)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		} else if (ZLibrary.SCREEN_ORIENTATION_PORTRAIT.equals(optionValue)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else if (ZLibrary.SCREEN_ORIENTATION_LANDSCAPE.equals(optionValue)) {
			orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		} else if (ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT.equals(optionValue)) {
			orientation = 9; // ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
		} else if (ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE.equals(optionValue)) {
			orientation = 8; // ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
		}
		activity.setRequestedOrientation(orientation);
	}

	private final String myOptionValue;

	SetScreenOrientationAction(FBReader baseActivity, FBReaderApp fbreader, String optionValue) {
		super(baseActivity, fbreader);
		myOptionValue = optionValue;
	}

	@Override
	public Boolean3 isChecked() {
		return myOptionValue.equals(ZLibrary.Instance().getOrientationOption().getValue())
			? Boolean3.TRUE : Boolean3.FALSE;
	}

	@Override
	protected void run(Object ... params) {
		setOrientation(BaseActivity, myOptionValue);
		ZLibrary.Instance().getOrientationOption().setValue(myOptionValue);
		Reader.onRepaintFinished();
	}
}
