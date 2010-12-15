/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.LinkedList;

import android.app.Activity;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.*;

public class BookmarkSearchActivity extends SearchActivity {
	private final LinkedList<Bookmark> myBookmarks = new LinkedList<Bookmark>();

	@Override
	public void onSuccess() {
		BookmarksActivity.Instance.showSearchResultsTab(myBookmarks);
	}

	/*@Override
	public void onFailure() {
	}*/

	@Override
	public String getFailureMessageResourceKey() {
		return "bookmarkNotFound";
	}

	@Override
	public String getWaitMessageResourceKey() {
		return "search";
	}

	@Override
	public boolean runSearch(String pattern) {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		fbreader.BookmarkSearchPatternOption.setValue(pattern);
		pattern = pattern.toLowerCase();
		myBookmarks.clear();
		for (Bookmark bookmark : BookmarksActivity.Instance.AllBooksBookmarks) {
			if (ZLMiscUtil.matchesIgnoreCase(bookmark.getText(), pattern)) {
				myBookmarks.add(bookmark);
			}
		}	
		return !myBookmarks.isEmpty();
	}

	@Override
	public Activity getParentActivity() {
		return BookmarksActivity.Instance;
	}
}
