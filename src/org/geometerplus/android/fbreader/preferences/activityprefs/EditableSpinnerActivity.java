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

package org.geometerplus.android.fbreader.preferences.activityprefs;

import java.util.*;

import android.content.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;

public class EditableSpinnerActivity extends BaseStringListActivity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		initView(new MyAdapter(this));
		Collections.sort(Suggestions);
	}

	private static class MyAdapter extends ItemAdapter {

		public MyAdapter(BaseStringListActivity a) {
			super(a);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final StringItem item = getItem(position);
			final View view;
			view = LayoutInflater.from(myActivity).inflate(R.layout.editable_stringlist_spinneritem, parent, false);
			final Spinner spinner = (Spinner)view.findViewById(R.id.editable_stringlist_spinner);
			int spinnerPosition = myActivity.Suggestions.indexOf(item.getData());
			SpinnerAdapter adapter;
			if (spinnerPosition != -1) {
				adapter = new SpinnerAdapter(item, false);
			} else {
				adapter = new SpinnerAdapter(item, true);
			}
			spinner.setAdapter(adapter);
			if (spinnerPosition != -1) {
				spinner.setSelection(spinnerPosition, false);
			}

			final ImageButton button = (ImageButton)view.findViewById(R.id.editable_stringlist_deletebutton);
			button.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						removeStringItem(item.getId());
						myActivity.enableButtons();
					}
				}
			);
			button.setEnabled(getCount() > 1);
			return view;
		}

		private class SpinnerAdapter extends BaseAdapter {
			private final StringItem myItem;
			private int myPrevPosition;
			private boolean myNeedToSkip;

			public SpinnerAdapter(StringItem i, boolean needToSkip) {
				myItem = i;
				myNeedToSkip = needToSkip;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public String getItem(int position) {
				return myActivity.Suggestions.get(position);
			}

			@Override
			public synchronized int getCount() {
				return myActivity.Suggestions.size();
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				final String name;
				if (myPrevPosition != position && !myNeedToSkip) {
					name = getItem(position);
					myItem.setData(name);
					myActivity.enableButtons();

				} else {
					name = myItem.getData();
					myNeedToSkip = false;
				}
				final View view;
				view = LayoutInflater.from(myActivity).inflate(R.layout.editable_spinner_item, parent, false);
				final EditText text = (EditText)view.findViewById(R.id.editable_spinner_text);
				text.setText(name);
				text.addTextChangedListener(new TextWatcher(){
					public void afterTextChanged(Editable s) {}
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						myItem.setData(s.toString());
						myActivity.enableButtons();
					}
				});
				myPrevPosition = position;
				return view;
			}

			@Override
			public View getDropDownView(final int position, View convertView, ViewGroup parent) {
				final String name = getItem(position);
				final View view;
				view = LayoutInflater.from(myActivity).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
				final TextView text = (TextView)view.findViewById(android.R.id.text1);
				text.setText(name);
				return view;
			}

		}

	}
}
