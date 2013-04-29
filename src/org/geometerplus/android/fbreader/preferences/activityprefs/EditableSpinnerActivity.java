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
import android.util.Log;
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
			final EditText text = (EditText)view.findViewById(R.id.editable_spinner_text);
			text.setText(item.getData());
			text.addTextChangedListener(new TextWatcher(){
				public void afterTextChanged(Editable s) {}
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					item.setData(s.toString());
					myActivity.enableButtons();
				}
			});
			final ImageButton delButton = (ImageButton)view.findViewById(R.id.editable_stringlist_deletebutton);
			delButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						removeStringItem(item.getId());
						myActivity.enableButtons();
					}
				}
			);
			delButton.setEnabled(getCount() > 1);
			final ImageButton choiceButton = (ImageButton)view.findViewById(R.id.editable_spinner_button);
			choiceButton.setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View view) {
							final int pos = myActivity.Suggestions.indexOf(item.getData());
							showDialog(pos, item.getId());
						}
					}
				);
			if ("".equals(item.getData())) {
				text.requestFocus();
				if ( !myKeyboardShowed) {
					InputMethodManager imm = (InputMethodManager) myActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(0, 0);
					myKeyboardShowed = true;
				}
			}
			return view;
		}

		protected void onItemSelected(int i, int id) {
			getStringItem(id).setData(myActivity.Suggestions.get(i));
			notifyDataSetChanged();
			myActivity.enableButtons();
		}

		protected void showDialog(int pos, final int id) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
			String[] suggs = new String[myActivity.Suggestions.size()];
			for (int i = 0; i < myActivity.Suggestions.size(); ++i) {
				String cur = myActivity.Suggestions.get(i);
				int index = cur.indexOf(StringItem.Divider);
				if (index != -1) {
					suggs[i] = cur.substring(0, index);
				} else {
					suggs[i] = cur;
				}
			}
			Log.d("spinner", Integer.toString(pos));
			builder.setSingleChoiceItems(suggs, pos,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onItemSelected(which, id);
						dialog.dismiss();
					}
				});
			final Dialog dialog = builder.create();
			dialog.show();

		}
	}
}
