/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.util.*;

import android.app.Activity;
import android.view.View;
import android.widget.BaseAdapter;

public abstract class ListAdapter<T> extends BaseAdapter implements View.OnCreateContextMenuListener {
	private final Activity myActivity;
	protected final List<T> myItems;

	ListAdapter(Activity activity, List<T> items) {
		myActivity = activity;
		myItems = Collections.synchronizedList(items);
	}

	public void clear() {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.clear();
			}
		});
	}

	public void add(final T item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(item);
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return myItems.size();
	}

	@Override
	public T getItem(int position) {
		return myItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
