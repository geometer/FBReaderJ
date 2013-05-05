/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

public class StyleListActivity extends ListActivity {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private Bookmark myBookmark;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myBookmark = SerializerUtil.deserializeBookmark(
					getIntent().getStringExtra(FBReader.BOOKMARK_KEY)
				);
				if (myBookmark == null) {
					finish();
					return;
				}
				final List<HighlightingStyle> styles = myCollection.highlightingStyles();
				if (styles.isEmpty()) {
					finish();
					return;
				}
				final ActionListAdapter adapter = new ActionListAdapter(styles);
				setListAdapter(adapter);
				getListView().setOnItemClickListener(adapter);
			}
		});
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	private class ActionListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final List<HighlightingStyle> myStyles;

		ActionListAdapter(List<HighlightingStyle> styles) {
			myStyles = new ArrayList<HighlightingStyle>(styles);
		}

		public final int getCount() {
			return myStyles.size();
		}

		public final HighlightingStyle getItem(int position) {
			return myStyles.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.style_item, parent, false);
			final TextView titleView = (TextView)view.findViewById(R.id.style_item_title);
			final String title = "Style " + getItem(position).Id;
			titleView.setText(title);
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			myCollection.bindToService(StyleListActivity.this, new Runnable() {
				public void run() {
					myBookmark.setStyleId(getItem(position).Id);
					myCollection.saveBookmark(myBookmark);
					finish();
				}
			});
		}
	}
}
