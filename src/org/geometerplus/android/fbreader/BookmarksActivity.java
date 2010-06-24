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

import java.util.*;

import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.app.TabActivity;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.library.*;

public class BookmarksActivity extends TabActivity implements MenuItem.OnMenuItemClickListener {
	static BookmarksActivity Instance;

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

		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.bookmarks, host.getTabContentView(), true);

		AllBooksBookmarks = Bookmark.bookmarks();
		Collections.sort(AllBooksBookmarks, new Bookmark.ByTimeComparator());
		final FBReader fbreader = (FBReader)FBReader.Instance();

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
	public void onResume() {
		super.onResume();
		Instance = this;
	}

	@Override
	public void onPause() {
		for (Bookmark bookmark : AllBooksBookmarks) {
			bookmark.save();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		Instance = null;
		super.onStop();
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
		final FBReader fbreader = (FBReader)FBReader.Instance();
		startSearch(fbreader.BookmarkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Bookmark> results) {
		if (mySearchResultsView == null) {
			mySearchResultsView = createTab("searchResults", R.id.search_results);
			new BookmarksAdapter(mySearchResultsView, mySearchResults, false);
		} else {
			mySearchResults.clear();
		}
		mySearchResults.addAll(results);
		mySearchResultsView.invalidateViews();
		mySearchResultsView.requestLayout();
		getTabHost().setCurrentTabByTag("searchResults");
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

	private String createBookmarkText(ZLTextWordCursor cursor) {
		cursor = new ZLTextWordCursor(cursor);

		final StringBuilder builder = new StringBuilder();
		final StringBuilder sentenceBuilder = new StringBuilder();
		final StringBuilder phraseBuilder = new StringBuilder();

		int wordCounter = 0;
		int sentenceCounter = 0;
		int storedWordCounter = 0;
		boolean lineIsNonEmpty = false;
		boolean appendLineBreak = false;
mainLoop:
		while ((wordCounter < 20) && (sentenceCounter < 3)) {
			while (cursor.isEndOfParagraph()) {
				if (!cursor.nextParagraph()) {
					break mainLoop;
				}
				if ((builder.length() > 0) && cursor.getParagraphCursor().isEndOfSection()) {
					break mainLoop;
				}
				if (phraseBuilder.length() > 0) {
					sentenceBuilder.append(phraseBuilder);
					phraseBuilder.delete(0, phraseBuilder.length());
				}
				if (sentenceBuilder.length() > 0) {
					if (appendLineBreak) {
						builder.append("\n");
					}
					builder.append(sentenceBuilder);
					sentenceBuilder.delete(0, sentenceBuilder.length());
					++sentenceCounter;
					storedWordCounter = wordCounter;
				}
				lineIsNonEmpty = false;
				if (builder.length() > 0) {
					appendLineBreak = true;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord)element;
				if (lineIsNonEmpty) {
					phraseBuilder.append(" ");
				}
				phraseBuilder.append(word.Data, word.Offset, word.Length);
				++wordCounter;
				lineIsNonEmpty = true;
				switch (word.Data[word.Offset + word.Length - 1]) {
					case ',':
					case ':':
					case ';':
					case ')':
						sentenceBuilder.append(phraseBuilder);
						phraseBuilder.delete(0, phraseBuilder.length());
						break;
					case '.':
					case '!':
					case '?':
						++sentenceCounter;
						if (appendLineBreak) {
							builder.append("\n");
							appendLineBreak = false;
						}
						sentenceBuilder.append(phraseBuilder);
						phraseBuilder.delete(0, phraseBuilder.length());
						builder.append(sentenceBuilder);
						sentenceBuilder.delete(0, sentenceBuilder.length());
						storedWordCounter = wordCounter;
						break;
				}
			}
			cursor.nextWord();
		}
		if (storedWordCounter < 4) {
			if (sentenceBuilder.length() == 0) {
				sentenceBuilder.append(phraseBuilder);
			}
			if (appendLineBreak) {
				builder.append("\n");
			}
			builder.append(sentenceBuilder);
		}
		return builder.toString();
	}

	private void addBookmark() {
		final FBReader fbreader = (FBReader)FBReader.Instance();
		final ZLTextView textView = fbreader.getTextView();
		final ZLTextWordCursor cursor = textView.getStartCursor();

		if (cursor.isNull()) {
			// TODO: implement
			return;
		}

		// TODO: text edit dialog
		final Bookmark bookmark = new Bookmark(
			fbreader.Model.Book,
			createBookmarkText(cursor),
			textView.getModel().getId(),
			cursor
		);
		myThisBookBookmarks.add(0, bookmark);
		AllBooksBookmarks.add(0, bookmark);
		invalidateAllViews();
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.onOpen();
		final FBReader fbreader = (FBReader)FBReader.Instance();
		final long bookId = bookmark.getBookId();
		if ((fbreader.Model == null) || (fbreader.Model.Book.getId() != bookId)) {
			final Book book = Book.getById(bookId);
			if (book != null) {
				finish();
				fbreader.openBook(book, bookmark);
			} else {
				Toast.makeText(
					this,
					ZLResource.resource("errorMessage").getResource("cannotOpenBook").getValue(),
					Toast.LENGTH_SHORT
				).show();
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
