/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.bookmark;

import java.util.*;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;

import yuku.ambilwarna.widget.AmbilWarnaPrefWidgetView;

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.*;

public class BookmarksActivity extends Activity implements IBookCollection.Listener<Book> {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private TabHost myTabHost;

	private final Map<Integer,HighlightingStyle> myStyles =
		Collections.synchronizedMap(new HashMap<Integer,HighlightingStyle>());

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	private volatile Bookmark myBookmark;

	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();

	private volatile BookmarksAdapter myThisBookAdapter;
	private volatile BookmarksAdapter myAllBooksAdapter;
	private volatile BookmarksAdapter mySearchResultsAdapter;

	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final ZLStringOption myBookmarkSearchPatternOption =
		new ZLStringOption("BookmarkSearch", "Pattern", "");

	private void createTab(String tag, int id) {
		final String label = myResource.getResource(tag).getValue();
		myTabHost.addTab(myTabHost.newTabSpec(tag).setIndicator(label).setContent(id));
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.bookmarks);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

        myTabHost = (TabHost)findViewById(R.id.bookmarks_tabhost);
		myTabHost.setup();

		createTab("thisBook", R.id.bookmarks_this_book);
		createTab("allBooks", R.id.bookmarks_all_books);
		createTab("search", R.id.bookmarks_search);

		myTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if ("search".equals(tabId)) {
					findViewById(R.id.bookmarks_search_results).setVisibility(View.GONE);
					onSearchRequested();
				}
			}
		});

		myBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);
		if (myBook == null) {
			finish();
		}
		myBookmark = FBReaderIntents.getBookmarkExtra(getIntent());
	}

	@Override
	protected void onStart() {
		super.onStart();

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myAllBooksAdapter != null) {
					return;
				}

				myThisBookAdapter =
					new BookmarksAdapter((ListView)findViewById(R.id.bookmarks_this_book), myBookmark != null);
				myAllBooksAdapter =
					new BookmarksAdapter((ListView)findViewById(R.id.bookmarks_all_books), false);
				myCollection.addListener(BookmarksActivity.this);

				updateStyles();
				loadBookmarks();
			}
		});

		OrientationUtil.setOrientation(this, getIntent());
	}

	private void updateStyles() {
		synchronized (myStyles) {
			myStyles.clear();
			for (HighlightingStyle style : myCollection.highlightingStyles()) {
				myStyles.put(style.Id, style);
			}
		}
	}

	private final Object myBookmarksLock = new Object();

	private void loadBookmarks() {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					for (BookmarkQuery query = new BookmarkQuery(myBook, 50); ; query = query.next()) {
						final List<Bookmark> thisBookBookmarks = myCollection.bookmarks(query);
						if (thisBookBookmarks.isEmpty()) {
							break;
						}
						myThisBookAdapter.addAll(thisBookBookmarks);
						myAllBooksAdapter.addAll(thisBookBookmarks);
					}
					for (BookmarkQuery query = new BookmarkQuery(50); ; query = query.next()) {
						final List<Bookmark> allBookmarks = myCollection.bookmarks(query);
						if (allBookmarks.isEmpty()) {
							break;
						}
						myAllBooksAdapter.addAll(allBookmarks);
					}
				}
			}
		}).start();
	}

	private void updateBookmarks(final Book book) {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					final boolean flagThisBookTab = book.getId() == myBook.getId();
					final boolean flagSearchTab = mySearchResultsAdapter != null;

					final Map<String,Bookmark> oldBookmarks = new HashMap<String,Bookmark>();
					if (flagThisBookTab) {
						for (Bookmark b : myThisBookAdapter.bookmarks()) {
							oldBookmarks.put(b.Uid, b);
						}
					} else {
						for (Bookmark b : myAllBooksAdapter.bookmarks()) {
							if (b.BookId == book.getId()) {
								oldBookmarks.put(b.Uid, b);
							}
						}
					}
					final String pattern = myBookmarkSearchPatternOption.getValue().toLowerCase();

					for (BookmarkQuery query = new BookmarkQuery(book, 50); ; query = query.next()) {
						final List<Bookmark> loaded = myCollection.bookmarks(query);
						if (loaded.isEmpty()) {
							break;
						}
						for (Bookmark b : loaded) {
							final Bookmark old = oldBookmarks.remove(b.Uid);
							myAllBooksAdapter.replace(old, b);
							if (flagThisBookTab) {
								myThisBookAdapter.replace(old, b);
							}
							if (flagSearchTab && MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
								mySearchResultsAdapter.replace(old, b);
							}
						}
					}
					myAllBooksAdapter.removeAll(oldBookmarks.values());
					if (flagThisBookTab) {
						myThisBookAdapter.removeAll(oldBookmarks.values());
					}
					if (flagSearchTab) {
						mySearchResultsAdapter.removeAll(oldBookmarks.values());
					}
				}
			}
		}).start();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);

		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		myBookmarkSearchPatternOption.setValue(pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : myAllBooksAdapter.bookmarks()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
		if (!bookmarks.isEmpty()) {
			final ListView resultsView = (ListView)findViewById(R.id.bookmarks_search_results);
			resultsView.setVisibility(View.VISIBLE);
			if (mySearchResultsAdapter == null) {
				mySearchResultsAdapter = new BookmarksAdapter(resultsView, false);
			} else {
				mySearchResultsAdapter.clear();
			}
			mySearchResultsAdapter.addAll(bookmarks);
		} else {
			UIMessageUtil.showErrorMessage(this, "bookmarkNotFound");
		}
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, BookmarksActivity.class, myBookmarkSearchPatternOption.getValue(), null);
		}
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final String tag = myTabHost.getCurrentTabTag();
		final BookmarksAdapter adapter;
		if ("thisBook".equals(tag)) {
			adapter = myThisBookAdapter;
		} else if ("allBooks".equals(tag)) {
			adapter = myAllBooksAdapter;
		} else if ("search".equals(tag)) {
			adapter = mySearchResultsAdapter;
		} else {
			throw new RuntimeException("Unknown tab tag: " + tag);
		}

		final Bookmark bookmark = adapter.getItem(position);
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				gotoBookmark(bookmark);
				return true;
			case EDIT_ITEM_ID:
				final Intent intent = new Intent(this, EditBookmarkActivity.class);
				FBReaderIntents.putBookmarkExtra(intent, bookmark);
				OrientationUtil.startActivity(this, intent);
				return true;
			case DELETE_ITEM_ID:
				myCollection.deleteBookmark(bookmark);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.BookId);
		if (book != null) {
			FBReader.openBookActivity(this, book, bookmark);
		} else {
			UIMessageUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		private final List<Bookmark> myBookmarksList =
			Collections.synchronizedList(new LinkedList<Bookmark>());
		private volatile boolean myShowAddBookmarkItem;

		BookmarksAdapter(ListView listView, boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarksList);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
							if (position < 0) {
								myBookmarksList.add(- position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		private boolean areEqualsForView(Bookmark b0, Bookmark b1) {
			return
				b0.getStyleId() == b1.getStyleId() &&
				b0.getText().equals(b1.getText()) &&
				b0.getTimestamp(Bookmark.DateType.Latest).equals(b1.getTimestamp(Bookmark.DateType.Latest));
		}

		public void replace(final Bookmark old, final Bookmark b) {
			if (old != null && areEqualsForView(old, b)) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						if (old != null) {
							myBookmarksList.remove(old);
						}
						final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
						if (position < 0) {
							myBookmarksList.add(- position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void removeAll(final Collection<Bookmark> bookmarks) {
			if (bookmarks.isEmpty()) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.removeAll(bookmarks);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.clear();
					notifyDataSetChanged();
				}
			});
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (getItem(position) != null) {
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("openBook").getValue());
				menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("editBookmark").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("deleteBookmark").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			final ImageView imageView = ViewUtil.findImageView(view, R.id.bookmark_item_icon);
			final View colorContainer = ViewUtil.findView(view, R.id.bookmark_item_color_container);
			final AmbilWarnaPrefWidgetView colorView =
				(AmbilWarnaPrefWidgetView)ViewUtil.findView(view, R.id.bookmark_item_color);
			final TextView textView = ViewUtil.findTextView(view, R.id.bookmark_item_text);
			final TextView bookTitleView = ViewUtil.findTextView(view, R.id.bookmark_item_booktitle);

			final Bookmark bookmark = getItem(position);
			if (bookmark == null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.ic_list_plus);
				colorContainer.setVisibility(View.GONE);
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				colorContainer.setVisibility(View.VISIBLE);
				BookmarksUtil.setupColorView(colorView, myStyles.get(bookmark.getStyleId()));
				textView.setText(bookmark.getText());
				if (myShowAddBookmarkItem) {
					bookTitleView.setVisibility(View.GONE);
				} else {
					bookTitleView.setVisibility(View.VISIBLE);
					bookTitleView.setText(bookmark.BookTitle);
				}
			}
			return view;
		}

		@Override
		public final boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {
			return true;
		}

		@Override
		public final long getItemId(int position) {
			final Bookmark item = getItem(position);
			return item != null ? item.getId() : -1;
		}

		@Override
		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return position >= 0 ? myBookmarksList.get(position) : null;
		}

		@Override
		public final int getCount() {
			return myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else if (myShowAddBookmarkItem) {
				myShowAddBookmarkItem = false;
				myCollection.saveBookmark(myBookmark);
			}
		}
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
			default:
				break;
			case BookmarkStyleChanged:
				runOnUiThread(new Runnable() {
					public void run() {
						updateStyles();
						myAllBooksAdapter.notifyDataSetChanged();
						myThisBookAdapter.notifyDataSetChanged();
						if (mySearchResultsAdapter != null) {
							mySearchResultsAdapter.notifyDataSetChanged();
						}
					}
				});
				break;
			case BookmarksUpdated:
				updateBookmarks(book);
				break;
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}
}
