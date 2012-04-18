/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.util.UIUtil;

public class BookmarksActivity extends TabActivity implements MenuItem.OnMenuItemClickListener {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	List<Bookmark> AllBooksBookmarks;
	private final List<Bookmark> myThisBookBookmarks = new LinkedList<Bookmark>();
	private final List<Bookmark> mySearchResults = new LinkedList<Bookmark>();

	private ListView myThisBookView;
	private ListView myAllBooksView;
	private ListView mySearchResultsView;

	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final ZLStringOption myBookmarkSearchPatternOption =
		new ZLStringOption("BookmarkSearch", "Pattern", "");

	private ListView createTab(String tag, int id) {
		final TabHost host = getTabHost();
		final String label = myResource.getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label).setContent(id));
		return (ListView)findViewById(id);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.bookmarks, host.getTabContentView(), true);

		AllBooksBookmarks = Library.Instance().allBookmarks();
		Collections.sort(AllBooksBookmarks, new Bookmark.ByTimeComparator());
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();

		if (fbreader.Model != null) {
			final long bookId = fbreader.Model.Book.getId();
			for (Bookmark bookmark : AllBooksBookmarks) {
				if (bookmark.getBookId() == bookId) {
					myThisBookBookmarks.add(bookmark);
				}
			}

			myThisBookView = createTab("thisBook", R.id.this_book);
			new BookmarksAdapter(myThisBookView, myThisBookBookmarks, true);
		} else {
			findViewById(R.id.this_book).setVisibility(View.GONE);
		}

		myAllBooksView = createTab("allBooks", R.id.all_books);
		new BookmarksAdapter(myAllBooksView, AllBooksBookmarks, false);

		findViewById(R.id.search_results).setVisibility(View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		myBookmarkSearchPatternOption.setValue(pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : AllBooksBookmarks) {
			if (ZLMiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
		if (!bookmarks.isEmpty()) {
			showSearchResultsTab(bookmarks);
		} else {
			UIUtil.showErrorMessage(this, "bookmarkNotFound");
		}
	}

	@Override
	public void onPause() {
		for (Bookmark bookmark : AllBooksBookmarks) {
			bookmark.save();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuItem item = menu.add(
			0, 1, Menu.NONE,
			myResource.getResource("menu").getResource("search").getValue()
		);
		item.setOnMenuItemClickListener(this);
		item.setIcon(R.drawable.ic_menu_search);
		return true;
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Bookmark> results) {
		if (mySearchResultsView == null) {
			mySearchResultsView = createTab("found", R.id.search_results);
			new BookmarksAdapter(mySearchResultsView, mySearchResults, false);
		} else {
			mySearchResults.clear();
		}
		mySearchResults.addAll(results);
		mySearchResultsView.invalidateViews();
		mySearchResultsView.requestLayout();
		getTabHost().setCurrentTabByTag("found");
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			default:
				return true;
		}
	}

	private void invalidateAllViews() {
		myThisBookView.invalidateViews();
		myThisBookView.requestLayout();
		myAllBooksView.invalidateViews();
		myAllBooksView.requestLayout();
		if (mySearchResultsView != null) {
			mySearchResultsView.invalidateViews();
			mySearchResultsView.requestLayout();
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final ListView view = (ListView)getTabHost().getCurrentView();
		final Bookmark bookmark = ((BookmarksAdapter)view.getAdapter()).getItem(position);
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				gotoBookmark(bookmark);
				return true;
			case EDIT_ITEM_ID:
				final Intent intent = new Intent(this, BookmarkEditActivity.class);
				startActivityForResult(intent, 1);
				// TODO: implement
				return true;
			case DELETE_ITEM_ID:
				bookmark.delete();
				myThisBookBookmarks.remove(bookmark);
				AllBooksBookmarks.remove(bookmark);
				mySearchResults.remove(bookmark);
				invalidateAllViews();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void addBookmark() {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final Bookmark bookmark = fbreader.addBookmark(20, true);
		if (bookmark != null) {
			myThisBookBookmarks.add(0, bookmark);
			AllBooksBookmarks.add(0, bookmark);
			invalidateAllViews();
		}
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.onOpen();
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final long bookId = bookmark.getBookId();
		if ((fbreader.Model == null) || (fbreader.Model.Book.getId() != bookId)) {
			final Book book = Book.getById(bookId);
			if (book != null) {
				finish();
				fbreader.openBook(book, bookmark, null);
			} else {
				UIUtil.showErrorMessage(this, "cannotOpenBook");
			}
		} else {
			finish();
			fbreader.gotoBookmark(bookmark);
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		private final List<Bookmark> myBookmarks;
		private final boolean myCurrentBook;

		BookmarksAdapter(ListView listView, List<Bookmark> bookmarks, boolean currentBook) {
			myBookmarks = bookmarks;
			myCurrentBook = currentBook;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (getItem(position) != null) {
				menu.setHeaderTitle(getItem(position).getText());
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("open").getValue());
				//menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("edit").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("delete").getValue());
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			final ImageView imageView = (ImageView)view.findViewById(R.id.bookmark_item_icon);
			final TextView textView = (TextView)view.findViewById(R.id.bookmark_item_text);
			final TextView bookTitleView = (TextView)view.findViewById(R.id.bookmark_item_booktitle);

			final Bookmark bookmark = getItem(position);
			if (bookmark == null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.ic_list_plus);
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				textView.setText(bookmark.getText());
				if (myCurrentBook) {
					bookTitleView.setVisibility(View.GONE);
				} else {
					bookTitleView.setVisibility(View.VISIBLE);
					bookTitleView.setText(bookmark.getBookTitle());
				}
			}
			return view;
		}

		public final boolean areAllItemsEnabled() {
			return true;
		}

		public final boolean isEnabled(int position) {
			return true;
		}

		public final long getItemId(int position) {
			return position;
		}

		public final Bookmark getItem(int position) {
			if (myCurrentBook) {
				--position;
			}
			return (position >= 0) ? myBookmarks.get(position) : null;
		}

		public final int getCount() {
			return myCurrentBook ? myBookmarks.size() + 1 : myBookmarks.size();
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else {
				addBookmark();
			}
		}
	}
}
