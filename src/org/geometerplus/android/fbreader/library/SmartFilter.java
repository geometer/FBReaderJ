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

public class SmartFilter implements Runnable {

	private Activity myParent;
	private Runnable myAction;
	private List<String> myOrders;

	private File myFile;
	private String myNewTypes;
	private List<String> myCurFiles = new ArrayList<String>();

	public SmartFilter(Activity parent, List<String> orders, Runnable action) {
		myParent = parent;
		myOrders = orders;
		myAction = action;
	}
	
	public void setPreferences(File file, String types) {
		myFile = file;
		myNewTypes = types;
		((ReturnRes) myAction).refresh();
	}

	public void run() {
		try {
			getOrders();
		} catch (Exception e) {
			Log.e(FileManager.FILE_MANAGER_LOG_TAG, e.getMessage());
		}
	}
	
	private void getOrders() {
		for (File file : myFile.listFiles()) {
			if (!Thread.currentThread().isInterrupted()) {
				String fileName = null;
				if (file.isDirectory())
					fileName = file.getName();
				else if (condition(file, myNewTypes))
					fileName = file.getName();
				if (!Thread.currentThread().isInterrupted() && fileName != null) {
					myOrders.add(fileName);
					myCurFiles.add(fileName);
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
