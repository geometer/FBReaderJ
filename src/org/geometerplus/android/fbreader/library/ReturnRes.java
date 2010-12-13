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

import android.app.ProgressDialog;
import android.widget.ArrayAdapter;

public class ReturnRes implements Runnable {
	private int myCurIdx = 0;
	private ProgressDialog myProgressDialog;
	private List<FileItem> myItems;
	private ArrayAdapter<FileItem> myAdapter;

	public ReturnRes(List<FileItem> items, ArrayAdapter<FileItem> adapter,
			ProgressDialog pd) {
		myItems = items;
		myAdapter = adapter;
		myProgressDialog = pd;
	}
	
	public List<FileItem> getItems(){
		return myItems;
	}
	
	public void refresh() {
		myCurIdx = 0;
		myItems.clear();
		myAdapter.clear();
		myAdapter.notifyDataSetChanged();
	}
	
	public void run() {
		if (myItems != null && myItems.size() > 0) {
			myAdapter.notifyDataSetChanged();
			int i = myCurIdx;
			for (i = myCurIdx; i < myItems.size(); i++) {
				myAdapter.add(myItems.get(i));
			}
			myCurIdx = i;
		}
		myAdapter.notifyDataSetChanged();
		myProgressDialog.dismiss();
	}
};
