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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.formats.PluginCollection;

import org.geometerplus.android.util.UIUtil;

public final class FileManager extends BaseActivity {
	public static String FILE_MANAGER_PATH = "FileManagerPath";
	
	private String myPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			adapter.notifyDataSetChanged();
		} else {
			setTitle(myPath);
			startUpdate();
		}

		getListView().setOnCreateContextMenuListener(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runItem(((FileListAdapter)getListAdapter()).getItem(position));
			}
		});
	}

	private void startUpdate() {
		new Thread(
			new SmartFilter(ZLFile.createFileByPath(myPath))
		).start();
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
		final FileItem fileItem = ((FileListAdapter)getListAdapter()).getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		((FileListAdapter)getListAdapter()).deleteFile(book.File);
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

	private boolean isItemSelected(FileItem item) {
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

	private final class FileListAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
		private List<FileItem> myItems = new ArrayList<FileItem>();

		public synchronized void clear() {
			myItems.clear();
		}

		public synchronized void add(FileItem item){
			myItems.add(item);
		}

		public synchronized void deleteFile(ZLFile file) {
			for (FileItem item : myItems) {
				if (file.equals(item.getFile())) {
					myItems.remove(item);
					break;
				}
			}
		}

		public synchronized int getCount() {
			return myItems.size();
		}

		public synchronized FileItem getItem(int position) {
			return myItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final Book book = getItem(position).getBook();
			if (book != null) {
				createBookContextMenu(menu, book); 
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
            final FileItem item = getItem(position);
			final View view = createView(convertView, parent, item.getName(), item.getSummary());
			if (isItemSelected(item)) {
				view.setBackgroundColor(0xff555555);
			} else {
				view.setBackgroundColor(0);
			}
			final ImageView coverView = getCoverView(view);
			final Bitmap coverBitmap = getCoverBitmap(item.getCover());

			if (coverBitmap != null) {
				coverView.setImageBitmap(coverBitmap);
			} else {
				coverView.setImageResource(item.getIcon());
			}

            return view;
		}
	}

	private final class FileItem {
		private final ZLFile myFile;
		private final String myName;
		private final String mySummary;
		private final boolean myIsSelectable;

		private ZLImage myCover = null;
		private boolean myCoverIsInitialized = false;

		public FileItem(ZLFile file, String name, String summary) {
			myFile = file;
			myName = name;
			mySummary = summary;
			myIsSelectable = false;
		}

		public FileItem(ZLFile file) {
			if (file.isArchive() && file.getPath().endsWith(".fb2.zip")) {
				final List<ZLFile> children = file.children();
				if (children.size() == 1) {
					final ZLFile child = children.get(0);
					if (child.getPath().endsWith(".fb2")) {
						myFile = child;
						myName = file.getLongName();
						mySummary = null;
						myIsSelectable = true;
						return;
					}
				} 
			}
			myFile = file;
			myName = null;
			mySummary = null;
			myIsSelectable = true;
		}

		public String getName() {
			return myName != null ? myName : myFile.getShortName();
		}

		public String getSummary() {
			if (mySummary != null) {
				return mySummary;
			}

			final Book book = getBook();
			if (book != null) {
				return book.getTitle();
			}

			return null;
		}

		public boolean isSelectable() {
			return myIsSelectable;
		}

		public int getIcon() {
			if (getBook() != null) {
				return R.drawable.ic_list_library_book;
			} else if (myFile.isDirectory()) {
				if (myFile.isReadable()) {
					return R.drawable.ic_list_library_folder;
				} else {
					return R.drawable.ic_list_library_permission_denied;
				}
			} else if (myFile.isArchive()) {
				return R.drawable.ic_list_library_zip;
			} else {
				System.err.println(
					"File " + myFile.getPath() +
					" that is not a directory, not a book and not an archive " +
					"has been found in getIcon()"
				);
				return R.drawable.ic_list_library_permission_denied;
			}
		}

		public ZLImage getCover() {
			if (!myCoverIsInitialized) {
				myCoverIsInitialized = true;
				myCover = Library.getCover(myFile);
			}
			return myCover;
		}

		public ZLFile getFile() {
			return myFile;
		}

		public Book getBook() {
			return Book.getByFile(myFile);
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
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				if (file.isDirectory() || file.isArchive() ||
					PluginCollection.Instance().getPlugin(file) != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							final FileListAdapter adapter = (FileListAdapter)getListAdapter();
							adapter.add(new FileItem(file));
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		}
	}

	private static class FileComparator implements Comparator<ZLFile> {
		public int compare(ZLFile f0, ZLFile f1) {
			return f0.getShortName().compareToIgnoreCase(f1.getShortName());
		}
	}
}
