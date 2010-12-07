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
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileListView {
	private Activity myParent;
	private String myCurDir = "/";
	private String myCurFile = null;
	private String myTypes = "";

	// Members for dynamic loading //
//	private ArrayAdapter<String> myAdapter;
//	private List<String> myOrders = Collections.synchronizedList(new ArrayList<String>());
	private SmartFilter myFilter;
	private ReturnRes myReturnRes;
	private Thread myCurFilterThread;
	private ProgressDialog myProgressDialog;
	
	private static final String BACK_DIR = "..";

	//new 
	private FManagerAdapter myAdapter;
	private List<FileOrder> myOrders = Collections.synchronizedList(new ArrayList<FileOrder>());
	
	public FileListView(Activity parent, ListView listView) {
		myParent = parent;

		// set parameters ProgressDialog
		myProgressDialog = new ProgressDialog(myParent);
		myProgressDialog.setTitle("Please wait...");
		myProgressDialog.setMessage("Retrieving data ...");
		
		myAdapter = myAdapter = new FManagerAdapter(myOrders, myParent, R.layout.library_ng_tree_item); // new ArrayAdapter<String>(myParent, R.layout.list_item);
		listView.setAdapter(myAdapter);

		myReturnRes = new ReturnRes(myOrders, myAdapter, myProgressDialog);
		myFilter = new SmartFilter(myParent, myOrders, myReturnRes);

		myCurDir = myParent.getIntent().getExtras().getString(FileManager.FILE_MANAGER_PATH);
		myTypes = myParent.getIntent().getExtras().getString(FileManager.FILE_MANAGER_TYPE);
		
		
		if (myCurDir == null){
			myCurDir = "";
			myOrders.add(new FileOrder(FileManager.FB_HOME_DIR, FileManager.FB_HOME_DIR, R.drawable.home));
			myOrders.add(new FileOrder(FileManager.SDCARD_DIR, FileManager.SDCARD_DIR, R.drawable.sdcard));
			myOrders.add(new FileOrder(FileManager.ROOT_DIR, FileManager.ROOT_DIR, R.drawable.root));
			initfill();
		}
		else
			fill(myCurDir);
		
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
		
//				myCurFile = ((TextView) view).getText().toString();
				myCurFile = ((TextView)view.findViewById(R.id.library_ng_tree_item_name)).getText().toString();
			
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
		File file = new File(myCurDir);
		if (file.getParent() != null){
			myCurDir = file.getParent(); 
			goAtDir(myCurDir);
		}
	}
	
	public void goAtDir(String path) {
		File file = new File(path);
		if (file.getName().equals(BACK_DIR))
			myParent.finish();
		else if (file.isDirectory()){
			myCurFile = null;
//			myCurFilterThread.interrupt();
			Intent i = new Intent(myParent, FileManager.class);
			i.putExtra(FileManager.FILE_MANAGER_PATH, path);
			i.putExtra(FileManager.FILE_MANAGER_TYPE, myTypes);
//			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // TODO delete later
		    myParent.startActivity(i);
		}
	}

	private void initfill(){
		myOrders.add(0, new FileOrder(BACK_DIR, BACK_DIR, R.drawable.back)); // FIXME critical
		for (FileOrder o : myOrders){
			myAdapter.add(o);
		}
	}
	
	private void fill(String path){
		myProgressDialog.show();
		File file = new File(path);
		myCurDir = path;
		myFilter.setPreferences(file, myTypes);
		myOrders.add(0, new FileOrder(BACK_DIR, BACK_DIR, R.drawable.back)); // FIXME critical
		myCurFilterThread = new Thread(null, myFilter, "MagentoBackground");
		myCurFilterThread.start();
	}
	
	private class FManagerAdapter extends ArrayAdapter<FileOrder>{
		List<FileOrder> myOOOrders;
		
		public FManagerAdapter(List<FileOrder> orders, Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			myOOOrders = orders;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//return super.getView(position, convertView, parent);
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) myParent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.library_ng_tree_item, null);
            }
            FileOrder order = myOOOrders.get(position);
            if (order != null) {
            	((ImageView)view.findViewById(R.id.library_ng_tree_item_icon)).setImageResource(order.getIcon());
            	((TextView)view.findViewById(R.id.library_ng_tree_item_name)).setText(order.getName());
    			((TextView)view.findViewById(R.id.library_ng_tree_item_childrenlist)).setText(order.getPath());
            }
            return view;
		}

	}
}

class FileOrder{
	private String myName;
	private String myPath;
	private int myIcon;
	
	public FileOrder(String name, String path, int icon){
		myName = name;
		myPath = path;
		myIcon = icon;
	}
	
	public String getName() {
		return myName;
	}

	public String getPath() {
		return myPath;
	}

	public int getIcon() {
		return myIcon;
	}
}