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
import java.util.List;

import android.app.Activity;
import android.util.Log;

import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.R;

public class SmartFilter implements Runnable {

	private Activity myParent;
	private Runnable myAction;
//	private List<String> myOrders;

//	private File myFile;
	private String myNewTypes;
//	private List<String> myCurFiles = new ArrayList<String>();
	private List<FileOrder> myOrders;
	
	private ZLFile myFile;
	
	
	public SmartFilter(Activity parent, List<FileOrder> orders, Runnable action) {
		myParent = parent;
		myOrders = orders;
		myAction = action;
	}
	
	public void setPreferences(ZLFile file, String types) {
		myFile = file;
		myNewTypes = types;
		((ReturnRes) myAction).refresh();
	}

	public void run() {
		try {
			getOrders();
		} catch (Exception e) {
			Log.e(FileManager.LOG, e.getMessage());
		}
	}
	
	private void getOrders() {
		if (myFile == null)
			myParent.runOnUiThread(myAction);
	
		// TODO
		
		for (ZLFile file : myFile.children()) {
			if (!Thread.currentThread().isInterrupted()) {
				
				
				if (file.isDirectory()){
					// FIXME later strange dir = full path
					String name = file.getName(false).substring(file.getName(false).lastIndexOf('/') + 1);
					myOrders.add(new FileOrder(name, name, R.drawable.ic_list_library_folder));
				}
				if (PluginCollection.Instance().getPlugin(file) != null){
					myOrders.add(new FileOrder(file.getName(false), file.getName(false), R.drawable.ic_list_library_book));
				}
				myParent.runOnUiThread(myAction);
			}
		}
		myParent.runOnUiThread(myAction);
	}

	private boolean condition(File file, String types) {
		return condition(file.getName(), types);
	}

	private boolean condition(String val, String types) {
		if (types.equals(""))
			return true;
		for (String type : types.split("[\\s]+")) {
			if (val.endsWith(type))
				return true;
		}
		return false;
	}
}
