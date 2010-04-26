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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.ZLBoolean3OptionEntry;

class ZLAndroidBoolean3OptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private Spinner mySpinner;

	protected ZLAndroidBoolean3OptionView(ZLAndroidDialogContent tab, String name, ZLBoolean3OptionEntry option) {
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
			mySpinner.setSelection(((ZLBoolean3OptionEntry)myOption).initialState());
		}
		myTab.addAndroidView(mySpinner, true);
	}

	protected void reset() {
		// TODO: implement
		/*
		final ZLBoolean3OptionEntry comboEntry = (ZLBoolean3OptionEntry)myOption;
		comboEntry.onReset();
		*/
		if (mySpinner != null) {
			mySpinner.setSelection(((ZLBoolean3OptionEntry)myOption).initialState());
		}
	}

	protected void _onAccept() {
		if (mySpinner != null) {
			int index = mySpinner.getSelectedItemPosition();
			if (index != -1) {
				((ZLBoolean3OptionEntry)myOption).onAccept(index);
			}
		}
	}

	private class ComboAdapter extends BaseAdapter implements Spinner.OnItemSelectedListener {
		EditText myEditor;

		public void onItemSelected(AdapterView parent, View v, int position, long id) {
			//final ZLBoolean3OptionEntry comboEntry = (ZLBoolean3OptionEntry)myOption;
			//comboEntry.onValueSelected(position);
		}

		public void onNothingSelected(AdapterView parent) {
		}

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || !(convertView instanceof TextView)) {
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
			EditText editor;
			if (convertView instanceof EditText) {
				editor = (EditText)convertView;
			} else {
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
			return 3;
		}

		public Object getItem(int position) {
			final ZLResource resource = ZLResource.resource("boolean3");
			switch (position) {
				case ZLBoolean3.B3_TRUE:
					return resource.getResource("on").getValue();
				case ZLBoolean3.B3_FALSE:
					return resource.getResource("off").getValue();
				default:
				case ZLBoolean3.B3_UNDEFINED:
					return resource.getResource("unchanged").getValue();
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean stableIds() {
			return true;
		}
	}
}
