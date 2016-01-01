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
import org.geometerplus.android.fbreader.bookmark.BookmarksActivity;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.PackageUtil;

class ShowBookmarksAction extends FBAndroidAction {
	ShowBookmarksAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isVisible() {
		return Reader.Model != null;
	}

	@Override
	protected void run(Object ... params) {
		final Intent externalIntent =
			new Intent(FBReaderIntents.Action.EXTERNAL_BOOKMARKS);
		final Intent internalIntent =
			new Intent(BaseActivity.getApplicationContext(), BookmarksActivity.class);
		if (PackageUtil.canBeStarted(BaseActivity, externalIntent, true)) {
			try {
				startBookmarksActivity(externalIntent);
			} catch (ActivityNotFoundException e) {
				startBookmarksActivity(internalIntent);
			}
		} else {
			startBookmarksActivity(internalIntent);
		}
	}

	private void startBookmarksActivity(Intent intent) {
		FBReaderIntents.putBookExtra(intent, Reader.getCurrentBook());
		FBReaderIntents.putBookmarkExtra(intent, Reader.createBookmark(80, true));
		OrientationUtil.startActivity(BaseActivity, intent);
	}
}
