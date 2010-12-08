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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public final class FileManager extends ListActivity {
	private String myCurDir = "/";
	private String myCurFile = null;
	private String myTypes = "";

	private SmartFilter myFilter;
	private ReturnRes myReturnRes;
	private Thread myCurFilterThread;
	private ProgressDialog myProgressDialog;
	
	private FManagerAdapter myAdapter;
	private List<FileOrder> myOrders = Collections.synchronizedList(new ArrayList<FileOrder>());
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG, "onCreate()");
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.fmanager);

		myProgressDialog = new ProgressDialog(this);
		myProgressDialog.setTitle("Please wait...");
		myProgressDialog.setMessage("Retrieving data ...");

		
		
		//ListView fileList = (ListView) findViewById(R.id.fileList1);
		final ListView fileList = getListView();
		myAdapter = new FManagerAdapter(this, myOrders, R.layout.library_tree_item);
		//fileList.setAdapter(myAdapter);
		setListAdapter(myAdapter);

		myReturnRes = new ReturnRes(myOrders, myAdapter, myProgressDialog);
		myFilter = new SmartFilter(this, myOrders, myReturnRes);
		
		myCurDir = getIntent().getExtras().getString(FileManager.FILE_MANAGER_PATH);
		myTypes = getIntent().getExtras().getString(FileManager.FILE_MANAGER_TYPE);
		
		if (myCurDir.equals("")) {
			initfill();
		} else {
			fill(myCurDir);
		}
			
		fileList.setTextFilterEnabled(true);
		fileList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
		
				myCurFile = ((TextView)view.findViewById(R.id.library_tree_item_name)).getText().toString();
				if (myCurFile.substring(0, 1).equals("/"))
					myCurFile = myCurFile.substring(1);
				goAtDir(myCurDir + "/" + myCurFile);
			}
		});
	}
	
	public void goAtDir(String path) {
		Log.v(FileManager.LOG, "gotAtDir :" + path);
//		File file = new File(path);

		ZLFile file = ZLFile.createFileByPath(path);
		
		if (file.isDirectory()) {
			myCurFile = null;
			if (myCurFilterThread != null)
				myCurFilterThread.interrupt();
			Intent i = new Intent(this, FileManager.class);
			i.putExtra(FileManager.FILE_MANAGER_PATH, path);
			i.putExtra(FileManager.FILE_MANAGER_TYPE, myTypes);
			startActivity(i);
		} else {
			launchFBReaderView(path);
		}
	}

	private void initfill(){
		myOrders.add(new FileOrder(FileManager.ROOT_DIR, FileManager.ROOT_DIR, R.drawable.root));
		myOrders.add(new FileOrder(FileManager.SDCARD_DIR, FileManager.SDCARD_DIR, R.drawable.sdcard));
		myOrders.add(new FileOrder(FileManager.FB_HOME_DIR, FileManager.FB_HOME_DIR, R.drawable.home));

		for (FileOrder o : myOrders){
			myAdapter.add(o);
		}
	}
	
	private void fill(String path){
		myProgressDialog.show();
		ZLFile file = ZLFile.createFileByPath(path);
		myCurDir = path;
		myFilter.setPreferences(file, myTypes);
		myCurFilterThread = new Thread(null, myFilter, "MagentoBackground");
		myCurFilterThread.start();
	}

	public void setFilter(String filterTypes){
		if (!myTypes.equals(filterTypes)){
			myTypes = filterTypes;
			goAtDir(myCurDir);
		}
	}
	
	public static String ROOT_DIR = "/"; //Environment.getRootDirectory().getPath();
	public static String SDCARD_DIR = Environment.getExternalStorageDirectory().getPath();
//	public static String FB_HOME_DIR = SDCARD_DIR + "/" + "Books";
	public static String FB_HOME_DIR = Paths.BooksDirectoryOption().getValue();

	
	public static String FILE_MANAGER_PATH = "file_manager.path";
	public static String FILE_MANAGER_TYPE = "file_manager.type";
	public static String LOG = "FileManager";

	
	
	
	
	
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (data != null)
//			myFileListView.setFilter(data.getAction());
//	}
//	

	private void launchFBReaderView(String path){
		if (path != null){
			Intent i = new Intent(this, FBReader.class);
			i.setAction(Intent.ACTION_VIEW);
			i.putExtra(FBReader.BOOK_PATH_KEY, path);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
	}
//	
//    private void launchFilterView() {
//    	Intent i = new Intent(this, FilterView.class);
//        i.putExtra(FILE_MANAGER_TYPE, myFileListView.getTypes());
//        startActivityForResult(i, 1);
//    }
//    
//    private void setPathListener(ImageButton imgBtn, final String path){
//    	imgBtn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				myFileListView.goAtDir(path);
//			}
//		});
//    }
	
}

