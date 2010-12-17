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

import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.UIUtil;

public final class FileManager extends BaseActivity {
	public static String FILE_MANAGER_PATH = "FileManagerPath";
	
	private String myPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			openBook(book);
		} else if (file.isDirectory() || file.isArchive()) {
			startActivityForResult(
				new Intent(this, FileManager.class)
					.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					.putExtra(FILE_MANAGER_PATH, file.getPath()),
				CHILD_LIST_REQUEST
			);
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
			if (mySelectedBookPath != null &&
				mySelectedBookPath.equals(item.getFile().getPath())) {
				view.setBackgroundColor(0xff808080);
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

		private Book myBook = null;
		private boolean myBookIsInitialized = false;
		private ZLImage myCover = null;
		private boolean myCoverIsInitialized = false;

		public FileItem(ZLFile file, String name, String summary) {
			myFile = file;
			myName = name;
			mySummary = summary;
		}

		public FileItem(ZLFile file) {
			if (file.isArchive() && file.getPath().endsWith(".fb2.zip")) {
				final List<ZLFile> children = file.children();
				if (children.size() == 1) {
					final ZLFile child = children.get(0);
					if (child.getPath().endsWith(".fb2")) {
						final String fileName = file.getName(false);
						myFile = child;
						myName = fileName.substring(fileName.lastIndexOf('/') + 1);
						mySummary = null;
						return;
					}
				} 
			}
			myFile = file;
			myName = null;
			mySummary = null;
		}

		public String getName() {
			if (myName != null) {
				return myName;
			}

			final String fileName = myFile.getName(false);
			return fileName.substring(fileName.lastIndexOf('/') + 1);
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

		public int getIcon() {
			if (getBook() != null) {
				return R.drawable.ic_list_library_book;
			} else if (myFile.isDirectory() || myFile.isArchive()) {
				return R.drawable.ic_list_library_folder;
			} else {
				System.err.println(
					"File " + myFile.getPath() +
					" that is not a directory, not a book and not an archive " +
					"has been found in getIcon()"
				);
				return R.drawable.ic_list_library_book;
			}
		}

		public ZLImage getCover() {
			if (!myCoverIsInitialized) {
				myCoverIsInitialized = true;
				final Book book = getBook();
				final FormatPlugin plugin = PluginCollection.Instance().getPlugin(myFile);
				if (book != null && plugin != null) {
					myCover = plugin.readCover(book);
				}
			}
			return myCover;
		}

		public ZLFile getFile() {
			return myFile;
		}

		public Book getBook() {
			if (!myBookIsInitialized) {
				myBookIsInitialized = true;
				myBook = Book.getByFile(myFile);
			}
			return myBook;
		}
	}

	private final class SmartFilter implements Runnable {
		private final ZLFile myFile;

		public SmartFilter(ZLFile file) {
			myFile = file;
		}

		public void run() {
			try {
				for (ZLFile file : myFile.children()) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					if (file.isDirectory() ||
						file.isArchive() ||
						PluginCollection.Instance().getPlugin(file) != null) {
							final FileListAdapter adapter = (FileListAdapter)getListAdapter();
							adapter.add(new FileItem(file));
//							adapter.notifyDataSetChanged();	// TODO question!
							runOnUiThread(new Runnable() {
								public void run() {
									adapter.notifyDataSetChanged();
								}
							});
					}
				}
			} catch (Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FileManager.this,
							myResource.getResource("permissionDenied").getValue(),
							Toast.LENGTH_SHORT
						).show();
					}
				});
				finish();
			}
		}
	}
}
