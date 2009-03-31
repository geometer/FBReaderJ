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

import java.util.ArrayList;

import android.content.Context;
import android.widget.ListView;

import org.geometerplus.fbreader.description.Author;
import org.geometerplus.fbreader.collection.BookCollection;

final class AuthorListAdapter extends ZLListAdapter {
	private final ArrayList<Author> myAuthors = new ArrayList(BookCollection.Instance().authors());
	private final ZLListItem[] myItems = new ZLListItem[myAuthors.size()];
	private final ListView myView;
	private final Author mySelectedAuthor;

	AuthorListAdapter(ListView view, Author selectedAuthor) {
		super(view.getContext(), null);
		myView = view;
		mySelectedAuthor = selectedAuthor;
	}

	public int getCount() {
		return myItems.length;
	}

	public ZLListItem getItem(final int position) {
		ZLListItem item = myItems[position];
		if (item == null) {
			item = new AuthorItem(myView, myAuthors.get(position));
			myItems[position] = item;
		}
		return item;
	}

	public int getSelectedIndex() {
		if (mySelectedAuthor == null) {
			return -1;
		}
		int count = 0;
		for (Author author : myAuthors) {
			if (author.equals(mySelectedAuthor)) {
				return count;
			}
			++count;
		}
		return -1;
	}
}
