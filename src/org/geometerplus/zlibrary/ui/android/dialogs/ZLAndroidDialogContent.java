/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.util.ZLArrayUtils;

class ZLAndroidDialogContent extends ZLDialogContent implements ZLAndroidDialogInterface {
	private Context myContext;
	protected ListView myListView;

	final ArrayList<View> myAndroidViews = new ArrayList<View>();
	private boolean[] mySelectableMarks = new boolean[10];

	ZLAndroidDialogContent(Context context, ZLResource resource) {
		super(resource);
		myContext = context;
	}

	public void setActivity(DialogActivity activity) {
		createListView(activity);
		activity.setContentView(myListView);
		activity.setTitle(getDisplayName());
	}

	public void endActivity() {
		accept();
		myAndroidViews.clear();
	}

	protected void createListView(Context context) {
		myContext = context;
		myListView = new ListView(context);	
		myListView.setAdapter(new ViewAdapter());
	}

	Context getContext() {
		return myContext;
	}

	private ArrayList<View> getAndroidViews() {
		if (myAndroidViews.isEmpty()) {
			final ArrayList<ZLOptionView> views = Views;
			final int len = views.size();
			for (int i = 0; i < len; ++i) {
				final ZLAndroidOptionView v = (ZLAndroidOptionView)views.get(i);
				if (v.isVisible()) {
					v.addAndroidViews();	
				}
			}
		}
		return myAndroidViews;
	}

	void invalidateView() {
		if (!myAndroidViews.isEmpty()) {
			myAndroidViews.clear();
			myListView.setAdapter(new ViewAdapter());
			myListView.invalidate();
		}
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		if (name != null) {
			name = name.replaceAll("&", "");
		}
		ZLAndroidOptionView view = null;
		switch (option.getKind()) {
			case ZLOptionKind.COMBO:
				view = new ZLAndroidComboOptionView(
					this, name, (ZLComboOptionEntry)option
				);
				break;
			case ZLOptionKind.COLOR:
				view = new ZLAndroidColorOptionView(
					this, name, (ZLColorOptionEntry)option
				);
				break;
		}
		if (view != null) {
			view.setVisible(option.isVisible());
		}
		addView(view);
	}

	void addAndroidView(View view, boolean isSelectable) {
		if (view != null) {
			boolean[] marks = mySelectableMarks;
			final int len = marks.length;
			final int index = myAndroidViews.size();
			if (index == len) {
				marks = ZLArrayUtils.createCopy(marks, len, 2 * len);
				mySelectableMarks = marks;
			}
			myAndroidViews.add(view);
			marks[index] = isSelectable;
		}
	}

	private class ViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			return (View)getAndroidViews().get(position);
		}

		/*public boolean areAllItemsSelectable() {
			return true;
		}

		public boolean isSelectable(int position) {
			return mySelectableMarks[position];
		}*/

		public int getCount() {
			return getAndroidViews().size();
		}

		public Object getItem(int position) {
			return "";
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
