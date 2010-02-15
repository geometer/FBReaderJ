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

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

class ZLAndroidComboOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private Spinner mySpinner;

	protected ZLAndroidComboOptionView(ZLAndroidDialogContent tab, String name, ZLComboOptionEntry option) {
		super(tab, name, option);
	}

	void addAndroidViews() {
		final Context context = myTab.getContext();
		if (myName != null) {
			if (myLabel == null) {
				myLabel = new TextView(context);
				myLabel.setText(myName);
				myLabel.setPadding(0, 12, 0, 12);
				myLabel.setTextSize(18);
			}
			myTab.addAndroidView(myLabel, false);
		}

		if (mySpinner == null) {
			mySpinner = new Spinner(context);
			final ComboAdapter adapter = new ComboAdapter();
			mySpinner.setAdapter(adapter);
			mySpinner.setOnItemSelectedListener(adapter);
			mySpinner.setSelection(initialIndex((ZLComboOptionEntry)myOption));
		}
		myTab.addAndroidView(mySpinner, true);
	}

	private static int initialIndex(ZLComboOptionEntry comboEntry) {
		int index = comboEntry.getValues().indexOf(comboEntry.initialValue());	
		return (index >= 0) ? index : 0;
	}

	protected void reset() {
		final ZLComboOptionEntry comboEntry = (ZLComboOptionEntry)myOption;
		comboEntry.onReset();
		if (mySpinner != null) {
			mySpinner.setSelection(initialIndex(comboEntry));
		}
	}

	protected void _onAccept() {
		if (mySpinner != null) {
			final EditText editor = ((ComboAdapter)mySpinner.getAdapter()).myEditor;
			if (editor != null) {
				((ZLComboOptionEntry)myOption).onAccept(editor.getText().toString());
			}
			myLabel = null;
			mySpinner = null;
		}
	}

	private class ComboAdapter extends BaseAdapter implements Spinner.OnItemSelectedListener {
		EditText myEditor;

		public void onItemSelected(AdapterView parent, View v, int position, long id) {
			final ZLComboOptionEntry comboEntry = (ZLComboOptionEntry)myOption;
			comboEntry.onValueSelected(position);
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
			} else {
				((TextView)convertView).setText((String)getItem(position));
			}
			return convertView;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ZLComboOptionEntry comboEntry = (ZLComboOptionEntry)myOption;
			if (convertView != null) {
				myEditor = (EditText)convertView;
			} else {
				myEditor = new EditText(parent.getContext()) {
					protected boolean getDefaultEditable() {
						return comboEntry.isEditable();
					}
				};
				myEditor.setSingleLine(true);
			}
			myEditor.setText((String)getItem(position), TextView.BufferType.EDITABLE);
			return myEditor;
		}

		public int getCount() {
			return ((ZLComboOptionEntry)myOption).getValues().size();
		}

		public Object getItem(int position) {
			return ((ZLComboOptionEntry)myOption).getValues().get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean stableIds() {
			return false;
		}
	}
}
