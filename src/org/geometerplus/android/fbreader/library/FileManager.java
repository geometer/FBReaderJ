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

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

public final class FileManager extends Activity {
	private static String FB_HOME_DIR = "./sdcard/Books";
	private static String ROOT_DIR = ".";
	private static String SDCARD_DIR = "./sdcard";
	
	private FileListView myFileListView;

	public static String FILE_MANAGER_PATH = "file_manager.path";
	public static String FILE_MANAGER_TYPE = "file_manager.type";
	public static String DEFAULT_START_PATH = FB_HOME_DIR;
	public static String FILE_MANAGER_LOG_TAG = "FileManager";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(FILE_MANAGER_LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_manager);
		
		ImageButton fbhomeButton = FileUtils.getImgBtn(this, R.id.fmanagerFBHomeButton, R.drawable.home); 
		ImageButton cardButton = FileUtils.getImgBtn(this, R.id.fmanagerCardButton, R.drawable.sdcard);
		ImageButton rootButton = FileUtils.getImgBtn(this, R.id.fmanagerRootButton, R.drawable.root);
		ImageButton filterButton = FileUtils.getImgBtn(this, R.id.fmanagerFilterButton, R.drawable.filter);
		ImageButton backButton = FileUtils.getImgBtn(this, R.id.fmanagerBackButton, R.drawable.back);
		
		Button okButton = FileUtils.getOkBtn(this, R.id.fmanagerOkButton);
		Button cancelButton = FileUtils.getCancelBtn(this, R.id.fmanagerCancelButton); 
		
		ListView fileList = (ListView) findViewById(R.id.fileList1);
		myFileListView = new FileListView(this, fileList);
	
		setPathListener(fbhomeButton, FB_HOME_DIR);
		setPathListener(cardButton, ROOT_DIR);
		setPathListener(rootButton, SDCARD_DIR);
		
		filterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchFilterView();
			}
		});
		
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myFileListView.back();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchFBReaderView();
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null)
			myFileListView.setFilter(data.getAction());
	}
	
	private void launchFBReaderView(){
		String path = myFileListView.getPathToFile();
		if (path != null){
			Log.v(FILE_MANAGER_LOG_TAG, "paht to book : " + path); // TODO delete later
			path = path.substring(1);
			Intent i = new Intent(this, FBReader.class);
			i.setAction(Intent.ACTION_VIEW);
			i.putExtra(FBReader.BOOK_PATH_KEY, path);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
		else
			finish();
	}
	
    private void launchFilterView() {
    	Intent i = new Intent(this, FilterView.class);
        i.putExtra(FILE_MANAGER_TYPE, myFileListView.getTypes());
        startActivityForResult(i, 1);
    }
    
    private void setPathListener(ImageButton imgBtn, final String path){
    	imgBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myFileListView.goAtDir(path);
			}
		});
    }
}

