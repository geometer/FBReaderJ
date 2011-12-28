/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

import org.geometerplus.zlibrary.ui.android.R;

public class CancelActivity extends ListActivity {
	static final String LIST_SIZE = "listSize";
	static final String ITEM_TITLE = "title";
	static final String ITEM_SUMMARY = "summary";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final ActionListAdapter adapter = new ActionListAdapter(getIntent());
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
		setResult(-1);
	}

	private class ActionListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final Intent myIntent;

		ActionListAdapter(Intent intent) {
			myIntent = intent;
		}

		public final int getCount() {
			return myIntent.getIntExtra(LIST_SIZE, 0);
		}

		public final Integer getItem(int position) {
			return position;
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.cancel_item, parent, false);
			final TextView titleView = (TextView)view.findViewById(R.id.cancel_item_title);
			final TextView summaryView = (TextView)view.findViewById(R.id.cancel_item_summary);
			final String title = myIntent.getStringExtra(ITEM_TITLE + position);
			final String summary = myIntent.getStringExtra(ITEM_SUMMARY + position);
			titleView.setText(title);
			if (summary != null) {
				summaryView.setVisibility(View.VISIBLE);
				summaryView.setText(summary);
				titleView.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
				));
			} else {
				summaryView.setVisibility(View.GONE);
				titleView.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT
				));
			}
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			setResult((int)id + 1);
			finish();
		}
	}
}
