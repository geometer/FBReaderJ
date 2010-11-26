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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileListView {
	private static final String START_DIR = "sdcard/Books";
	
	private Activity myParent;
	private ListView myListView;
	private String myCurDir = ".";
	private String myCurFile = ".";
	private List<String> myHistory = new ArrayList<String>();
	private String myFilterTypes = "";

	// Members for dynamic loading //
	private ArrayAdapter<String> myAdapter;
	private List<String> myOrders = Collections.synchronizedList(new ArrayList<String>());
	private SmartFilter myFilter;
	private ReturnRes myReturnRes;
	private Thread myCurFilterThread;
	private ProgressDialog myProgressDialog;
	

	public FileListView(Activity parent, ListView listView) {
		myParent = parent;
		myListView = listView;

		// set parameters ProgressDialog
		myProgressDialog = new ProgressDialog(myParent);
		myProgressDialog.setTitle("Please wait...");
		myProgressDialog.setMessage("Retrieving data ...");
		
		myAdapter = new ArrayAdapter<String>(myParent, R.layout.list_item);
		myListView.setAdapter(myAdapter);
		myReturnRes = new ReturnRes(myOrders, myAdapter, myProgressDialog);
		myFilter = new SmartFilter(myParent, myOrders, myReturnRes);
		
		init(myCurDir);
		
		myListView.setTextFilterEnabled(true);
		myListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
				
				myCurFile = ((TextView) view).getText().toString();
				if (new File(myCurDir + "/" + myCurFile).isDirectory())
					myHistory.add(myCurFile);
				goAtDir(myCurDir + "/" + myCurFile);
			}
		});

	}

	public ListView getListView() {
		return myListView;
	}

	public String getFilterTypes(){
		return myFilterTypes;
	}

	public String getPathToFile(){
		if (myCurDir.equals(myCurFile))
			return null;
		return myCurDir + "/" + myCurFile;
	}
	
	public void goAtBack(){
		back();
	}
	
	public void setFilter(String filterTypes){
		if (!myFilterTypes.equals(filterTypes)){
			myFilterTypes = filterTypes;
			goAtDir(myCurDir);
		}
	}
	
	private void back(){
		if (myHistory.size() > 0){
			
		// TODO delete later 
			for (String s : myHistory){
				Log.v(FileManager.FILE_MANAGER_LOG_TAG, "histiry : " + s);
			}
			
			String dir = myHistory.remove(myHistory.size() - 1);
			myCurDir = myCurDir.substring(0, myCurDir.length() - dir.length() - 1);
			goAtDir(myCurDir);
		}
	}
	
	private void init(String path){
		myProgressDialog.show();
		File file = new File(path);
		myCurDir = path;
		myFilter.setPreferences(file, myFilterTypes);
		myCurFilterThread = new Thread(null, myFilter, "MagentoBackground");
		myCurFilterThread.start();
		
		for(String dir : START_DIR.split("[\\/]+")){
			myCurFile = dir;
			myCurDir += "/" + dir;
			
			Log.v(FileManager.FILE_MANAGER_LOG_TAG, "file: " + myCurFile + "\t dir: " + myCurDir);

			myHistory.add(myCurFile);
			goAtDir(myCurDir);
		}
	}
	
	public void goAtDir(String path) {
		if (new File(path).isDirectory()){

			myProgressDialog.show();
			File file = new File(path);
			myCurDir = path;
			myCurFilterThread.interrupt();
			myFilter.setPreferences(file, myFilterTypes);
			myCurFilterThread = new Thread(null, myFilter, "MagentoBackground");
			myCurFilterThread.start();
		}
	}
	
}
