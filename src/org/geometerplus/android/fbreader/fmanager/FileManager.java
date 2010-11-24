/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geometerplus.android.fbreader.fmanager;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public final class FileManager extends Activity {

	// **************************************************************************//
	// Enums //
	// **************************************************************************//

	// **************************************************************************//
	// Members //
	// **************************************************************************//

	FileListView myFileListView;
	
	// **************************************************************************//
	// Constructors //
	// **************************************************************************//

	// **************************************************************************//
	// Getters //
	// **************************************************************************//

	// **************************************************************************//
	// Setters //
	// **************************************************************************//

	// **************************************************************************//
	// Publics //
	// **************************************************************************//
	/**
	 * Called when the activity is first created. Responsible for initializing
	 * the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_manager);

		// Obtain handles to UI objects
		Button filterButton = (Button) findViewById(R.id.filterButton);
		Button backButton = (Button) findViewById(R.id.backButton);
		ListView fileList = (ListView) findViewById(R.id.fileList1);
	
		myFileListView = new FileListView(this, fileList);

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
		
	}

	// **************************************************************************//
	// Abstracts //
	// **************************************************************************//

	// **************************************************************************//
	// Protected //
	// **************************************************************************//
    protected void launchFilterView() {
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
    
	// **************************************************************************//
	// Privates //
	// **************************************************************************//

	// **************************************************************************//
	// Public Statics //
	// **************************************************************************//
	public static final String TAG = "FileManager";

	// **************************************************************************//
	// Private Statics //
	// **************************************************************************//

	// **************************************************************************//
	// Internal Classes //
	// **************************************************************************//
}
