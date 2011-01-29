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

package org.geometerplus.android.fbreader;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.*;

import org.geometerplus.zlibrary.ui.android.R;

public class CancelActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setListAdapter(new ActionListAdapter());
	}

	private class ActionListAdapter extends BaseAdapter {
		@Override
		public final int getCount() {
			return 5;
		}

		@Override
		public final Integer getItem(int position) {
			return position;
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.cancel_item, parent, false);
			final TextView titleView = (TextView)view.findViewById(R.id.cancel_item_title);
			final TextView summaryView = (TextView)view.findViewById(R.id.cancel_item_summary);
			if (position == 0) {
				titleView.setText("Open previous book");
			} else if (position == getCount() - 1) {
				titleView.setText("Close FBReader");
			} else {
				titleView.setText("Go to page");
			}
			return view;
		}
	}
}
