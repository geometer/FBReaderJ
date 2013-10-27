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

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import android.util.Log;
import android.view.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.CancelMenuHelper;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

import org.geometerplus.android.util.ViewUtil;

public class CancelActivity extends ListActivity {
	@Override
	protected void onStop() {
		if (myCollection != null) {
			myCollection.unbind();
		}
		super.onStop();
	}

	static final String LIST_SIZE = "listSize";
	static final String ITEM_TITLE = "title";
	static final String ITEM_SUMMARY = "summary";
	
	private BookCollectionShadow myCollection = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		if (fbReader != null) {
			Intent i = getIntent();
			if (i.getStringExtra("FROM_PLUGIN") != null) {
				myCollection = new BookCollectionShadow();
				myCollection.bindToService(this, new Runnable() {
					public void run() {
						List<CancelMenuHelper.ActionDescription> descriptionList = fbReader.getCancelActionsList(myCollection);
						init(descriptionList);
					}
				});
			} else {
				List<CancelMenuHelper.ActionDescription> descriptionList = fbReader.getCancelActionsList();
				init(descriptionList);
			}
		} else {
			myCollection = new BookCollectionShadow();
			myCollection.bindToService(this, new Runnable() {
				public void run() {
					List<CancelMenuHelper.ActionDescription> descriptionList =
						new CancelMenuHelper().getActionsList(myCollection, false);
					init(descriptionList);
				}
			});
		}
	}
	
	private void init(List<CancelMenuHelper.ActionDescription> descriptionList) {
		Intent i = getIntent();
		i.putExtra(CancelActivity.LIST_SIZE, descriptionList.size());
		int index = 0;
		for (CancelMenuHelper.ActionDescription description : descriptionList) {
			i.putExtra(CancelActivity.ITEM_TITLE + index, description.Title);
			i.putExtra(CancelActivity.ITEM_SUMMARY + index, description.Summary);
			++index;
		}
		final ActionListAdapter adapter = new ActionListAdapter(i);
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
			final TextView titleView = ViewUtil.findTextView(view, R.id.cancel_item_title);
			final TextView summaryView = ViewUtil.findTextView(view, R.id.cancel_item_summary);
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
