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

import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class SmartFilter implements Runnable {
	private final Activity myParent;
	private FManagerAdapter myAdapter;
	private final ZLFile myFile;
	
	public SmartFilter(Activity parent, FManagerAdapter adapter, ZLFile file) {
		myParent = parent;
		myAdapter = adapter;
		myFile = file;
	}
	
	public void run() {
		try {
			getItems();
		} catch (Exception e) {
			Log.e(FileManager.LOG, e.getMessage());
		}
	}
	
	private void getItems() {
		try {
			for (ZLFile file : myFile.children()) {
				if (!Thread.currentThread().isInterrupted()) {
					if (file.isDirectory() ||
						file.isArchive() ||
						PluginCollection.Instance().getPlugin(file) != null) {
							myAdapter.add(new FileItem(file));
//							myAdapter.notifyDataSetChanged();	// TODO question!
							myParent.runOnUiThread(new Runnable() {
								public void run() {
									myAdapter.notifyDataSetChanged();
								}
							});
					}
				}
			}
		} catch (Exception e) {
			myParent.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(myParent,
						ZLResource.resource("fmanagerView").getResource("permission_denied").getValue(),
						Toast.LENGTH_SHORT
					).show();
				}
			});
			myParent.finish();
		}
	}

}
