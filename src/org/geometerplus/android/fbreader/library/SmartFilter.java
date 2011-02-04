/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;
import java.util.Collections;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.app.Activity;

public final class SmartFilter implements Runnable {
	private final Activity myActivity;
	private final ZLFile myFile;

	public SmartFilter(Activity activity, ZLFile file) {
		myActivity = activity; 
		myFile = file;
	}

	public void run() {
		if (!myFile.isReadable()) {
			myActivity.runOnUiThread(new Runnable() {
				public void run() {
					UIUtil.showErrorMessage(myActivity, "permissionDenied");
				}
			});
			myActivity.finish();
			return;
		}

		final int step = 5;
		final FMBaseAdapter adapter = ((FMBaseAdapter.HasAdapter)myActivity).getAdapter();
		final ArrayList<ZLFile> children = new ArrayList<ZLFile>(myFile.children());
		Collections.sort(children, new FileComparator());
		for (final ZLFile file : children) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			if (file.isDirectory() || file.isArchive() ||
				PluginCollection.Instance().getPlugin(file) != null) {
				myActivity.runOnUiThread(new Runnable() {
					public void run() {
						adapter.add(new FileItem(file));
						if (adapter.getCount() % step == 0){
							adapter.notifyDataSetChanged();				
						} 
					}
				});
			}
		}
		adapter.notifyDataSetChanged();			

		if (adapter.getCount() == 0){
			myActivity.runOnUiThread(new Runnable() {
				public void run() {
					ToastMaker.MakeToast(myActivity, "messEmptyDirectory");
				}
			});
			return;
		}
	}	
}

