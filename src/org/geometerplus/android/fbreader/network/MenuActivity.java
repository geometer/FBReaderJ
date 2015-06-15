/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.api.PluginApi;

abstract class MenuActivity extends ListActivity implements AdapterView.OnItemClickListener {
	protected List<PluginApi.MenuActionInfo> myInfos;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		myInfos = new ArrayList<PluginApi.MenuActionInfo>();

		init();

		try {
			startActivityForResult(new Intent(getAction(), getIntent().getData()), 0);
		} catch (ActivityNotFoundException e) {
			if (finishInitialization()) {
				return;
			}
		}

		setListAdapter(new ActionListAdapter());
		getListView().setOnItemClickListener(this);
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runItem(myInfos.get(position));
		finish();
	}

	private boolean finishInitialization() {
		switch (myInfos.size()) {
			default:
				return false;
			case 0:
				finish();
				return true;
			case 1:
				runItem(myInfos.get(0));
				finish();
				return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent != null) {
			final List<PluginApi.MenuActionInfo> actions =
				intent.<PluginApi.MenuActionInfo>getParcelableArrayListExtra(
					PluginApi.PluginInfo.KEY
				);
			if (actions != null) {
				myInfos.addAll(actions);
			}
			if (finishInitialization()) {
				return;
			}
			Collections.sort(myInfos);
			((ActionListAdapter)getListAdapter()).notifyDataSetChanged();
			getListView().invalidateViews();
		}
	}

	protected abstract void init();
	protected abstract String getAction();
	protected abstract void runItem(final PluginApi.MenuActionInfo info);

	private class ActionListAdapter extends BaseAdapter {
		public final int getCount() {
			return myInfos.size();
		}

		public final PluginApi.MenuActionInfo getItem(int position) {
			return myInfos.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
			final TextView titleView = (TextView)view.findViewById(R.id.menu_item_title);
			titleView.setText(getItem(position).MenuItemName);
			return view;
		}
	}
}
