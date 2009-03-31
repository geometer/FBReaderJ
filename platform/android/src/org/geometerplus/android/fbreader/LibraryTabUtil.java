/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import android.widget.ListView;

import org.geometerplus.fbreader.description.Author;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.collection.BookCollection;

class LibraryTabUtil {
	static void setAuthorList(ListView view, Author selectedAuthor) {
		setAdapter(view, new AuthorListAdapter(view, selectedAuthor));
	}

	static void setTagList(ListView view, String selectedTag) {
		setAdapter(view, new TagListAdapter(view, selectedTag));
	}

	static void setBookList(final ListView view, final Author author, String selectedSeries) {
		ZLListAdapterImpl adapter =
			new ZLListAdapterImpl(
				view.getContext(),
				new Runnable() {
					public void run() {
						setAuthorList(view, author);
					}
				}
			);
		SeriesItem seriesItem = null;
		int selectedIndex = -1;
		int count = 0;
		for (BookDescription book : BookCollection.Instance().booksByAuthor(author)) {
			String series = book.getSeriesName();
			if (series.length() == 0) {
				seriesItem = null;
				adapter.addItem(new BookItem(book));
				++count;
			} else {
				if ((seriesItem == null) || !series.equals(seriesItem.getTopText())) {
					if (series.equals(selectedSeries)) {
						selectedIndex = count;
					}
					seriesItem = new SeriesItem(view, author, series);
					adapter.addItem(seriesItem);
					++count;
				}
				seriesItem.addBook(book);
			}
		}
		setAdapter(view, adapter);
		if (selectedIndex >= 0) {
			view.setSelection(selectedIndex);
		}
	}

	static void setBookList(final ListView view, final String tag) {
		ZLListAdapterImpl adapter =
			new ZLListAdapterImpl(
				view.getContext(),
				new Runnable() {
					public void run() {
						setTagList(view, tag);
					}
				}
			);
		for (BookDescription book : BookCollection.Instance().booksByTag(tag)) {
			adapter.addItem(new BookItem(book));
		}
		setAdapter(view, adapter);
	}

	static void setRecentBooksList(final ListView view) {
		setAdapter(view, new RecentBooksListAdapter(view.getContext()));
	}

	static void setSeriesBookList(final ListView view, final Author author, final String series) {
		ZLListAdapterImpl adapter =
			new ZLListAdapterImpl(
				view.getContext(),
				new Runnable() {
					public void run() {
						setBookList(view, author, series);
					}
				}
			);
		for (BookDescription book : BookCollection.Instance().booksByAuthor(author)) {
			if (series.equals(book.getSeriesName())) {
				adapter.addItem(new BookItem(book));
			}
		}
		setAdapter(view, adapter);
	}

	private static void setAdapter(ListView view, ZLListAdapter adapter) {
		view.setAdapter(adapter);
		view.setOnKeyListener(adapter);
		view.setOnItemClickListener(adapter);
		int selectedIndex = adapter.getSelectedIndex();
		if (selectedIndex >= 0) {
			view.setSelection(selectedIndex);
		}
	}
}
