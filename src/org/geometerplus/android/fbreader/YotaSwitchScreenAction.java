/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import android.content.Context;
import android.content.Intent;

import android.os.Vibrator;
import android.view.View;

import com.yotadevices.sdk.utils.RotationAlgorithm;

import com.yotadevices.fbreader.FBReaderYotaService;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

class YotaSwitchScreenAction extends FBAndroidAction {
	private final boolean mySwitchToBack;

	YotaSwitchScreenAction(FBReader baseActivity, FBReaderApp fbreader, boolean switchToBack) {
		super(baseActivity, fbreader);
		mySwitchToBack = switchToBack;
	}

	@Override
	public boolean isVisible() {
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		return zlibrary.YotaDrawOnBackScreenOption.getValue() != mySwitchToBack;
	}

	@Override
	protected void run(Object ... params) {
		final Context context = BaseActivity.getApplicationContext();
		final Intent serviceIntent = new Intent(context, FBReaderYotaService.class);
		final View mainView = BaseActivity.findViewById(R.id.main_view);
		final View mainHiddenView = BaseActivity.findViewById(R.id.main_hidden_view);
		if (mySwitchToBack) {
			RotationAlgorithm.getInstance(context).turnScreenOffIfRotated();
			//serviceIntent.setAction(BroadcastEvents.BROADCAST_ACTION_BACKSCREEN_APPLICATION_ACTIVE);
			context.startService(serviceIntent);
			mainView.setVisibility(View.GONE);
			mainHiddenView.setVisibility(View.VISIBLE);
		} else {
			context.stopService(serviceIntent);
			mainView.setVisibility(View.VISIBLE);
			mainHiddenView.setVisibility(View.GONE);
		}
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		zlibrary.YotaDrawOnBackScreenOption.setValue(mySwitchToBack);
		((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(400);
	}
}
