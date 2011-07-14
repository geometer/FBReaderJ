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

import android.content.Intent;
import android.os.Bundle;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.tree.FBTree;

public final class FileManager extends BaseActivity {
	private LibraryTree myFileItem;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final ListAdapter adapter = new ListAdapter(this, new ArrayList<FBTree>());

		myFileItem = LibraryInstance.getLibraryTree(myTreeKey);
		if (myFileItem instanceof FileItem) {
			setTitle(myTreeKey.Id);
			startUpdate();
		} else {
			setTitle(myFileItem.getName());
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
			addItem("/", "fileTreeRoot");
			addItem(Paths.cardDirectory(), "fileTreeCard");
		}

		getListView().setTextFilterEnabled(true);
	}

	private void startUpdate() {
		new Thread(new Runnable() {
			public void run() {
				((FileItem)myFileItem).update();
				getListAdapter().addAll(myFileItem.subTrees());
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
			if (myFileItem instanceof FileItem) {
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
		getListAdapter().remove(new FileItem((FileItem)myFileItem, book.File));
		getListView().invalidateViews();
	}

	private void addItem(String path, String resourceKey) {
		final ZLResource resource = Library.resource().getResource(resourceKey);
		getListAdapter().add(new FileItem(
			myFileItem,
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
	}
}
