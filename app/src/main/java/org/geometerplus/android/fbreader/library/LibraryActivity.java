/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.*;
import org.geometerplus.android.fbreader.*;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener<Book> {
	static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile RootTree myRootTree;
	private Book mySelectedBook;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mySelectedBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);

		new LibraryTreeAdapter(this);

		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);

		deleteRootTree();

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(!myCollection.status().IsComplete);
				myRootTree = new RootTree(myCollection, PluginCollection.Instance(Paths.systemInfo(LibraryActivity.this)));
				myCollection.addListener(LibraryActivity.this);
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
			myCollection.removeListener(this);
			myCollection.unbind();
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
		final LibraryTree tree = (LibraryTree)getTreeAdapter().getItem(position);
		if (tree instanceof ExternalViewTree) {
			runOrInstallExternalView(true);
		} else {
			final Book book = tree.getBook();
			if (book != null) {
				showBookInfo(book);
			} else {
				openTree(tree);
			}
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

	private interface ContextItemId {
		int OpenBook              = 0;
		int ShowBookInfo          = 1;
		int ShareBook             = 2;
		int AddToFavorites        = 3;
		int RemoveFromFavorites   = 4;
		int MarkAsRead            = 5;
		int MarkAsUnread          = 6;
		int DeleteBook            = 7;
		int UploadAgain           = 8;
		int TryAgain              = 9;
	}
	private interface OptionsItemId {
		int Search                = 0;
		int Rescan                = 1;
		int UploadAgain           = 2;
		int TryAgain              = 3;
		int DeleteAll             = 4;
		int ExternalView          = 5;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getTreeAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book);
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = LibraryTree.resource();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, ContextItemId.OpenBook, 0, resource.getResource("openBook").getValue());
		menu.add(0, ContextItemId.ShowBookInfo, 0, resource.getResource("showBookInfo").getValue());
		if (BookUtil.fileByBook(book).getPhysicalFile() != null) {
			menu.add(0, ContextItemId.ShareBook, 0, resource.getResource("shareBook").getValue());
		}
		if (book.hasLabel(Book.FAVORITE_LABEL)) {
			menu.add(0, ContextItemId.RemoveFromFavorites, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ContextItemId.AddToFavorites, 0, resource.getResource("addToFavorites").getValue());
		}
		if (book.hasLabel(Book.READ_LABEL)) {
			menu.add(0, ContextItemId.MarkAsUnread, 0, resource.getResource("markAsUnread").getValue());
		} else {
			menu.add(0, ContextItemId.MarkAsRead, 0, resource.getResource("markAsRead").getValue());
		}
		if (myCollection.canRemoveBook(book, true)) {
			menu.add(0, ContextItemId.DeleteBook, 0, resource.getResource("deleteBook").getValue());
		}
		if (book.hasLabel(Book.SYNC_DELETED_LABEL)) {
			menu.add(0, ContextItemId.UploadAgain, 0, resource.getResource("uploadAgain").getValue());
		}
		if (book.hasLabel(Book.SYNC_FAILURE_LABEL)) {
			menu.add(0, ContextItemId.TryAgain, 0, resource.getResource("tryAgain").getValue());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getTreeAdapter().getItem(position)).getBook();
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	private void syncAgain(Book book) {
		book.removeLabel(Book.SYNC_FAILURE_LABEL);
		book.removeLabel(Book.SYNC_DELETED_LABEL);
		book.addNewLabel(Book.SYNC_TOSYNC_LABEL);
		myCollection.saveBook(book);
	}

	private boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case ContextItemId.OpenBook:
				FBReader.openBookActivity(this, book, null);
				return true;
			case ContextItemId.ShowBookInfo:
				showBookInfo(book);
				return true;
			case ContextItemId.ShareBook:
				FBUtil.shareBook(this, book);
				return true;
			case ContextItemId.AddToFavorites:
				book.addNewLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				return true;
			case ContextItemId.RemoveFromFavorites:
				book.removeLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
				return true;
			case ContextItemId.MarkAsRead:
				book.addNewLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.MarkAsUnread:
				book.removeLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.DeleteBook:
				tryToDeleteBook(book);
				return true;
			case ContextItemId.UploadAgain:
			case ContextItemId.TryAgain:
				syncAgain(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
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
		addMenuItem(menu, OptionsItemId.Search, "localSearch", R.drawable.ic_menu_search);
		addMenuItem(menu, OptionsItemId.Rescan, "rescan", R.drawable.ic_menu_refresh);
		addMenuItem(menu, OptionsItemId.UploadAgain, "uploadAgain", -1);
		addMenuItem(menu, OptionsItemId.TryAgain, "tryAgain", -1);
		addMenuItem(menu, OptionsItemId.DeleteAll, "deleteAll", -1);
		if (Build.VERSION.SDK_INT >= 9) {
			addMenuItem(menu, OptionsItemId.ExternalView, "bookshelfView", -1);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean enableUploadAgain = false;
		boolean enableTryAgain = false;
		boolean enableDeleteAll = false;
		final LibraryTree tree = getCurrentTree();
		if (tree instanceof SyncLabelTree) {
			final String label = ((SyncLabelTree)tree).Label;
			if (Book.SYNC_DELETED_LABEL.equals(label)) {
				enableUploadAgain = true;
				enableDeleteAll = true;
			} else if (Book.SYNC_FAILURE_LABEL.equals(label)) {
				enableTryAgain = true;
			}
		}

		final MenuItem rescanItem = menu.findItem(OptionsItemId.Rescan);
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				rescanItem.setEnabled(myCollection.status().IsComplete);
			}
		});
		rescanItem.setVisible(tree == myRootTree);
		menu.findItem(OptionsItemId.UploadAgain).setVisible(enableUploadAgain);
		menu.findItem(OptionsItemId.TryAgain).setVisible(enableTryAgain);
		menu.findItem(OptionsItemId.DeleteAll).setVisible(enableDeleteAll);

		return true;
	}

	private MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId) {
		final String label = LibraryTree.resource().getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		if (iconId != -1) {
			item.setIcon(iconId);
		}
		return item;
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case OptionsItemId.Search:
				return onSearchRequested();
			case OptionsItemId.Rescan:
				if (myCollection.status().IsComplete) {
					myCollection.reset(true);
					openTree(myRootTree);
				}
				return true;
			case OptionsItemId.UploadAgain:
			case OptionsItemId.TryAgain:
				for (FBTree tree : getCurrentTree().subtrees()) {
					if (tree instanceof BookTree) {
						syncAgain(((BookTree)tree).Book);
					}
				}
				getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
				return true;
			case OptionsItemId.DeleteAll:
			{
				final List<Book> books = new LinkedList<Book>();
				for (FBTree tree : getCurrentTree().subtrees()) {
					if (tree instanceof BookTree) {
						books.add(((BookTree)tree).Book);
					}
				}
				tryToDeleteBooks(books);
				return true;
			}
			case OptionsItemId.ExternalView:
				runOrInstallExternalView(true);
				return true;
			default:
				return true;
		}
	}

	private void runOrInstallExternalView(boolean install) {
		try {
			startActivity(new Intent(FBReaderIntents.Action.EXTERNAL_LIBRARY));
			finish();
		} catch (ActivityNotFoundException e) {
			if (install) {
				PackageUtil.installFromMarket(this, "org.fbreader.plugin.library");
			}
		}
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final List<Book> myBooks;

		BookDeleter(List<Book> books) {
			myBooks = new ArrayList<Book>(books);
		}

		public void onClick(DialogInterface dialog, int which) {
			if (getCurrentTree() instanceof FileTree) {
				for (Book book : myBooks) {
					getTreeAdapter().remove(new FileTree(
						(FileTree)getCurrentTree(),
						BookUtil.fileByBook(book)
					));
					myCollection.removeBook(book, true);
				}
				getListView().invalidateViews();
			} else {
				boolean doReplace = false;
				for (Book book : myBooks) {
					doReplace |= getCurrentTree().onBookEvent(BookEvent.Removed, book);
					myCollection.removeBook(book, true);
				}
				if (doReplace) {
					getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
			}
		}
	}

	private void tryToDeleteBooks(List<Book> books) {
		final int size = books.size();
		if (size == 0) {
			return;
		}
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource(
			size == 1 ? "deleteBookBox" : "deleteMultipleBookBox"
		);
		final String title = size == 1
			? books.get(0).getTitle()
			: boxResource.getResource("title").getValue();
		final String message =
			boxResource.getResource("message").getValue(size).replaceAll("%s", String.valueOf(size));
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(books))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void tryToDeleteBook(Book book) {
		tryToDeleteBooks(Collections.singletonList(book));
	}

	private void startBookSearch(final String pattern) {
		BookSearchPatternOption.setValue(pattern);

		final Thread searcher = new Thread("Library.searchBooks") {
			public void run() {
				final SearchResultsTree oldSearchResults = myRootTree.getSearchResultsTree();

				if (oldSearchResults != null && pattern.equals(oldSearchResults.Pattern)) {
					onSearchEvent(true);
				} else if (myCollection.hasBooks(new Filter.ByPattern(pattern))) {
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
					UIMessageUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
				}
			}
		});
	}

	public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsComplete);
	}
}
