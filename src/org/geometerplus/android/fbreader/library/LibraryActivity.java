/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.fbreader.*;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, Library.ChangeListener, IBookCollection.Listener {
	static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";

	private Library myLibrary;

	private Book mySelectedBook;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (myLibrary == null) {
			myLibrary = new Library(new BookCollectionShadow());
			myLibrary.addChangeListener(this);
			myLibrary.Collection.addListener(this);
		}

		mySelectedBook =
			SerializerUtil.deserializeBook(getIntent().getStringExtra(FBReader.BOOK_KEY));

		new LibraryTreeAdapter(this);

		init(getIntent());

		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		((BookCollectionShadow)myLibrary.Collection).bindToService(this, null);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (START_SEARCH_ACTION.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				BookSearchPatternOption.setValue(pattern);
				myLibrary.startBookSearch(pattern);
			}
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	public void onResume() {
	  	super.onResume();
		setProgressBarIndeterminateVisibility(!myLibrary.isUpToDate());
	}

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myLibrary.getLibraryTree(key) : myLibrary.getRootTree();
	}

	@Override
	protected void onStop() {
		((BookCollectionShadow)myLibrary.Collection).unbind();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		myLibrary.Collection.removeListener(this);
		myLibrary.removeChangeListener(this);
		myLibrary = null;
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
		final Book book = tree.getBook();
		if (book != null) {
			showBookInfo(book);
		} else {
			openTree(tree);
		}
	}

	//
	// show BookInfoActivity
	//
	private static final int BOOK_INFO_REQUEST = 1;

	private void showBookInfo(Book book) {
		OrientationUtil.startActivityForResult(
			this,
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(FBReader.BOOK_KEY, SerializerUtil.serialize(book)),
			BOOK_INFO_REQUEST
		);
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == BOOK_INFO_REQUEST) {
			final Book book = BookInfoActivity.bookByIntent(intent);
			((BookCollectionShadow)myLibrary.Collection).bindToService(this, new Runnable() {
				public void run() {
					myLibrary.refreshBookInfo(book);
					if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
						getListView().invalidateViews();
					}
				}
			});
		} else {
			super.onActivityResult(requestCode, returnCode, intent);
		}
	}

	//
	// Search
	//
	private final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	private void openSearchResults() {
		final FBTree tree = myLibrary.getRootTree().getSubTree(LibraryTree.ROOT_FOUND);
		if (tree != null) {
			openTree(tree);
		}
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	//
	// Context menu
	//
	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int SHARE_BOOK_ITEM_ID = 2;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 3;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 4;
	private static final int DELETE_BOOK_ITEM_ID = 5;

	@Override
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
		if (book.File.getPhysicalFile() != null) {
			menu.add(0, SHARE_BOOK_ITEM_ID, 0, resource.getResource("shareBook").getValue());
		}
		if (myLibrary.Collection.isFavorite(book)) {
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, resource.getResource("addToFavorites").getValue());
		}
		if (myLibrary.canRemoveBookFile(book)) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
		}
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
				FBReader.openBookActivity(this, book, null);
				return true;
			case SHOW_BOOK_INFO_ITEM_ID:
				showBookInfo(book);
				return true;
			case SHARE_BOOK_ITEM_ID:
				FBUtil.shareBook(this, book);
				return true;
			case ADD_TO_FAVORITES_ITEM_ID:
				myLibrary.Collection.setBookFavorite(book, true);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				myLibrary.Collection.setBookFavorite(book, false);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getListAdapter().replaceAll(getCurrentTree().subTrees());
					getListView().invalidateViews();
				}
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}

	//
	// Options menu
	//

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

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;
		private final int myMode;

		BookDeleter(Book book, int removeMode) {
			myBook = book;
			myMode = removeMode;
		}

		public void onClick(DialogInterface dialog, int which) {
			deleteBook(myBook, myMode);
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

	private void deleteBook(Book book, int mode) {
		if (getCurrentTree() instanceof FileTree) {
			getListAdapter().remove(new FileTree((FileTree)getCurrentTree(), book.File));
			getListView().invalidateViews();
		} else if (getCurrentTree().onBookEvent(BookEvent.Removed, book)) {
			getListAdapter().replaceAll(getCurrentTree().subTrees());
			getListView().invalidateViews();
		}
		myLibrary.removeBook(book, mode);
	}

	public void onLibraryChanged(final Code code) {
		runOnUiThread(new Runnable() {
			public void run() {
				switch (code) {
					default:
						getListAdapter().replaceAll(getCurrentTree().subTrees());
						break;
					case StatusChanged:
						setProgressBarIndeterminateVisibility(!myLibrary.isUpToDate());
						break;
					case Found:
						openSearchResults();
						break;
					case NotFound:
						UIUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
						break;
				}
			}
		});
	}

	public void onBookEvent(BookEvent event, Book book) {
		getCurrentTree().onBookEvent(event, book);
	}

	public void onBuildEvent(BuildEvent event) {
	}
}
