/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.util;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class EditTagsDialogActivity extends EditListDialogActivity {
	public static final int REQ_CODE = 001;

	private ZLResource myResource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_tags_dialog);

		myResource = ZLResource.resource("dialog").getResource("editTags");

		final EditText inputField = (EditText)findViewById(R.id.edit_tags_input_field);
		inputField.setHint(myResource.getResource("addTag").getValue());
		inputField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction (TextView v, int actionId, KeyEvent event){
				System.out.println(actionId);
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addTag(inputField.getText().toString());
					inputField.setText("");
					return false;
				}
				return true;
			}
		});
	
		parseUIElements();		

		final TagsAdapter adapter = new TagsAdapter();
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);

		setResult(RESULT_CANCELED);
	}

	private void addTag(String tag){
		if(tag.length() != 0){
			String[] tags = tag.split(",");
			for(String s : tags){
				s = s.trim();
				if(!myEditList.contains(s)){
					myEditList.add(s);
				}
			}
			((BaseAdapter)getListAdapter()).notifyDataSetChanged();
		}
	}

	private class TagsAdapter extends EditListAdapter {
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);

			final View deleteButton = view.findViewById(R.id.edit_item_remove);
			deleteButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(final View v) {
					showItemRemoveDialog(position);
				}
			});

			return view;
		}
	}
}
