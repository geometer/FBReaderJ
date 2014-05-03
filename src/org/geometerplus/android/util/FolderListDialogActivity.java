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

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class FolderListDialogActivity extends ListActivity {
	interface Key {
		String FOLDER_LIST            = "folder_list.folder_list";
		String ACTIVITY_TITLE         = "folder_list.title";
		String CHOOSER_TITLE          = "folder_list.chooser_title";
		String WRITABLE_FOLDERS_ONLY  = "folder_list.writable_folders_only";
	}

	private final int ADD_NEW_DIR_POSITION = 0;

	private DirectoriesAdapter myAdapter;
	private ArrayList<String> myDirList;
	private String myChooserTitle;
	private ZLResource myResource;
	private boolean myChooseWritableDirectoriesOnly;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list_dialog);

		myDirList = getIntent().getStringArrayListExtra(Key.FOLDER_LIST);
		setTitle(getIntent().getStringExtra(Key.ACTIVITY_TITLE));
		myChooserTitle = getIntent().getStringExtra(Key.CHOOSER_TITLE);
		myChooseWritableDirectoriesOnly = getIntent().getBooleanExtra(Key.WRITABLE_FOLDERS_ONLY, true);

		myResource = ZLResource.resource("dialog").getResource("folderList");

		setupActionButtons();

		myDirList.add(ADD_NEW_DIR_POSITION, myResource.getResource("addFolder").getValue());
		setupDirectoriesAdapter(myDirList);
	}

	private void openFileChooser(int index, String dirName) {
		FileChooserUtil.runDirectoryChooser(
			this,
			index,
			myChooserTitle,
			dirName,
			myChooseWritableDirectoriesOnly
		);
	}

	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void updateDirs(int index, Intent data) {
		final String path = FileChooserUtil.pathFromData(data);
		if (!myDirList.contains(path)) {
			myDirList.set(index, path);
			myAdapter.notifyDataSetChanged();
		} else if (!path.equals(myDirList.get(index))) {
			showMessage(myResource.getResource("duplicate").getValue().replace("%s", path));
		}
	}

	private void addNewDir(Intent data) {
		final String path = FileChooserUtil.pathFromData(data);
		if (!myDirList.contains(path)) {
			myDirList.add(path);
			myAdapter.notifyDataSetChanged();
		} else {
			showMessage(myResource.getResource("duplicate").getValue().replace("%s", path));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if(requestCode != ADD_NEW_DIR_POSITION) {
				updateDirs(requestCode, data);
			} else {
				addNewDir(data);
			}
		}
	}

	private void setupDirectoriesAdapter(ArrayList<String> dirs) {
		myAdapter = new DirectoriesAdapter(this, dirs);
		setListAdapter(myAdapter);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				String dirName = (String)parent.getItemAtPosition(position);
				if (position <= 0) {
					dirName = "/";
				}
				openFileChooser(position, dirName);
			}
		});
	}

	private void setupActionButtons() {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button okButton = (Button)findViewById(R.id.folder_list_dialog_button_ok);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myDirList.remove(0);
				final Intent result = new Intent();
				result.putExtra(Key.FOLDER_LIST, myDirList);
				setResult(RESULT_OK, result);
				finish();
			}
		});
		final Button cancelButton = (Button)findViewById(R.id.folder_list_dialog_button_cancel);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	private class DirectoriesAdapter extends ArrayAdapter<String> {
		public DirectoriesAdapter(Context context, ArrayList<String> dirs) {
			super(context, R.layout.folder_list_item, dirs);
		}

		private void removeItemView(final View view, final int position) {
			if (view != null && position < getCount()) {
				myDirList.remove(position);
				myAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public View getView (final int position, View convertView, ViewGroup parent) {
			final View view = LayoutInflater.from(getContext()).inflate(R.layout.folder_list_item, parent, false);

			final String dirName = (String) getItem(position);

			((TextView)view.findViewById(R.id.folder_list_item_title)).setText(dirName);

			final ImageView deleteButton = (ImageView) view.findViewById(R.id.folder_list_item_remove);

			if (position != ADD_NEW_DIR_POSITION) {
				deleteButton.setVisibility(View.VISIBLE);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(final View v) {
						final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
						final ZLResource removeDialogResource = myResource.getResource("removeDialog");
						new AlertDialog.Builder(getContext())
							.setCancelable(false)
							.setTitle(removeDialogResource.getValue())
							.setMessage(removeDialogResource.getResource("message").getValue().replace("%s", dirName))
							.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									removeItemView(v, position);
								}
							})
							.setNegativeButton(buttonResource.getResource("cancel").getValue(), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									dialog.cancel();
								}
							}).create().show();
					}
				});
			} else {
				deleteButton.setVisibility(View.INVISIBLE);
			}

			return view;
		}
	}
}
