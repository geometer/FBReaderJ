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

package org.geometerplus.android.fbreader.preferences.activityprefs;

import java.util.*;

import android.content.*;
import android.app.*;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class BaseStringListActivity extends ListActivity {
	public static final String LIST = "list";
	public static final String TITLE = "title";
	public static final String SUGGESTIONS = "suggestions";

	private ImageButton myAddButton;
	private Button myOkButton;
	public List<String> Suggestions;

	public void enableButtons() {
		if (myAddButton != null) {
			myAddButton.setEnabled(getListAdapter().isSaveable());
		}
		if (myOkButton != null) {
			myOkButton.setEnabled(getListAdapter().isSaveable());
		}
	}

	protected void initView(ItemAdapter a) {
		setContentView(R.layout.editable_stringlist);

		setTitle(getIntent().getStringExtra(TITLE));

		final List<String> list = getIntent().getStringArrayListExtra(LIST);
		Suggestions = getIntent().getStringArrayListExtra(SUGGESTIONS);

		final View footerView = LayoutInflater.from(this).inflate(R.layout.editable_stringlist_footer, null);
		getListView().addFooterView(footerView);
		setListAdapter(a);

		for (String s : list) {
			StringItem i = new StringItem();
			i.setData(s);
			getListAdapter().addStringItem(i);
		}

		myAddButton = (ImageButton)footerView.findViewById(R.id.editable_stringlist_addbutton);
		myAddButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					getListAdapter().addStringItem(new StringItem());
				}
			}
		);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final View buttonView = footerView.findViewById(R.id.editable_stringlist_buttons);
		myOkButton = (Button)buttonView.findViewById(R.id.ok_button);
		final Button cancelButton = (Button)buttonView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		myOkButton.setText(buttonResource.getResource("ok").getValue());

		myOkButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					ArrayList<String> paths = new ArrayList<String>();
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						paths.add(getListAdapter().getItem(i).getFullData());
					}
					Intent intent = new Intent();
					intent.putStringArrayListExtra(LIST, paths);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		);
		enableButtons();

		cancelButton.setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					setResult(RESULT_CANCELED);
					finish();
				}
			}
		);

	}

	@Override
	public ItemAdapter getListAdapter() {
		return (ItemAdapter)super.getListAdapter();
	}

	protected static abstract class ItemAdapter extends BaseAdapter {
		private int nextId = 0;
		protected final BaseStringListActivity myActivity;

		private final ArrayList<StringItem> myItems = new ArrayList<StringItem>();

		protected boolean myKeyboardShowed = false;

		protected boolean myUserWasWarned = false;

		public ItemAdapter(BaseStringListActivity a) {
			super();
			myActivity = a;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public StringItem getItem(int position) {
			return myItems.get(position);
		}

		synchronized void addStringItem(StringItem i) {
			i.setId(nextId);
			nextId = nextId + 1;
			myItems.add(i);
			notifyDataSetChanged();
			myActivity.enableButtons();
			myKeyboardShowed = false;
			myActivity.getListView().post(new Runnable(){
				public void run() {
					myActivity.getListView().setSelection(getCount() - 1);
				}
			});
		}

		@Override
		public synchronized int getCount() {
			return myItems.size();
		}

		public boolean isSaveable() {
			for (StringItem i : myItems) {
				if ("".equals(i.getData())) return false;
			}
			return true;
		}

		synchronized void removeStringItem(int id) {
			for (int i = 0; i < myItems.size(); i++) {
				if (myItems.get(i).getId() == id) {
					myItems.remove(i);
					notifyDataSetChanged();
					myActivity.enableButtons();
					return;
				}
			}
		}

		StringItem getStringItem(int id) {
			for (int i = 0; i < myItems.size(); i++) {
				if (myItems.get(i).getId() == id) {
					return myItems.get(i);
				}
			}
			return null;
		}
	}

	public static class StringItem {
		private String myData = "";
		private String mySubData = "";
		private int myId;
		public static char Divider = '\n';

		public String getData() {
			return myData;
		}

		public String getFullData() {
			if (mySubData == null || "".equals(mySubData)) {
				return myData;
			}
			return myData + Divider + mySubData;
		}

		public void setData(String data) {
			int index = data.indexOf(Divider);
			if (index != -1) {
				myData = data.substring(0, index);
				mySubData = data.substring(index + 1);
			} else {
				myData = data;
				mySubData = "";
			}
		}

		public int getId() {
			return myId;
		}

		public void setId(int id) {
			myId = id;
		}
	}
}
