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

import java.io.File;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.AndroidUtil;

public class LibraryTabActivity extends TabActivity implements MenuItem.OnMenuItemClickListener {
	public static final String CURRENT_BOOK_PATH_KEY = "CurrentBookPath";

	static LibraryTabActivity Instance;

	final ZLStringOption BookSearchPatternOption = new ZLStringOption("BookSearch", "Pattern", "");
	final ZLStringOption mySelectedTabOption = new ZLStringOption("TabActivity", "SelectedTab", "");

	private final ZLResource myResource = ZLResource.resource("libraryView");
	private String myCurrentBookPath;

	private Library myLibrary;

	Library library() {
		return myLibrary;
	}

	private ListView createTab(String tag, int viewId, int iconId) {
		final TabHost host = getTabHost();
		final String label = myResource.getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label, getResources().getDrawable(iconId)).setContent(viewId));
		return (ListView)findViewById(viewId);
	}

	private void createDefaultTabs() {
		new LibraryAdapter(createTab("byAuthor", R.id.by_author, R.drawable.ic_tab_library_author), myLibrary.byAuthor(), Type.TREE);
		new LibraryAdapter(createTab("byTag", R.id.by_tag, R.drawable.ic_tab_library_tag), myLibrary.byTag(), Type.TREE);
		new LibraryAdapter(createTab("recent", R.id.recent, R.drawable.ic_tab_library_recent), myLibrary.recentBooks(), Type.FLAT);
		findViewById(R.id.search_results).setVisibility(View.GONE);
	}

	private boolean isSelectedItem(FBTree tree) {
		return
			(tree instanceof BookTree) &&
			((BookTree)tree).Book.File.getPath().equals(myCurrentBookPath);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase("LIBRARY");
		}

		if (myLibrary == null) {
			myLibrary = new Library();
		}
		final Runnable action = new Runnable() {
			public void run() {
				myLibrary.clear();
				myLibrary.synchronize();
			}
		};
		System.err.println("before");
        action.run();
		//AndroidUtil.wait("loadingBookList", action, this);
		System.err.println("after");

		final Intent intent = getIntent();
		myCurrentBookPath = intent.getStringExtra(CURRENT_BOOK_PATH_KEY);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.library, host.getTabContentView(), true);

		createDefaultTabs();

		host.setCurrentTabByTag(mySelectedTabOption.getValue());
	}

	private LibraryAdapter mySearchResultsAdapter;
	void showSearchResultsTab(LibraryTree tree) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter =
				new LibraryAdapter(createTab("searchResults", R.id.search_results, R.drawable.ic_tab_library_results), tree, Type.FLAT);
		} else {
			mySearchResultsAdapter.resetTree(tree);
		}
		getTabHost().setCurrentTabByTag("searchResults");
	}

	@Override
	public void onResume() {
		super.onResume();
		Instance = this;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		mySelectedTabOption.setValue(getTabHost().getCurrentTabTag());
		Instance = null;
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, 1, "localSearch", R.drawable.ic_menu_search);
		return true;
	}

	private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
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
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	interface Type {
		int TREE = 0;
		int FLAT = 1;
		int NETWORK = 2;
	}

	private final class LibraryAdapter extends ZLTreeAdapter {
		private final LibraryTree myLibraryTree;
			
		private final int myType;

		LibraryAdapter(ListView view, LibraryTree tree, int type) {
			super(view, tree);
			myLibraryTree = tree;
			myType = type;
			selectItem(findFirstSelectedItem());
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final LibraryTree tree = (LibraryTree)getItem(position);
			if (tree instanceof BookTree) {
				menu.setHeaderTitle(tree.getName());
				final ZLResource resource = ZLResource.resource("libraryView");
				menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
				if ((myLibrary.getRemoveBookMode(((BookTree)tree).Book)
						& Library.REMOVE_FROM_DISK) != 0) {
					menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
				}
			}
		}

		private ZLTree<?> findFirstSelectedItem() {
			if (myCurrentBookPath == null) {
				return null;
			}
			for (FBTree tree : myLibraryTree) {
				if (isSelectedItem(tree)) {
					return tree;
				}
			}
			return null;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
			final LibraryTree tree = (LibraryTree)getItem(position);
			if (isSelectedItem(tree)) {
				view.setBackgroundColor(0xff808080);
			} else {
				view.setBackgroundColor(0);
			}
			final ImageView iconView = (ImageView)view.findViewById(R.id.library_tree_item_icon);
			switch (myType) {
				case Type.FLAT:
					iconView.setVisibility(View.GONE);
					break;
				case Type.TREE:
					setIcon(iconView, tree);
					break;
				case Type.NETWORK:
					switch (position % 3) {
						case 0:
							iconView.setImageResource(R.drawable.ic_list_buy);
							break;
						case 1:
							iconView.setImageResource(R.drawable.ic_list_download);
							break;
						case 2:
							iconView.setImageResource(R.drawable.ic_list_flag);
							break;
					}
					break;
			}
			((TextView)view.findViewById(R.id.library_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(tree.getSecondString());
			return view;
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			finish();
			final Book book = ((BookTree)tree).Book;
			if (!book.File.getPath().equals(myCurrentBookPath)) {
				ZLFile physicalFile = book.File.getPhysicalFile();
				startActivity(getFBReaderIntent(physicalFile != null ? new File(physicalFile.getPath()) : null));
			}
			return true;
		}
	}

	private Intent getFBReaderIntent(final File file) {
		final Intent intent = new Intent(getApplicationContext(), FBReader.class);
		intent.setAction(Intent.ACTION_VIEW);
		if (file != null) {
			intent.setData(Uri.fromFile(file));
		} else {
			intent.setData(Uri.parse("file:///"));
		}
		return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int DELETE_BOOK_ITEM_ID = 1;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final LibraryAdapter adapter =
			(LibraryAdapter)((ListView)getTabHost().getCurrentView()).getAdapter();
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final BookTree tree = (BookTree)adapter.getItem(position);
		switch (item.getItemId()) {
			case OPEN_BOOK_ITEM_ID:
				adapter.runTreeItem(tree);
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(tree.Book);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;
		private final int myMode;

		BookDeleter(Book book, int removeMode) {
			myBook = book;
			myMode = removeMode;
		}

		private void invalidateView(View v) {
			ZLTreeAdapter adapter = (ZLTreeAdapter)((ListView)v).getAdapter();
			if (adapter != null) {
				adapter.resetTree();
			}
		}

		public void onClick(DialogInterface dialog, int which) {
			myLibrary.removeBook(myBook, myMode);

			invalidateView(findViewById(R.id.by_author));
			invalidateView(findViewById(R.id.by_tag));
			invalidateView(findViewById(R.id.recent));
			invalidateView(findViewById(R.id.search_results));
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
}
