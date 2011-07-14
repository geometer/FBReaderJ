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

import java.util.Map;
import java.util.HashMap;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.BookInfoActivity;
import org.geometerplus.android.fbreader.SQLiteBooksDatabase;

abstract class BaseActivity extends ListActivity implements View.OnCreateContextMenuListener {
	private static class FBTreeInfo {
		final int CoverResourceId;
		final Runnable Action;

		FBTreeInfo(int coverResourceId, Runnable action) {
			CoverResourceId = coverResourceId;
			Action = action;
		}
	};

	static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";

	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
	private static final int DELETE_BOOK_ITEM_ID = 4;

	protected static final int CHILD_LIST_REQUEST = 0;
	protected static final int BOOK_INFO_REQUEST = 1;

	protected static final int RESULT_DONT_INVALIDATE_VIEWS = 0;
	protected static final int RESULT_DO_INVALIDATE_VIEWS = 1;

	static BooksDatabase DatabaseInstance;
	static Library LibraryInstance;

	protected String mySelectedBookPath;
	private Book mySelectedBook;
	protected LibraryTree myCurrentTree;

	private final Map<FBTree,FBTreeInfo> myInfoMap = new HashMap<FBTree,FBTreeInfo>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		DatabaseInstance = SQLiteBooksDatabase.Instance();
		if (DatabaseInstance == null) {
			DatabaseInstance = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (LibraryInstance == null) {
			LibraryInstance = new Library();
			startService(new Intent(getApplicationContext(), InitializationService.class));
		}

		final FBTree.Key key = (FBTree.Key)getIntent().getSerializableExtra(TREE_KEY_KEY);
		if (key != null) {
			myCurrentTree = LibraryInstance.getLibraryTree(key);
			setTitle(myCurrentTree.getTreeTitle());
		} else {
			myCurrentTree = null;
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
        
		mySelectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
		mySelectedBook = null;
		if (mySelectedBookPath != null) {
			final ZLFile file = ZLFile.createFileByPath(mySelectedBookPath);
			if (file != null) {
				mySelectedBook = Book.getByFile(file);
			}
		}
        
		setResult(RESULT_DONT_INVALIDATE_VIEWS);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public ListAdapter getListAdapter() {
		return (ListAdapter)super.getListAdapter();
	}

	protected void addFBTreeWithInfo(FBTree tree, int coverResourceId, Runnable action) {
		getListAdapter().add(tree);
		myInfoMap.put(tree, new FBTreeInfo(coverResourceId, action));
	}

	int getCoverResourceId(FBTree tree) {
		final FBTreeInfo info = myInfoMap.get(tree);
		if (info != null && info.CoverResourceId != -1) {
			return info.CoverResourceId;
		} else if (((LibraryTree)tree).getBook() != null) {
			return R.drawable.ic_list_library_book;
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
		} else {
			return R.drawable.ic_list_library_books;
		}
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
		final Book book = tree.getBook();
		if (book != null) {
			showBookInfo(book);
		} else if (tree instanceof FileTree) {
			new OpenTreeRunnable(tree, FileManager.class).run();
		} else {
			final FBTreeInfo info = myInfoMap.get(tree);
			if (info != null && info.Action != null) {
				info.Action.run();
			} else {
				new OpenTreeRunnable(tree, LibraryTreeActivity.class).run();
			}
		}
	}

	boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	protected void openBook(Book book) {
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		);
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book); 
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = Library.resource();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, resource.getResource("showBookInfo").getValue());
		if (LibraryInstance.isBookInFavorites(book)) {
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, resource.getResource("addToFavorites").getValue());
		}
		if ((LibraryInstance.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
        }
	}

	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;
		private final int myMode;

		BookDeleter(Book book, int removeMode) {
			myBook = book;
			myMode = removeMode;
		}

		public void onClick(DialogInterface dialog, int which) {
			deleteBook(myBook, myMode);
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		}
	}

	private void tryToDeleteBook(Book book) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
			.setTitle(book.getTitle())
			.setMessage(boxResource.getResource("message").getValue())
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book, Library.REMOVE_FROM_DISK))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	protected void deleteBook(Book book, int mode) {
		LibraryInstance.removeBook(book, mode);
	}

	protected void showBookInfo(Book book) {
		startActivityForResult(
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, book.File.getPath()),
			BOOK_INFO_REQUEST
		);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	private boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case OPEN_BOOK_ITEM_ID:
				openBook(book);
				return true;
			case SHOW_BOOK_INFO_ITEM_ID:
				showBookInfo(book);
				return true;
			case ADD_TO_FAVORITES_ITEM_ID:
				LibraryInstance.addBookToFavorites(book);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				LibraryInstance.removeBookFromFavorites(book);
				getListView().invalidateViews();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}

	protected class OpenTreeRunnable implements Runnable {
		private final FBTree myTree;
		private final Class<?> myActivityClass;

		public OpenTreeRunnable(FBTree tree, Class<?> activityClass) {
			myTree = tree;
			myActivityClass = activityClass;
		}

		public void run() {
			switch (myTree.getOpeningStatus()) {
				case WAIT_FOR_OPEN:
				case ALWAYS_RELOAD_BEFORE_OPENING:
					final String messageKey = myTree.getOpeningStatusMessage();
					if (messageKey != null) {
						UIUtil.runWithMessage(
							BaseActivity.this, messageKey,
							new Runnable() {
								public void run() {
									myTree.waitForOpening();
								}
							},
							new Runnable() {
								public void run() {
									openTree();
								}
							}
						);
					} else {
						myTree.waitForOpening();
						openTree();
					}
					break;
				default:
					openTree();
					break;
			}
		}

		protected void openTree() {
			switch (myTree.getOpeningStatus()) {
				case READY_TO_OPEN:
				case ALWAYS_RELOAD_BEFORE_OPENING:
					startActivityForResult(
						new Intent(BaseActivity.this, myActivityClass)
							.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
							.putExtra(TREE_KEY_KEY, myTree.getUniqueKey()),
						CHILD_LIST_REQUEST
					);
					break;
				case CANNOT_OPEN:
					UIUtil.showErrorMessage(BaseActivity.this, myTree.getOpeningStatusMessage());
					break;
			}
		}
	}
}
