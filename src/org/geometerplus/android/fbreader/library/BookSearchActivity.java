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

package org.geometerplus.android.fbreader.library;

import android.app.Activity;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.SearchActivity;

public class BookSearchActivity extends SearchActivity {
	//private LibraryTree myTree;

	@Override
	public void onSuccess() {
		//LibraryTabActivity.Instance.showSearchResultsTab(myTree);
	}

	/*@Override
	public void onFailure() {
	}*/

	@Override
	public String getFailureMessageResourceKey() {
		return "bookNotFound";
	}

	@Override
	public String getWaitMessageResourceKey() {
		return "search";
	}

	@Override
	public boolean runSearch(final String pattern) {
		/*
		final LibraryTabActivity parentActivity = LibraryTabActivity.Instance;
		parentActivity.BookSearchPatternOption.setValue(pattern);
		myTree = parentActivity.library().searchBooks(pattern);
		return myTree.hasChildren();
		*/
		return false;
	}

	@Override
	public Activity getParentActivity() {
		return null;//LibraryTabActivity.Instance;
	}
}
