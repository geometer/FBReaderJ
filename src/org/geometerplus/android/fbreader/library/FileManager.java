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

import java.util.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.UIUtil;

public final class FileManager extends BaseActivity {
	public static String FILE_MANAGER_PATH = "FileManagerPath";
	
	private String myPath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (DatabaseInstance == null || LibraryInstance == null) {
			finish();
			return;
		}

		FileListAdapter adapter = new FileListAdapter();
		setListAdapter(adapter);

		myPath = getIntent().getStringExtra(FILE_MANAGER_PATH);

		if (myPath == null) {
			setTitle(myResource.getResource("fileTree").getValue());
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
			addItem("/", "fileTreeRoot");
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
		} else {
			setTitle(myPath);
			startUpdate();
		}

		getListView().setOnCreateContextMenuListener(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runItem((FileItem)((FileListAdapter)getListAdapter()).getItem(position));
			}
		});
	}

	private void startUpdate() {
		final ZLFile file = ZLFile.createFileByPath(myPath);
		if (file != null) {
			new Thread(new SmartFilter(file)).start();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			if (myPath != null) {
				((FileListAdapter)getListAdapter()).clear();
				startUpdate();
			}
			getListView().invalidateViews();
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		} else if (requestCode == BOOK_INFO_REQUEST) {
			getListView().invalidateViews();
		}
	} 

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final FileItem fileItem = (FileItem)((FileListAdapter)getListAdapter()).getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		((FileListAdapter)getListAdapter()).remove(new FileItem(book.File));
		getListView().invalidateViews();
	}

	private void runItem(FileItem item) {
		final ZLFile file = item.getFile();
		final Book book = item.getBook();
		if (book != null) {
			showBookInfo(book);
		} else if (file.isDirectory() || file.isArchive()) {
			startActivityForResult(
				new Intent(this, FileManager.class)
					.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					.putExtra(FILE_MANAGER_PATH, file.getPath()),
				CHILD_LIST_REQUEST
			);
		} else {
			UIUtil.showErrorMessage(FileManager.this, "permissionDenied");
		}
	}

	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		((FileListAdapter)getListAdapter()).add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
	}

	@Override
	protected boolean isTreeSelected(FBTree tree) {
		final FileItem item = (FileItem)tree;

		if (mySelectedBookPath == null || !item.isSelectable()) {
			return false;
		}

		final ZLFile file = item.getFile();
		final String path = file.getPath();
		if (mySelectedBookPath.equals(path)) {
			return true;
		}

		String prefix = path;
		if (file.isDirectory()) {
			if (!prefix.endsWith("/")) {
				prefix += '/';
			}
		} else if (file.isArchive()) {
			prefix += ':';
		} else {
			return false;
		}
		return mySelectedBookPath.startsWith(prefix);
	}

	private final class FileListAdapter extends ListAdapter {
		public FileListAdapter() {
			super(FileManager.this, new ArrayList<FBTree>());
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final Book book = ((FileItem)getItem(position)).getBook();
			if (book != null) {
				createBookContextMenu(menu, book); 
			}
		}
	}

	private final class SmartFilter implements Runnable {
		private final ZLFile myFile;

		public SmartFilter(ZLFile file) {
			myFile = file;
		}

		public void run() {
			if (!myFile.isReadable()) {
				runOnUiThread(new Runnable() {
					public void run() {
						UIUtil.showErrorMessage(FileManager.this, "permissionDenied");
					}
				});
				finish();
				return;
			}

			final ArrayList<ZLFile> children = new ArrayList<ZLFile>(myFile.children());
			Collections.sort(children, new FileComparator());
			for (final ZLFile file : children) {
				if (file.isDirectory() || file.isArchive() ||
					PluginCollection.Instance().getPlugin(file) != null) {
					((FileListAdapter)getListAdapter()).add(new FileItem(file));
				}
			}
		}
	}

	private static class FileComparator implements Comparator<ZLFile> {
		public int compare(ZLFile f0, ZLFile f1) {
			final boolean isDir = f0.isDirectory();
			if (isDir != f1.isDirectory()) {
				return isDir ? -1 : 1;
			} 
			return f0.getShortName().compareToIgnoreCase(f1.getShortName());
		}
	}
}
