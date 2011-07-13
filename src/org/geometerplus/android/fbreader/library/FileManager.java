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

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tree.FBTree;

public final class FileManager extends BaseActivity {
	private ZLFile myFile;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final ListAdapter adapter = new ListAdapter(this, new ArrayList<FBTree>());

		if (PATH_FILE_TREE.equals(myTreeKey.Id)) {
			myFile = null;
			setTitle(myResource.getResource(myTreeKey.Id).getValue());
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
			addItem("/", "fileTreeRoot");
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
		} else {
			myFile = ZLFile.createFileByPath(myTreeKey.Id);
			if (myFile == null) {
				finish();
				return;
			}
			setTitle(myTreeKey.Id);
			startUpdate();
		}

		getListView().setTextFilterEnabled(true);
	}

	private void startUpdate() {
		new Thread(new Runnable() {
			public void run() {
				final ArrayList<FBTree> children = new ArrayList<FBTree>();
				for (ZLFile file : myFile.children()) {
					if (file.isDirectory() || file.isArchive() ||
						PluginCollection.Instance().getPlugin(file) != null) {
						children.add(new FileItem(file));
					}
				}
				Collections.sort(children);
				getListAdapter().addAll(children);
				runOnUiThread(new Runnable() {
					public void run() {
						setSelection(getListAdapter().getFirstSelectedItemIndex());
					}
				});
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			if (myFile != null) {
				getListAdapter().clear();
				startUpdate();
			}
			getListView().invalidateViews();
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		} else if (requestCode == BOOK_INFO_REQUEST) {
			getListView().invalidateViews();
		}
	} 

	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		getListAdapter().remove(new FileItem(book.File));
		getListView().invalidateViews();
	}

	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		getListAdapter().add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
	}
}
