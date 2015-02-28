/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.content.DialogInterface.OnClickListener;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public abstract class EditListDialogActivity extends ListActivity {
	public static final int REQ_CODE = 001;
	public interface Key {
		final String LIST					= "edit_list.list";
		final String ALL_ITEMS_LIST			= "edit_list.all_items_list";
		final String ACTIVITY_TITLE         = "edit_list.title";
	}

	protected ArrayList<String> myEditList;
	private ArrayList<String> myContextMenuItems = new ArrayList<String>();
	protected ZLResource myResource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		myEditList = intent.getStringArrayListExtra(Key.LIST);
		setTitle(intent.getStringExtra(Key.ACTIVITY_TITLE));
		setResult(RESULT_CANCELED);

		ZLResource editListResource = ZLResource.resource("dialog").getResource("editList");
		myContextMenuItems.add(editListResource.getResource("edit").getValue());
		myContextMenuItems.add(editListResource.getResource("remove").getValue());
	}
	
	protected void parseUIElements(){
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button okButton = (Button)findViewById(R.id.edit_dialog_button_ok);
		if(okButton != null){
			okButton.setText(buttonResource.getResource("ok").getValue());
			okButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setResult(RESULT_OK, new Intent().putExtra(Key.LIST, myEditList));
					finish();
				}
			});
		}
		final Button cancelButton = (Button)findViewById(R.id.edit_dialog_button_cancel);
		if(cancelButton != null){
			cancelButton.setText(buttonResource.getResource("cancel").getValue());
			cancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});
		}
	}

	protected void showItemRemoveDialog(final int index) {
		if(index < 0 || myResource == null)
			return;

		final ZLResource resource = myResource.getResource("removeDialog");
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		new AlertDialog.Builder(EditListDialogActivity.this)
			.setCancelable(false)
			.setTitle(resource.getValue())
			.setMessage(resource.getResource("message").getValue().replace("%s", myEditList.get(index)))
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					myEditList.remove(index);
					if(getListAdapter() != null)
						((BaseAdapter)getListAdapter()).notifyDataSetChanged();
				}
			})
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
			.create().show();
	}
	
	protected void showItemContextMenuDialog(final int position){
		new AlertDialog.Builder(EditListDialogActivity.this)
			.setTitle(myEditList.get(position))
			.setItems(myContextMenuItems.toArray(new String[myContextMenuItems.size()]), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					onChooseContextMenu(which, position);
				}
			}).create().show();
	}
	
	abstract protected void onChooseContextMenu(int index, int itemPosition);

	protected void onClick(int position){
		showItemContextMenuDialog(position);
	}
	
	protected void onLongClick(int position){
		//can be overriden in children
	}
	
	protected void deleteItem(int position){
		showItemRemoveDialog(position);
	}
	
	protected class EditListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
		@Override
		public int getCount() {
			return myEditList.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public String getItem(int position) {
			return myEditList.get(position);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(EditListDialogActivity.this).inflate(R.layout.edit_list_dialog_item, parent, false);

			((TextView)view.findViewById(R.id.edit_item_title)).setText(getItem(position));
			return view;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
			onClick(position);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
			onLongClick(position);
			return true;
		}
	}
}
