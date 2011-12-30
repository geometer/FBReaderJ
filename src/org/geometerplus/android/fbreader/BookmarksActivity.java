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
	
	private static final int MENU_SEARCH_ID = 1;
	private static final int MENU_SHARE_ID = 2;
	private static final int MENU_DELETE_ALL_ID = 3;
	
	private static final int DIALOG_DELETE_ALL_BOOKMARKS_ID = 1;
//	private static final int DIALOG_DELETE_BOOKMARK_ID = 2;

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

		AllBooksBookmarks = Bookmark.bookmarks();
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
		final MenuItem itemSearch = menu.add(
			0, MENU_SEARCH_ID, Menu.NONE,
			myResource.getResource("menu").getResource("search").getValue()
		);
		itemSearch.setOnMenuItemClickListener(this);
		itemSearch.setIcon(R.drawable.ic_menu_search);

		final MenuItem itemShare = menu.add(
				0, MENU_SHARE_ID, Menu.NONE,
				myResource.getResource("menu").getResource("share").getValue()
			);
			itemShare.setOnMenuItemClickListener(this);
			itemShare.setIcon(R.drawable.selection_share_default);

		final MenuItem itemDeleteAll = menu.add(
					0, MENU_DELETE_ALL_ID, Menu.NONE,
					myResource.getResource("menu").getResource("deleteAll").getValue()
				);
				itemDeleteAll.setOnMenuItemClickListener(this);
				itemDeleteAll.setIcon(R.drawable.ic_menu_trash);
		return true;
	}
	@Override 
	protected Dialog onCreateDialog(int id) {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton(buttonResource.getResource("no").getValue(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.setCancelable(false);

		switch(id) {
			case DIALOG_DELETE_ALL_BOOKMARKS_ID:
				builder.setMessage(myResource.getResource("dialog").getResource("deleteAllPrompt").getValue())
				.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						deleteAllBookmarks((ListView)getTabHost().getCurrentView());
					}
				});
				break;
//			case DIALOG_DELETE_BOOKMARK_ID:
//				builder.setMessage(myResource.getResource("dialog").getResource("deletePrompt").getValue())
//				.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						deleteAllBookmarks((ListView)getTabHost().getCurrentView());d
//					}
//				});
//				break;
			default:
				return null;
		}
		return builder.create();
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		return true;
	}
	

	private void shareBookmarks() {
		final TabHost host = getTabHost();
		ListView currentView = (ListView)host.getCurrentView();
		String text = getBookmaksText(currentView);

		String subject;
		if (currentView == myThisBookView) {
			final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
			String title = fbreader.Model.Book.getTitle();
			subject = myResource.getResource("quotesFrom").getValue() + " " + title;
		}
		else 
			subject = myResource.getResource("quotes").getValue();
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, null));
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
			case MENU_SEARCH_ID:
				return onSearchRequested();
			case MENU_SHARE_ID:
				shareBookmarks();
				return true;
			case MENU_DELETE_ALL_ID:
				showDialog(DIALOG_DELETE_ALL_BOOKMARKS_ID);
				return true;
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
//				showDialog(DIALOG_DELETE_BOOKMARK_ID);
				deleteBookmark(bookmark);
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
	private void deleteBookmark(Bookmark bookmark) {
		bookmark.delete();
		myThisBookBookmarks.remove(bookmark);
		AllBooksBookmarks.remove(bookmark);
		mySearchResults.remove(bookmark);
		invalidateAllViews();
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.onOpen();
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final long bookId = bookmark.getBookId();
		if ((fbreader.Model == null) || (fbreader.Model.Book.getId() != bookId)) {
			final Book book = Book.getById(bookId);
			if (book != null) {
				finish();
				fbreader.openBook(book, bookmark);
			} else {
				UIUtil.showErrorMessage(this, "cannotOpenBook");
			}
		} else {
			finish();
			fbreader.gotoBookmark(bookmark);
		}
	}

	private String getBookmarkText(Bookmark bookmark) {
		final int maxLen = 64;
		String text = bookmark.getText();
		if (text.length() > maxLen)
			return text.substring(0, maxLen - 3) + "...";
		return text;
	}
	private String getBookmaksText(ListView currentView) {
		List<Bookmark> bookmarks = getBookmarks(currentView);
		
		String text = "";
		for (int i = bookmarks.size() - 1; i >= 0; i--) {
//		for (Bookmark b: bookmarks) {
			Bookmark b = bookmarks.get(i);
			if (b.IsVisible)
				if (bookmarks != myThisBookBookmarks)
					text += b.getBookTitle() + "\n";
				text += b.getText() + "\n\n";
		}
		return text;
	}
	private List<Bookmark> getBookmarks(ListView currentView) {
		if (currentView == myThisBookView)
			return myThisBookBookmarks;
		else if (currentView == myAllBooksView)
			return AllBooksBookmarks;
		return mySearchResults;
	}
	private void deleteAllBookmarks(ListView currentView) {
		List<Bookmark> bookmarks = getBookmarks(currentView);

		for (Bookmark b: bookmarks)
			b.delete();

		if (myThisBookBookmarks != bookmarks)
			myThisBookBookmarks.removeAll(bookmarks);
		if (AllBooksBookmarks != bookmarks)
			AllBooksBookmarks.removeAll(bookmarks);
		if (mySearchResults != bookmarks)
			mySearchResults.removeAll(bookmarks);

		bookmarks.clear();
		invalidateAllViews();
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
				menu.setHeaderTitle(getBookmarkText(getItem(position)));
				final ZLResource resource = ZLResource.resource("bookmarksView");
				menu.add(0, OPEN_ITEM_ID, 0, resource.getResource("open").getValue());
				//menu.add(0, EDIT_ITEM_ID, 0, resource.getResource("edit").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, resource.getResource("delete").getValue());
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
				textView.setText(ZLResource.resource("bookmarksView").getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				textView.setText(getBookmarkText(bookmark));
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
