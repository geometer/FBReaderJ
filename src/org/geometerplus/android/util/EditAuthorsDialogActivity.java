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

public class EditAuthorsDialogActivity extends ListActivity {
	public static final int REQ_CODE = 001;
	public interface Key {
		final String AUTHOR_LIST			= "edit_authors.author_list";
		final String ACTIVITY_TITLE         = "edit_authors.title";
	}

	private ArrayList<String> myAuthorList;
	private ZLResource myResource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_authors_dialog);

		final Intent intent = getIntent();
		myAuthorList = intent.getStringArrayListExtra(Key.AUTHOR_LIST);
		setTitle(intent.getStringExtra(Key.ACTIVITY_TITLE));
		myResource = ZLResource.resource("dialog").getResource("editAuthors");

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button okButton = (Button)findViewById(R.id.edit_authors_dialog_button_ok);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent().putExtra(Key.AUTHOR_LIST, myAuthorList));
				finish();
			}
		});
		final Button cancelButton = (Button)findViewById(R.id.edit_authors_dialog_button_cancel);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		final AutoCompleteTextView inputField = (AutoCompleteTextView)findViewById(R.id.edit_authors_input_field);
		inputField.setHint(myResource.getResource("addAuthor").getValue());
		inputField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction (TextView v, int actionId, KeyEvent event){
				System.out.println(actionId);
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addAuthor(inputField.getText().toString());
					inputField.setText("");
					return false;
				}
				return true;
			}
		});
		String[] authorsSuggestions = {};
		inputField.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, authorsSuggestions));
		
		final AuthorsAdapter adapter = new AuthorsAdapter();
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);

		setResult(RESULT_CANCELED);
	}
	
	private void addAuthor(String tag){
		if(tag.length() != 0){
			String[] tags = tag.split(",");
			for(String s : tags){
				s = s.trim();
				if(!myAuthorList.contains(s)){
					myAuthorList.add(s);
				}
			}
			((AuthorsAdapter)getListAdapter()).notifyDataSetChanged();
		}
	}

	private void showItemRemoveDialog(final int index) {
		final ZLResource resource = myResource.getResource("removeDialog");
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		new AlertDialog.Builder(EditAuthorsDialogActivity.this)
			.setCancelable(false)
			.setTitle(resource.getValue())
			.setMessage(resource.getResource("message").getValue().replace("%s", myAuthorList.get(index)))
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					myAuthorList.remove(index);
					((AuthorsAdapter)getListAdapter()).notifyDataSetChanged();
				}
			})
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
			.create().show();
	}

	private class AuthorsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		@Override
		public int getCount() {
			return myAuthorList.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public String getItem(int position) {
			return myAuthorList.get(position);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(EditAuthorsDialogActivity.this).inflate(R.layout.edit_authors_item, parent, false);

			((TextView)view.findViewById(R.id.edit_authors_item_title)).setText(getItem(position));

			final View deleteButton = view.findViewById(R.id.edit_authors_item_remove);
			
			if (myAuthorList.size() > 1) {
				deleteButton.setVisibility(View.VISIBLE);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(final View v) {
						showItemRemoveDialog(position);
					}
				});
			}else{
				deleteButton.setVisibility(View.INVISIBLE);
			}

			return view;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		}
	}
}
