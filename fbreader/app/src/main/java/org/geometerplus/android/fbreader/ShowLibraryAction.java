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

import android.content.ActivityNotFoundException;
import android.content.Intent;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.PackageUtil;

class ShowLibraryAction extends FBAndroidAction {
	ShowLibraryAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final Intent externalIntent =
			new Intent(FBReaderIntents.Action.EXTERNAL_LIBRARY);
		final Intent internalIntent =
			new Intent(BaseActivity.getApplicationContext(), LibraryActivity.class);
		if (PackageUtil.canBeStarted(BaseActivity, externalIntent, true)) {
			try {
				startLibraryActivity(externalIntent);
			} catch (ActivityNotFoundException e) {
				startLibraryActivity(internalIntent);
			}
		} else {
			startLibraryActivity(internalIntent);
		}
	}

	private void startLibraryActivity(Intent intent) {
		if (Reader.Model != null) {
			FBReaderIntents.putBookExtra(intent, Reader.getCurrentBook());
		}
		OrientationUtil.startActivity(BaseActivity, intent);
	}
}
