/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.style.StyleListActivity;
import org.geometerplus.android.util.UIUtil;

public class SelectionBookmarkAction extends FBAndroidAction {
	SelectionBookmarkAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final boolean existingBookmark;
		final Bookmark bookmark;

		if (params.length != 0) {
			existingBookmark = true;
			bookmark = (Bookmark)params[0];
		} else {
			existingBookmark = false;
			bookmark = Reader.addSelectionBookmark();
			UIUtil.showMessageText(
				BaseActivity,
				ZLResource.resource("selection").getResource("bookmarkCreated").getValue()
					.replace("%s", bookmark.getText())
			);
		}

		final Intent intent =
			new Intent(BaseActivity.getApplicationContext(), StyleListActivity.class);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		intent.putExtra(StyleListActivity.EXISTING_BOOKMARK_KEY, existingBookmark);
		OrientationUtil.startActivity(BaseActivity, intent);
	}
}
