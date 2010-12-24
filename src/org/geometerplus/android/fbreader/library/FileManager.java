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

import java.io.IOException;
import java.util.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
	public static String LOG = "FileManager";
	
	public static String FILE_MANAGER_INSERT_MODE = "FileManagerInsertMode";
	
	private static final int DELETE_FILE_ITEM_ID = 10;
	private static final int RENAME_FILE_ITEM_ID = 11;
	private static final int MOVE_FILE_ITEM_ID = 12;
	public static String FILE_MANAGER_PATH = "FileManagerPath";
	
	private String myPath;
	private String myInsertPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FileListAdapter adapter = new FileListAdapter();
		setListAdapter(adapter);

		myPath = getIntent().getStringExtra(FILE_MANAGER_PATH);
		myInsertPath = getIntent().getStringExtra(FILE_MANAGER_INSERT_MODE);
		
		if (myPath == null) {
			setTitle(myResource.getResource("fileTree").getValue());
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
//			addItem("/", "fileTreeRoot");	for alex version
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
		} else {
			setTitle(myPath);
			startUpdate();
		}
		if (myInsertPath != null)
			setTitle(myResource.getResource("moveTitle").getValue());

		getListView().setOnCreateContextMenuListener(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runItem(((FileListAdapter)getListAdapter()).getItem(position));
			}
		});
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (myPath != null){
			((FileListAdapter)getListAdapter()).clear();
			startUpdate();
		}
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
		FileListAdapter adapter = ((FileListAdapter)getListAdapter()); 
		final FileItem fileItem = adapter.getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			onContextItemSelected(item.getItemId(), book);
		}
		
		switch (item.getItemId()) {
			case MOVE_FILE_ITEM_ID:
				adapter.remove(fileItem);
				adapter.notifyDataSetChanged();
				Log.v(LOG, "MOVE_FILE_ITEM_ID");
				startActivityForResult(
						new Intent(this, FileManager.class)
							.putExtra(FILE_MANAGER_INSERT_MODE, fileItem.getFile().getPath()),
						FileManager.CHILD_LIST_REQUEST
				);
				return true;
			case RENAME_FILE_ITEM_ID:
				new RenameDialog(this, fileItem.getFile()).show();
				return true;
			case DELETE_FILE_ITEM_ID:
				deleteFileItem(fileItem);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private class FileDeleter implements DialogInterface.OnClickListener {
		private final FileItem myFileItem;

		FileDeleter(FileItem fileItem) {
			myFileItem = fileItem;
		}

		public void onClick(DialogInterface dialog, int which) {
			FileListAdapter adapter = (FileListAdapter)getListAdapter();
			adapter.remove(myFileItem);
			adapter.notifyDataSetChanged();
			ZLFile file = myFileItem.getFile();
			if(file != null){
				file.getPhysicalFile().delete();
			}
		}
	}
	
	private void deleteFileItem(FileItem fileItem){
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteFileBox");
		new AlertDialog.Builder(this)
			.setTitle(fileItem.getName())
			.setMessage(boxResource.getResource("message").getValue())
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new FileDeleter(fileItem))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
		
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
			Intent i = new Intent(this, FileManager.class)
				.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
				.putExtra(FILE_MANAGER_PATH, file.getPath())
				.putExtra(FILE_MANAGER_INSERT_MODE, myInsertPath);
			if (myInsertPath != null){
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
			startActivityForResult(i,CHILD_LIST_REQUEST);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (myPath != null){
            if (myInsertPath != null){
            	addMenuItem(menu, 0, "insert", R.drawable.ic_menu_sorting);
            	addMenuItem(menu, 1, "mkdir", R.drawable.ic_menu_mkdir);
            }
        }
        return true;
    }

    private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
        final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
        final MenuItem item = menu.add(0, index, Menu.NONE, label);
        item.setIcon(iconId);
        return item;
    }
    
    
    private Runnable messFileMoved = new Runnable() {
		public void run() {
			Toast.makeText(FileManager.this,
					myResource.getResource("messFileMoved").getValue(), 
					Toast.LENGTH_SHORT).show();
		}
	};
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	    	case 0:
	    		try {
	    			FileUtil.moveFile(myInsertPath, myPath);
	    			myInsertPath = null;
	    			finish();
					runOnUiThread(messFileMoved);
	    		} catch (IOException e) {
    				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    			}
	    		return true;
        	case 1:
        		new MkDirDialog(this, myPath, myInsertPath).show();
        		return true;
        	case 2:
	            return true;
        	case 3:
	            return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
    
    
	
	private final class FileListAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
		private List<FileItem> myItems = new ArrayList<FileItem>();

		public synchronized void clear() {
			myItems.clear();
		}

		public synchronized void add(FileItem item){
			myItems.add(item);
		}
		
		public synchronized void remove(FileItem fileItem) {
			myItems.remove(fileItem);
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
			if (myPath == null)
				return;
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final FileItem item = getItem(position);

			menu.setHeaderTitle(item.getName());
			if (item.getFile().isDirectory()){
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
			}else{
				final Book book = item.getBook();
				if (book != null) {
					createBookContextMenu(menu, book); 
				}
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, MOVE_FILE_ITEM_ID, 0, myResource.getResource("move").getValue());
				if (book == null) {
					menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
				}
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

	public final class FileItem {
		private final ZLFile myFile;
		private String myName;
		private final String mySummary;

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
						myFile = child;
						myName = file.getLongName();
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
			for (ZLFile file : children) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				if (file.isDirectory() || file.isArchive() ||
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
		}
	}

	private static class FileComparator implements Comparator<ZLFile> {
		public int compare(ZLFile f0, ZLFile f1) {
			return f0.getShortName().compareToIgnoreCase(f1.getShortName());
		}
	}
	
}
