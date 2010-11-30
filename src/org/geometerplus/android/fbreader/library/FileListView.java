/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileListView {
	private Activity myParent;
	private String myCurDir = ".";
	private String myCurFile = null;
	private String myTypes = "";

	// Members for dynamic loading //
	private ArrayAdapter<String> myAdapter;
	private List<String> myOrders = Collections.synchronizedList(new ArrayList<String>());
	private SmartFilter myFilter;
	private ReturnRes myReturnRes;
	private Thread myCurFilterThread;
	private ProgressDialog myProgressDialog;
	
	public FileListView(Activity parent, ListView listView) {
		myParent = parent;

		// set parameters ProgressDialog
		myProgressDialog = new ProgressDialog(myParent);
		myProgressDialog.setTitle("Please wait...");
		myProgressDialog.setMessage("Retrieving data ...");
		
		myAdapter = new ArrayAdapter<String>(myParent, R.layout.list_item);
		listView.setAdapter(myAdapter);
		myReturnRes = new ReturnRes(myOrders, myAdapter, myProgressDialog);
		myFilter = new SmartFilter(myParent, myOrders, myReturnRes);

		myCurDir = myParent.getIntent().getExtras().getString(FileManager.FILE_MANAGER_PATH);
		myTypes = myParent.getIntent().getExtras().getString(FileManager.FILE_MANAGER_TYPE);
		fill(myCurDir);
		
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
		
				myCurFile = ((TextView) view).getText().toString();
				goAtDir(myCurDir + "/" + myCurFile);
			}
		});
	}

	public String getTypes(){
		return myTypes;
	}

	public String getPathToFile(){
		return myCurFile != null ? myCurDir + "/" + myCurFile : null;
	}
	
	public void setFilter(String filterTypes){
		if (!myTypes.equals(filterTypes)){
			myTypes = filterTypes;
			goAtDir(myCurDir);
		}
	}
	
	public void back(){
		String[] dirs = myCurDir.split("[\\/]+"); 
		if (dirs.length > 1){
			String dir = dirs[dirs.length - 1];
			myCurDir = myCurDir.substring(0, myCurDir.length() - dir.length() - 1);
			goAtDir(myCurDir);
		}
	}
	
	public void goAtDir(String path) {
		if (new File(path).isDirectory()){
			myCurFile = null;
			myCurFilterThread.interrupt();
			Intent i = new Intent(myParent, FileManager.class);
			i.putExtra(FileManager.FILE_MANAGER_PATH, path);
			i.putExtra(FileManager.FILE_MANAGER_TYPE, myTypes);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		    myParent.startActivity(i);
		}
	}
	
	private void fill(String path){
		myProgressDialog.show();
		File file = new File(path);
		myCurDir = path;
		myFilter.setPreferences(file, myTypes);
		myCurFilterThread = new Thread(null, myFilter, "MagentoBackground");
		myCurFilterThread.start();
	}
}
