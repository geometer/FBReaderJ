/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import org.geometerplus.fbreader.tree.FBTree;

public class LibraryTopLevelActivity extends LibraryActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		new ListAdapter(this, myCurrentTree.subTrees());

		onNewIntent(getIntent());
	}

	@Override
	public void onDestroy() {
		LibraryInstance = null;
		super.onDestroy();
	}

	private void setSearchResults() {
		final List<FBTree> trees = myCurrentTree.subTrees();
		getListAdapter().replaceAll(trees);
		getListView().invalidateViews();
		//new OpenTreeRunnable(trees.get(0)).run();
	}

	public void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (runSearch(intent)) {
				setSearchResults();
			} else {
				showNotFoundToast();
			}
		} else if (ACTION_FOUND.equals(intent.getAction())) {
			setSearchResults();
		}
	}
}
