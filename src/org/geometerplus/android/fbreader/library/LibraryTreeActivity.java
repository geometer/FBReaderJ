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

import android.content.Intent;
import android.os.Bundle;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.FileTree;

public class LibraryTreeActivity extends LibraryBaseActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (runSearch(intent)) {
				startActivity(intent
					.setAction(ACTION_FOUND)
					.setClass(getApplicationContext(), LibraryTopLevelActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
				);
			} else {
				showNotFoundToast();
				finish();
			}
			return;
		}

		final ListAdapter adapter = new ListAdapter(this, myCurrentTree.subTrees());
		setSelection(adapter.getFirstSelectedItemIndex());

		getListView().setTextFilterEnabled(true);
	}

	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		if (myCurrentTree instanceof FileTree) {
			getListAdapter().remove(new FileTree((FileTree)myCurrentTree, book.File));
		} else {
			getListAdapter().replaceAll(myCurrentTree.subTrees());
		}
		getListView().invalidateViews();
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (myCurrentTree instanceof FileTree) {
			if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
				if (myCurrentTree instanceof FileTree) {
					startUpdate();
				}
				getListView().invalidateViews();
				setResult(RESULT_DO_INVALIDATE_VIEWS);
			} else if (requestCode == BOOK_INFO_REQUEST) {
				getListView().invalidateViews();
			}
		} else {
			super.onActivityResult(requestCode, returnCode, intent);
		}
	} 

	private void startUpdate() {
		new Thread(new Runnable() {
			public void run() {
				myCurrentTree.waitForOpening();
				getListAdapter().replaceAll(myCurrentTree.subTrees());
			}
		}).start();
	}
}
