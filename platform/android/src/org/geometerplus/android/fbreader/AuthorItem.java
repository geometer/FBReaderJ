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

class AuthorItem implements LibraryListItem {
	private final ListView myView;
	private final Author myAuthor;

	AuthorItem(ListView view, Author author) {
		myView = view;
		myAuthor = author;
	}

	public String getTopText() {
		return myAuthor.DisplayName;
	}

	public String getBottomText() {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		String currentSeries = null;
		for (BookDescription description : BookCollection.Instance().booksByAuthor(myAuthor)) {
			final String series = description.getSeriesName();
			if (series.length() == 0) {
				if (count++ > 0) {
					builder.append(",  ");
				}
				builder.append(description.getTitle());
				currentSeries = null;
			} else if (!series.equals(currentSeries)) {
				if (count++ > 0) {
					builder.append(",  ");
				}
				builder.append(series);
				currentSeries = series;
			}
			if (count == 5) {
				break;
			}
		}
		return builder.toString();
	}

	public void run() {
		LibraryTabUtil.setBookList(myView, myAuthor, null);
		myView.invalidate();
	}
}
