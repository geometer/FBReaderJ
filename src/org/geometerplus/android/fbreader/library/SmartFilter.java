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

import java.util.List;

import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.util.Log;

public class SmartFilter implements Runnable {
	private Activity myParent;
	private ReturnRes myReturnRes;
	private List<FileOrder> myOrders;
	private ZLFile myFile;

	public SmartFilter(Activity parent, ReturnRes returnRes) {
		myParent = parent;
		myOrders = returnRes.getOrders();
		myReturnRes = returnRes;
	}
	
	public void setPreferences(ZLFile file) {
		myFile = file;
		myReturnRes.refresh();
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
			myParent.runOnUiThread(myReturnRes);

		for (ZLFile file : myFile.children()) {
			if (!Thread.currentThread().isInterrupted()) {
				String path = file.getPath();
				String name = file.getName(false).substring(file.getName(false).lastIndexOf('/') + 1); 
				
				if (file.isDirectory())
					myOrders.add(new FileOrder(name, path, R.drawable.ic_list_library_folder));
				else if (file.isArchive())
					myOrders.add(new FileOrder(name, path, R.drawable.fbreader));
				else if (PluginCollection.Instance().getPlugin(file) != null)
					myOrders.add(new FileOrder(name, path, R.drawable.ic_list_library_book));
				myParent.runOnUiThread(myReturnRes);
			}
		}
		myParent.runOnUiThread(myReturnRes);
	}
}
