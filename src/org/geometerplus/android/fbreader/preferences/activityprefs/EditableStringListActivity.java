/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

public class EditableStringListActivity extends BaseStringListActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		initView(new MyAdapter(this));
	}

	private static class MyAdapter extends ItemAdapter {
		public MyAdapter(BaseStringListActivity a) {
			super(a);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final StringItem item = getItem(position);
			final View view;
			view = LayoutInflater.from(myActivity).inflate(R.layout.editable_stringlist_item, parent, false);
			final AutoCompleteTextView text = (AutoCompleteTextView)view.findViewById(R.id.editable_stringlist_text);
			text.setText(item.getData());
			text.addTextChangedListener(new TextWatcher(){
				public void afterTextChanged(Editable s) {}
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					item.setData(s.toString());
					myActivity.enableButtons();
				}
			});
			text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						text.setAdapter(new ArrayAdapter<String>(myActivity,
							android.R.layout.simple_dropdown_item_1line, myActivity.Suggestions
						));
					} else {
						text.setAdapter(new ArrayAdapter<String>(myActivity,
							android.R.layout.simple_dropdown_item_1line, Collections.<String>emptyList()));
					}
				}
			});


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
	}


}
