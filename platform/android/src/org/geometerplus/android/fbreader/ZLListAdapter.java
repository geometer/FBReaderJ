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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

abstract class ZLListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnKeyListener {
	private final Context myContext;
	private final Runnable myCancelAction;

	ZLListAdapter(Context context, Runnable cancelAction) {
		myContext = context;
		myCancelAction = cancelAction;
	}

	public abstract int getCount();
	public abstract ZLListItem getItem(final int position);
	public abstract int getSelectedIndex();

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(myContext).inflate(R.layout.library_list_item, parent, false);
		final ZLListItem item = getItem(position);
		((TextView)view.findViewById(R.id.library_list_item_top)).setText(item.getTopText());
		((TextView)view.findViewById(R.id.library_list_item_bottom)).setText(item.getBottomText());
		return view;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		getItem(position).run();
	}

	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		switch (keyEvent.getAction()) {
			case KeyEvent.ACTION_UP:
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						((ZLListItem)((ListView)view).getSelectedItem()).run();
						return true;
					case KeyEvent.KEYCODE_BACK:
						return true;
				}
				break;
			case KeyEvent.ACTION_DOWN:
				if ((keyCode == KeyEvent.KEYCODE_BACK) && (myCancelAction != null)) {
					myCancelAction.run();
					return true;
				}
				break;
		}
		return false;
	}
}
