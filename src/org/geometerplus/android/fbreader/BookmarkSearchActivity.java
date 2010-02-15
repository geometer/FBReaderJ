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
import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.library.*;

public class BookmarkSearchActivity extends SearchActivity {
	private final LinkedList<Bookmark> myBookmarks = new LinkedList<Bookmark>();

	@Override
	void onSuccess() {
		BookmarksActivity.Instance.showSearchResultsTab(myBookmarks);
	}

	@Override
	void onFailure() {
	}

	@Override
	String getFailureMessageResourceKey() {
		return "bookmarkNotFound";
	}

	@Override
	String getWaitMessageResourceKey() {
		return "search";
	}

	@Override
	boolean runSearch(String pattern) {
		final FBReader fbreader = (FBReader)FBReader.Instance();
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
	Activity getParentActivity() {
		return BookmarksActivity.Instance;
	}
}
