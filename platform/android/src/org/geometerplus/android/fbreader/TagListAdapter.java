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

import org.geometerplus.fbreader.collection.BookCollection;

final class TagListAdapter extends ZLListAdapter {
	private final ArrayList<String> myTags = new ArrayList<String>(BookCollection.Instance().tags());
	private final ZLListItem[] myItems;
	private final ListView myView;
	private final String mySelectedTag;
	private final boolean myHasBooksWithNoTags;

	TagListAdapter(ListView view, String selectedTag) {
		super(view.getContext(), null);
		myView = view;
		mySelectedTag = selectedTag;
		myHasBooksWithNoTags = !BookCollection.Instance().booksByTag(null).isEmpty();
		int count = myTags.size();
		if (myHasBooksWithNoTags) {
			++count;
		}
		myItems = new ZLListItem[count];
	}

	public int getCount() {
		return myItems.length;
	}

	public ZLListItem getItem(final int position) {
		ZLListItem item = myItems[position];
		if (item == null) {
			int pos = position;
			if (myHasBooksWithNoTags) {
				if (pos == 0) {
					item = new TagItem(myView, null); 
				} else {
					--pos;
				}
			}
			if (item == null) {
				item = new TagItem(myView, myTags.get(pos));
			}
			myItems[position] = item;
		}
		return item;
	}

	public int getSelectedIndex() {
		if (mySelectedTag == null) {
			return 0;
		}
		if (mySelectedTag.length() == 0) {
			return -1;
		}
		int count = myHasBooksWithNoTags ? 1 : 0;
		for (String tag : myTags) {
			if (tag.equals(mySelectedTag)) {
				return count;
			}
			++count;
		}
		return -1;
	}
}
