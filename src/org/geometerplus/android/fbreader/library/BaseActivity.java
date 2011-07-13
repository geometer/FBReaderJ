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

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.BookInfoActivity;
import org.geometerplus.android.fbreader.SQLiteBooksDatabase;

abstract class BaseActivity extends ListActivity {
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
	protected FBTree.Key myTreeKey;

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

		myTreeKey = (FBTree.Key)getIntent().getSerializableExtra(TREE_KEY_KEY);
        
		mySelectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
		mySelectedBook = null;
		if (mySelectedBookPath != null) {
			final ZLFile file = ZLFile.createFileByPath(mySelectedBookPath);
			if (file != null) {
				mySelectedBook = Book.getByFile(file);
			}
		}
        
		setResult(RESULT_DONT_INVALIDATE_VIEWS);
	}

	@Override
	public ListAdapter getListAdapter() {
		return (ListAdapter)super.getListAdapter();
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final FBTree tree = getListAdapter().getItem(position);
		if (tree instanceof TopLevelTree) {
			((TopLevelTree)tree).run();
		} else if (tree instanceof FileItem) {
			final FileItem item = (FileItem)tree;
			final ZLFile file = item.getFile();
			final Book book = item.getBook();
			if (book != null) {
				showBookInfo(book);
			} else if (!file.isReadable()) {
				UIUtil.showErrorMessage(this, "permissionDenied");
			} else if (file.isDirectory() || file.isArchive()) {
				startActivityForResult(
					new Intent(this, FileManager.class)
						.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
						.putExtra(TREE_KEY_KEY, tree.getUniqueKey()),
					CHILD_LIST_REQUEST
				);
			}
		} else if (tree instanceof BookTree) {
			showBookInfo(((BookTree)tree).Book);
		} else {
			new OpenTreeRunnable(LibraryInstance, tree.getUniqueKey()).run();
		}
	}

	boolean isTreeSelected(FBTree tree) {
		if (mySelectedBook == null) {
			return false;
		}

		if (tree instanceof BookTree) {
			return mySelectedBook.equals(((BookTree)tree).Book);
		} else if (tree instanceof AuthorTree) {
			return mySelectedBook.authors().contains(((AuthorTree)tree).Author);
		} else if (tree instanceof TitleTree) {
			final String title = mySelectedBook.getTitle();
			return tree != null && title.trim().startsWith(((TitleTree)tree).Title);
		} else if (tree instanceof SeriesTree) {
			final SeriesInfo info = mySelectedBook.getSeriesInfo();
			final String series = ((SeriesTree)tree).Series;
			return info != null && series != null && series.equals(info.Name);
		} else if (tree instanceof TagTree) {
			final Tag tag = ((TagTree)tree).Tag;
			for (Tag t : mySelectedBook.tags()) {
				for (; t != null; t = t.Parent) {
					if (t == tag) {
						return true;
					}
				}
			}
		} else if (tree instanceof FileItem) {
			final FileItem item = (FileItem)tree;
			if (!item.isSelectable()) {
				return false;
			}
			final ZLFile file = item.getFile();
			final String path = file.getPath();
			if (mySelectedBookPath.equals(path)) {
				return true;
			}
        
			String prefix = path;
			if (file.isDirectory()) {
				if (!prefix.endsWith("/")) {
					prefix += '/';
				}
			} else if (file.isArchive()) {
				prefix += ':';
			} else {
				return false;
			}
			return mySelectedBookPath.startsWith(prefix);
		}

		return false;
	}

	protected void openBook(Book book) {
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		);
	}

	protected void createBookContextMenu(ContextMenu menu, Book book) {
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
		final FBTree tree = getListAdapter().getItem(position);
		if (tree instanceof BookTree) {
			return onContextItemSelected(item.getItemId(), ((BookTree)tree).Book);
		} else if (tree instanceof FileItem) {
			final Book book = ((FileItem)tree).getBook(); 
			if (book != null) {
				return onContextItemSelected(item.getItemId(), book);
			}
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

	protected class StartTreeActivityRunnable implements Runnable {
		private final FBTree.Key myTreeKey;

		public StartTreeActivityRunnable(FBTree.Key key) {
			myTreeKey = key;
		}

		public void run() {
			startActivityForResult(
				new Intent(BaseActivity.this, LibraryTreeActivity.class)
					.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					.putExtra(TREE_KEY_KEY, myTreeKey),
				CHILD_LIST_REQUEST
			);
		}
	}

	protected class OpenTreeRunnable implements Runnable {
		private final Library myLibrary;
		private final Runnable myPostRunnable;

		public OpenTreeRunnable(Library library, FBTree.Key key) {
			this(library, new StartTreeActivityRunnable(key));
		}

		public OpenTreeRunnable(Library library, Runnable postRunnable) {
			myLibrary = library;
			myPostRunnable = postRunnable;
		}

		public void run() {
			if (myLibrary == null) {
				return;
			}
			if (myLibrary.hasState(Library.STATE_FULLY_INITIALIZED)) {
				myPostRunnable.run();
			} else {
				UIUtil.runWithMessage(BaseActivity.this, "loadingBookList",
				new Runnable() {
					public void run() {
						myLibrary.waitForState(Library.STATE_FULLY_INITIALIZED);
					}
				},
				myPostRunnable);
			}
		}
	}
}
