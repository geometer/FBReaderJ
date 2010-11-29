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

import org.geometerplus.zlibrary.core.resources.ZLResource;
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
	
	
	// TODO new
	public static String FILE_MANAGER_PATH = "fm_path";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(FILE_MANAGER_LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_manager);
		
		// Obtain handles to UI objects
		ImageButton fbhomeButton = FileUtils.getImgBtn(this, R.id.fmanagerFBHomeButton, R.drawable.home); 
		ImageButton cardButton = FileUtils.getImgBtn(this, R.id.fmanagerCardButton, R.drawable.sdcard);
		ImageButton rootButton = FileUtils.getImgBtn(this, R.id.fmanagerRootButton, R.drawable.root);
		ImageButton filterButton = FileUtils.getImgBtn(this, R.id.fmanagerFilterButton, R.drawable.filter);
		ImageButton backButton = FileUtils.getImgBtn(this, R.id.fmanagerBackButton, R.drawable.back);
		
		Button okButton = FileUtils.getOkBtn(this, R.id.fmanagerOkButton);
		Button cancelButton = FileUtils.getCancelBtn(this, R.id.fmanagerCancelButton); 
		
		ListView fileList = (ListView) findViewById(R.id.fileList1);
		myFileListView = new FileListView(this, fileList);
		
		
		
		fbhomeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				myFileListView.goAtDir(FB_HOME_DIR);
			}
		});
		
		cardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				myFileListView.goAtDir(SDCARD_DIR);
			}
		});
		
		rootButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				myFileListView.goAtDir(ROOT_DIR);
			}
		});
		
		filterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchFilterView();
			}
		});
		
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				myFileListView.goAtBack();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				finish();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				finish();
			}
		});

		
	}

    protected void launchFilterView() {
        // TODO refact
    	Intent i = new Intent(this, FilterView.class);
        i.setAction(myFileListView.getFilterTypes());
        startActivityForResult(i, 1);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null)
			myFileListView.setFilter(data.getAction());
	}
	
	public static String FILE_MANAGER_LOG_TAG = "FileManager";
}
