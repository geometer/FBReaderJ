/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.*;
import org.geometerplus.android.fbreader.*;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener {
	static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";

	private volatile RootTree myRootTree;
	private Book mySelectedBook;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mySelectedBook = FBReaderIntents.getBookExtra(getIntent());

		new LibraryTreeAdapter(this);

		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);

		deleteRootTree();

		final BookCollectionShadow collection = new BookCollectionShadow();
		collection.bindToService(this, new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(!collection.status().IsCompleted);
				myRootTree = new RootTree(collection);
				collection.addListener(LibraryActivity.this);
				init(getIntent());
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (START_SEARCH_ACTION.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				startBookSearch(pattern);
			}
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
	}

	private synchronized void deleteRootTree() {
		if (myRootTree != null) {
			myRootTree.Collection.removeListener(this);
			((BookCollectionShadow)myRootTree.Collection).unbind();
			myRootTree = null;
		}
	}

	@Override
	protected void onDestroy() {
		deleteRootTree();
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
	private void showBookInfo(Book book) {
		final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
		FBReaderIntents.putBookExtra(intent, book);
		OrientationUtil.startActivity(this, intent);
	}

	//
	// Search
	//
	private final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	private void openSearchResults() {
		final LibraryTree tree = myRootTree.getSearchResultsTree();
		if (tree != null) {
			openTree(tree);
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(BookSearchPatternOption.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, LibrarySearchActivity.class, BookSearchPatternOption.getValue(), null);
		}
		return true;
	}

	//
	// Context menu
	//
	private interface ItemId {
		int OpenBook              = 0;
		int ShowBookInfo          = 1;
		int ShareBook             = 2;
		int AddToFavorites        = 3;
		int RemoveFromFavorites   = 4;
		int MarkAsRead            = 5;
		int MarkAsUnread          = 6;
		int DeleteBook            = 7;
		int SyncAgain             = 8;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book);
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = LibraryTree.resource();
		final List<String> labels = book.labels();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, ItemId.OpenBook, 0, resource.getResource("openBook").getValue());
		menu.add(0, ItemId.ShowBookInfo, 0, resource.getResource("showBookInfo").getValue());
		if (book.File.getPhysicalFile() != null) {
			menu.add(0, ItemId.ShareBook, 0, resource.getResource("shareBook").getValue());
		}
		if (labels.contains(Book.FAVORITE_LABEL)) {
			menu.add(0, ItemId.RemoveFromFavorites, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ItemId.AddToFavorites, 0, resource.getResource("addToFavorites").getValue());
		}
		if (labels.contains(Book.READ_LABEL)) {
			menu.add(0, ItemId.MarkAsUnread, 0, resource.getResource("markAsUnread").getValue());
		} else {
			menu.add(0, ItemId.MarkAsRead, 0, resource.getResource("markAsRead").getValue());
		}
		if (BookUtil.canRemoveBookFile(book)) {
			menu.add(0, ItemId.DeleteBook, 0, resource.getResource("deleteBook").getValue());
		}
		if (labels.contains(Book.SYNC_FAILURE_LABEL) || labels.contains(Book.SYNC_DELETED_LABEL)) {
			menu.add(0, ItemId.SyncAgain, 0, resource.getResource("syncAgain").getValue());
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
			case ItemId.OpenBook:
				FBReader.openBookActivity(this, book, null);
				return true;
			case ItemId.ShowBookInfo:
				showBookInfo(book);
				return true;
			case ItemId.ShareBook:
				FBUtil.shareBook(this, book);
				return true;
			case ItemId.AddToFavorites:
				book.addLabel(Book.FAVORITE_LABEL);
				myRootTree.Collection.saveBook(book);
				return true;
			case ItemId.RemoveFromFavorites:
				book.removeLabel(Book.FAVORITE_LABEL);
				myRootTree.Collection.saveBook(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
				return true;
			case ItemId.MarkAsRead:
				book.addLabel(Book.READ_LABEL);
				myRootTree.Collection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ItemId.MarkAsUnread:
				book.removeLabel(Book.READ_LABEL);
				myRootTree.Collection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ItemId.DeleteBook:
				tryToDeleteBook(book);
				return true;
			case ItemId.SyncAgain:
				book.removeLabel(Book.SYNC_FAILURE_LABEL);
				book.removeLabel(Book.SYNC_DELETED_LABEL);
				book.addLabel(Book.SYNC_TOSYNC_LABEL);
				myRootTree.Collection.saveBook(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
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
		addMenuItem(menu, 2, "rescan", R.drawable.ic_menu_refresh);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(2).setEnabled(myRootTree.Collection.status().IsCompleted);
		return true;
	}

	private MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId) {
		final String label = LibraryTree.resource().getResource("menu").getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		return item;
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			case 2:
				if (myRootTree.Collection.status().IsCompleted) {
					((BookCollectionShadow)myRootTree.Collection).reset(true);
					openTree(myRootTree);
				}
				return true;
			default:
				return true;
		}
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;

		BookDeleter(Book book) {
			myBook = book;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (getCurrentTree() instanceof FileTree) {
				getListAdapter().remove(new FileTree((FileTree)getCurrentTree(), myBook.File));
				getListView().invalidateViews();
			} else if (getCurrentTree().onBookEvent(BookEvent.Removed, myBook)) {
				getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
			}

			myRootTree.Collection.removeBook(myBook, true);
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
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void startBookSearch(final String pattern) {
		BookSearchPatternOption.setValue(pattern);

		final Thread searcher = new Thread("Library.searchBooks") {
			public void run() {
				final SearchResultsTree oldSearchResults = myRootTree.getSearchResultsTree();

				if (oldSearchResults != null && pattern.equals(oldSearchResults.Pattern)) {
					onSearchEvent(true);
				} else if (myRootTree.Collection.hasBooks(new Filter.ByPattern(pattern))) {
					if (oldSearchResults != null) {
						oldSearchResults.removeSelf();
					}
					myRootTree.createSearchResultsTree(pattern);
					onSearchEvent(true);
				} else {
					onSearchEvent(false);
				}
			}
		};
		searcher.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		searcher.start();
	}

	private void onSearchEvent(final boolean found) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (found) {
					openSearchResults();
				} else {
					UIUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
				}
			}
		});
	}

	public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsCompleted);
	}
}
