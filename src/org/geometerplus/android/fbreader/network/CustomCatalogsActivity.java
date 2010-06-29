/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;


public class CustomCatalogsActivity extends ListActivity {

	private final ZLResource myResource = ZLResource.resource("customCatalogsView");

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (!NetworkView.Instance().isInitialized()) {
			finish();
			return;
		}

		setTitle(myResource.getResource("title").getValue());
		setListAdapter(new CatalogsAdapter());
	}


	private final class CatalogsAdapter extends BaseAdapter {

		public int getCount() {
			return NetworkLibrary.Instance().getCustomLinksNumber();
		}

		public ICustomNetworkLink getItem(int position) {
			return NetworkLibrary.Instance().getCustomLink(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

			ICustomNetworkLink link = getItem(position);

			((TextView)view.findViewById(R.id.network_tree_item_name)).setText(link.getTitle());
			((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(link.getSummary());

			return view;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		l.getItemAtPosition(position);
	}
}
