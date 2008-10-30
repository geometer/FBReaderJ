/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
import android.text.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.ZLKeyOptionEntry;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

class ZLAndroidKeyOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private EditText myEditor;
	private Spinner mySpinner;

	protected ZLAndroidKeyOptionView(ZLAndroidDialogContent tab, String name, ZLKeyOptionEntry option) {
		super(tab, name, option);
	}

	private void setKeyName(String keyName) {
		myEditor.setText(keyName);
		myTab.invalidateView();
		final ZLKeyOptionEntry keyEntry = (ZLKeyOptionEntry)myOption;
		mySpinner.setSelection(keyEntry.actionIndex(keyName));
		keyEntry.onKeySelected(keyName);
	}

	void addAndroidViews() {
		final Context context = myTab.getContext();

		if (myLabel == null) {
			myLabel = new TextView(context);
			myLabel.setText(ZLResource.resource("keyOptionView").getResource("actionFor").getValue());
			myLabel.setPadding(0, 12, 0, 12);
			myLabel.setTextSize(18);
		}
		myTab.addAndroidView(myLabel, false);

		final ZLKeyOptionEntry keyEntry = (ZLKeyOptionEntry)myOption;

		if (myEditor == null) {
			myEditor = new EditText(context) {
				protected boolean getDefaultEditable() {
					return false;
				}
    
				public boolean onKeyDown(int keyCode, KeyEvent event) {
					setKeyName(ZLAndroidKeyUtil.getKeyNameByCode(keyCode));
					return true;
				}
			};
		}
		myTab.addAndroidView(myEditor, true);

		if (myEditor.getText().length() > 0) {
			if (mySpinner == null) {
				mySpinner = new Spinner(context);
				final ComboAdapter adapter = new ComboAdapter(keyEntry.getActionNames());
				mySpinner.setAdapter(adapter);
				mySpinner.setOnItemSelectedListener(adapter);
			}
			myTab.addAndroidView(mySpinner, true);
		}
	}

	protected void reset() {
		final ZLKeyOptionEntry keyEntry = (ZLKeyOptionEntry)myOption;
		keyEntry.onReset();
		if (myEditor != null) {
			setKeyName("");
		}
	}

	protected void _onAccept() {
		((ZLKeyOptionEntry)myOption).onAccept();
		myLabel = null;
		myEditor = null;
		mySpinner = null;
	}

	private class ComboAdapter extends BaseAdapter implements Spinner.OnItemSelectedListener {
		private final ArrayList myValues;

		ComboAdapter(ArrayList values) {
			myValues = values;
		}

		public void onItemSelected(AdapterView parent, View v, int position, long id) {
			final ZLKeyOptionEntry keyEntry = (ZLKeyOptionEntry)myOption;
			keyEntry.onValueChanged(myEditor.getText().toString(), position);
		}

		public void onNothingSelected(AdapterView parent) {
		}

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				TextView textView = new TextView(parent.getContext());
				textView.setPadding(0, 12, 0, 12);
				textView.setTextSize(20);
				textView.setText((String)getItem(position));
				convertView = textView;
			}
			return convertView;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			EditText editor;
			if (convertView != null) {
				editor = (EditText)convertView;
			}	else {
				editor = new EditText(parent.getContext()) {
					protected boolean getDefaultEditable() {
						return false;
					}
				};
			}
			editor.setText((String)getItem(position));
			return editor;
		}

		public int getCount() {
			return myValues.size();
		}

		public Object getItem(int position) {
			return myValues.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean stableIds() {
			return true;
		}
	}
}
