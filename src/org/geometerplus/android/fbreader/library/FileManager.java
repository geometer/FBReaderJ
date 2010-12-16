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
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.UIUtil;

public final class FileManager extends BaseActivity {
	public static String FILE_MANAGER_PATH = "FileManagerPath";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FManagerAdapter adapter = new FManagerAdapter();
		setListAdapter(adapter);

		final Bundle extras = getIntent().getExtras();
		final String path = extras != null ? extras.getString(FILE_MANAGER_PATH) : null;

		if (path == null) {
			setTitle(myResource.getResource("fileTree").getValue());
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
			addItem("/", "fileTreeRoot");
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
		} else {
			setTitle(path);
			final SmartFilter filter = new SmartFilter(ZLFile.createFileByPath(path));
			new Thread(filter).start();
		}

		getListView().setOnCreateContextMenuListener(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runItem(((FManagerAdapter)getListAdapter()).getItem(position));
			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final FManagerAdapter adapter = (FManagerAdapter)getListAdapter();
		final FileItem fileItem = adapter.getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			switch (item.getItemId()) {
				case OPEN_BOOK_ITEM_ID:
					openBook(book);
					return true;
				case ADD_TO_FAVORITES_ITEM_ID:
					LibraryInstance.addBookToFavorites(book);
					return true;
				case REMOVE_FROM_FAVORITES_ITEM_ID:
					LibraryInstance.removeBookFromFavorites(book);
					getListView().invalidateViews();
					return true;
				case DELETE_BOOK_ITEM_ID:
					// TODO: implemen
					// TODO: if book is in favorites list do ((FManagerAdapter)getListAdapter())
					adapter.remove(fileItem);
					adapter.notifyDataSetChanged();
					book.File.getPhysicalFile().delete();
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	private void runItem(FileItem item) {
		final ZLFile file = item.getFile();
		final Book book = item.getBook();
		if (book != null) {
			openBook(book);
		} else if (file.isDirectory() || file.isArchive()) {
			startActivity(
				new Intent(this, FileManager.class)
					.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					.putExtra(FILE_MANAGER_PATH, file.getPath())
			);
		}
	}

	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		((FManagerAdapter)getListAdapter()).add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
	}

	private final class FManagerAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
		private List<FileItem> myItems = Collections.synchronizedList(new ArrayList<FileItem>());

		public FManagerAdapter() {
		}

		public void add(FileItem item){
			myItems.add(item);
		}

		public void remove(FileItem item){
			myItems.remove(item);
		}

		public int getCount() {
			return myItems.size();
		}

		public FileItem getItem(int position) {
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

		private int myCoverWidth = -1;
		private int myCoverHeight = -1;

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ?  convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
            FileItem item = myItems.get(position);

            ((TextView)view.findViewById(R.id.library_tree_item_name)).setText(item.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(item.getSummary());

			if (myCoverWidth == -1) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				myCoverHeight = view.getMeasuredHeight();
				myCoverWidth = myCoverHeight * 15 / 32;
				view.requestLayout();
			}

			final ImageView coverView = (ImageView)view.findViewById(R.id.library_tree_item_icon);
			coverView.getLayoutParams().width = myCoverWidth;
			coverView.getLayoutParams().height = myCoverHeight;
			coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			coverView.requestLayout();

			final Bitmap coverBitmap = getCoverBitmap(item.getCover(), myCoverWidth, myCoverHeight);
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
			this(file, null, null);
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
							final FManagerAdapter adapter = (FManagerAdapter)getListAdapter();
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
