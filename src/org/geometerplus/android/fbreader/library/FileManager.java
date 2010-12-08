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
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
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

		myProgressDialog = new ProgressDialog(this);
		myProgressDialog.setTitle("Please wait...");
		myProgressDialog.setMessage("Retrieving data ...");

		final ListView fileList = getListView();
		myAdapter = new FManagerAdapter(this, myOrders, R.layout.library_tree_item);
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
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				myCurFile = ((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).getText().toString();
				if (myCurFile.substring(0, 1).equals("/"))
					myCurFile = myCurFile.substring(1);
				goAtDir(myCurDir + "/" + myCurFile);
			}
		});
	}
	
	public void goAtDir(String path) {
		Log.v(FileManager.LOG, "gotAtDir: 	" + path);
		path = normalize(path);
		
		ZLFile file = ZLFile.createFileByPath(path);
		if (file.isDirectory() || file.isArchive()) {
			myCurFile = null;
			if (myCurFilterThread != null)
				myCurFilterThread.interrupt();
			Intent i = new Intent(this, FileManager.class);
			i.putExtra(FileManager.FILE_MANAGER_PATH, path);
			i.putExtra(FileManager.FILE_MANAGER_TYPE, myTypes);
			startActivity(i);
		}
		else {
			launchFBReaderView(path);
		}
	}

	private void initfill(){
		ZLResource resource = ZLResource.resource("fmanagerView");
		String nameRoot = resource.getResource("root").getValue();
		String nameSdcard = resource.getResource("sdcard").getValue();
		String nameBooks = resource.getResource("books").getValue();
		
		String pathRoot = "/";
		String pathSdcard = Environment.getExternalStorageDirectory().getPath();
		String pathBook = Paths.BooksDirectoryOption().getValue();
		
		myOrders.add(new FileOrder(nameRoot, pathRoot, R.drawable.ic_list_library_folder));
		myOrders.add(new FileOrder(nameSdcard, pathSdcard, R.drawable.ic_list_library_folder));
		myOrders.add(new FileOrder(nameBooks, pathBook, R.drawable.ic_list_library_folder));

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

	private void launchFBReaderView(String path){
		Log.v(FileManager.LOG, "launchFBReaderView 1: 	" + path);
		
		Intent i = new Intent(this, FBReader.class);
		i.setAction(Intent.ACTION_VIEW);
		i.putExtra(FBReader.BOOK_PATH_KEY, path);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	private String normalize(String path){
		Log.v(FileManager.LOG, "normalize 1: 	" + path);

		if (ZLFile.createFileByPath(path).exists())
			return path;
		else if(path.contains(".zip")){
			int idx = path.indexOf(".zip") + 5;
			String str = path.substring(0, idx - 1) + ":" + path.substring(idx);
			path = str;
		}
		Log.v(FileManager.LOG, "normalize 2: 	" + path);
		return path;
	}
	
	public static String FILE_MANAGER_PATH = "file_manager.path";
	public static String FILE_MANAGER_TYPE = "file_manager.type";
	public static String LOG = "FileManager";

}

