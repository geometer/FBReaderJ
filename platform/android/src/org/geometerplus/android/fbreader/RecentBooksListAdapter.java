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

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.collection.RecentBooks;

final class RecentBooksListAdapter extends ZLListAdapter {
	final ArrayList<BookDescription> myBooks = RecentBooks.Instance().books();
	final ZLListItem[] myItems = new ZLListItem[myBooks.size()];

	RecentBooksListAdapter(Context context) {
		super(context, null);
	}

	public int getCount() {
		return myItems.length;
	}

	public ZLListItem getItem(final int position) {
		ZLListItem item = myItems[position];
		if (item == null) {
			item = new BookItem(myBooks.get(position));
			myItems[position] = item;
		}
		return item;
	}

	public int getSelectedIndex() {
		return -1;
	}
}
