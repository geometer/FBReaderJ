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
import android.view.inputmethod.InputMethodManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class EditTagsDialogActivity extends EditListDialogActivity {
	public static final int REQ_CODE = 001;

	private ZLResource myResource;
	private EditText myInputField;
	private int myEditPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_tags_dialog);

		myResource = ZLResource.resource("dialog").getResource("editTags");

		myInputField = (EditText)findViewById(R.id.edit_tags_input_field);
		myInputField.setHint(myResource.getResource("addTag").getValue());
		myInputField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction (TextView v, int actionId, KeyEvent event){
				System.out.println(actionId);
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addTag(myInputField.getText().toString(), myEditPosition);
					myInputField.setText("");
					myEditPosition = -1;
					return false;
				}
				return true;
			}
		});
	
		parseUIElements();		

		final TagsAdapter adapter = new TagsAdapter();
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
		getListView().setOnItemLongClickListener(adapter);

		setResult(RESULT_CANCELED);
	}

	private void addTag(String tag, int position){
		if(tag.length() != 0){
			String[] tags = tag.split(",");
			if(position < 0){
				for(String s : tags){
					s = s.trim();
					if(!myEditList.contains(s) && s.matches("[A-Za-z0-9_\\- ]*")){
						myEditList.add(s);
					}
				}
			}else{
				String s = tags[0].trim();
				if(s.matches("[A-Za-z0-9_\\- ]*")){
					myEditList.set(position, s);
				}
			}
			((BaseAdapter)getListAdapter()).notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onLongClick(int position){
		myEditPosition = position;
		String s = (String)getListAdapter().getItem(position);
		myInputField.setText(s);
		myInputField.setSelection(myInputField.getText().length());
		myInputField.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(myInputField, InputMethodManager.SHOW_IMPLICIT);
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
