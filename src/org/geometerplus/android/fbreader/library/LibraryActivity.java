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

import android.app.SearchManager;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

abstract class LibraryActivity extends BaseActivity implements MenuItem.OnMenuItemClickListener {
	static final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			if (myCurrentTree instanceof FileTree) {
				startUpdate();
			}
			getListView().invalidateViews();
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		} else if (requestCode == BOOK_INFO_REQUEST) {
			getListView().invalidateViews();
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

	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	protected static final String ACTION_FOUND = "fbreader.library.intent.FOUND";

	protected boolean runSearch(Intent intent) {
	   	final String pattern = intent.getStringExtra(SearchManager.QUERY);
		if (pattern == null || pattern.length() == 0) {
			return false;
		}
		BookSearchPatternOption.setValue(pattern);
		return LibraryInstance.searchBooks(pattern) != null;
	}

	protected void showNotFoundToast() {
		UIUtil.showErrorMessage(this, "bookNotFound");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        addMenuItem(menu, 1, "localSearch", R.drawable.ic_menu_search);
        return true;
    }

    private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
        final String label = Library.resource().getResource("menu").getResource(resourceKey).getValue();
        final MenuItem item = menu.add(0, index, Menu.NONE, label);
        item.setOnMenuItemClickListener(this);
        item.setIcon(iconId);
        return item;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                return onSearchRequested();
            default:
                return true;
        }
    }

	@Override
	protected int getCoverResourceId(FBTree tree) {
		if (((LibraryTree)tree).getBook() != null) {
			return R.drawable.ic_list_library_book;
		} else if (tree instanceof FirstLevelTree) {
			final String id = tree.getUniqueKey().Id;
			if (Library.ROOT_FAVORITES.equals(id)) {
				return R.drawable.ic_list_library_favorites;
			} else if (Library.ROOT_RECENT.equals(id)) {
				return R.drawable.ic_list_library_recent;
			} else if (Library.ROOT_BY_AUTHOR.equals(id)) {
				return R.drawable.ic_list_library_authors;
			} else if (Library.ROOT_BY_TITLE.equals(id)) {
				return R.drawable.ic_list_library_books;
			} else if (Library.ROOT_BY_TAG.equals(id)) {
				return R.drawable.ic_list_library_tags;
			} else if (Library.ROOT_FILE_TREE.equals(id)) {
				return R.drawable.ic_list_library_folder;
			}
		} else if (tree instanceof FileTree) {
			final ZLFile file = ((FileTree)tree).getFile();
			if (file.isArchive()) {
				return R.drawable.ic_list_library_zip;
			} else if (file.isDirectory() && file.isReadable()) {
				return R.drawable.ic_list_library_folder;
			} else {
				return R.drawable.ic_list_library_permission_denied;
			}
		} else if (tree instanceof AuthorTree) {
			return R.drawable.ic_list_library_author;
		} else if (tree instanceof TagTree) {
			return R.drawable.ic_list_library_tag;
		}

		return R.drawable.ic_list_library_books;
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
}
