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

import java.io.IOException;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

public final class FileManager extends BaseActivity 
	implements HasAdapter, HasFileManagerConstants {
	private String myPath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DatabaseInstance == null || LibraryInstance == null) {
			finish();
			return;
		}
		myPath = getIntent().getStringExtra(FILE_MANAGER_PATH);
		FileListAdapter adapter = new FileListAdapter();
		setListAdapter(adapter);
		
		LibraryCommon.SortTypeInstance = SortingDialog.getOprionSortType();			// TODO move inisialization
		LibraryCommon.ViewTypeInstance = ViewChangeDialog.getOprionViewType(); 		// TODO move inisialization

		if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
			SketchGalleryActivity.launchSketchGalleryActivity(this, myPath);
			finish();
		}
		
		if (myPath == null) {
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
//			addItem("/", "fileTreeRoot");	for alex version
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
			adapter.notifyDataSetChanged();
		} else {
			startUpdate();
		}

		getListView().setOnCreateContextMenuListener(adapter);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runItem(getAdapter().getItem(position));
			}
		});
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (myPath != null){
			getAdapter().clear();
			startUpdate();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
			SketchGalleryActivity.launchSketchGalleryActivity(this, myPath);
			finish();
			return;
		}

		if (FMCommon.InsertPath != null) {
			setTitle(myResource.getResource("moveTitle").getValue());
		} else if (myPath == null) {
			setTitle(myResource.getResource("fileTree").getValue());
		} else {
			setTitle(myPath);
		}
	}

	private void startUpdate() {
		new Thread(
			new SmartFilter(this, ZLFile.createFileByPath(myPath))
		).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			if (myPath != null) {
				getAdapter().clear();
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
		final FileItem fileItem = getAdapter().getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			onContextItemSelected(item.getItemId(), book);
		}
		
		switch (item.getItemId()) {
			case MOVE_FILE_ITEM_ID:
				FMCommon.InsertPath = fileItem.getFile().getPhysicalFile().getPath();
				FileUtil.refreshActivity(this, myPath);
				return true;
//			case RENAME_FILE_ITEM_ID:
//				new RenameDialog(this, fileItem.getFile()).show();
//				return true;
			case DELETE_FILE_ITEM_ID:
				FileUtil.deleteFileItem(this, fileItem);
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		getAdapter().deleteFile(book.File);
		getListView().invalidateViews();
	}

	public void runItem(FileItem item) {
		final ZLFile file = item.getFile();
		final Book book = item.getBook();
		if (book != null) {
			showBookInfo(book);
		} else if (file.isDirectory() || file.isArchive()) {
			Intent i = new Intent(this, FileManager.class)
				.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
				.putExtra(FILE_MANAGER_PATH, file.getPath());
			startActivityForResult(i,CHILD_LIST_REQUEST);
		} else {
			UIUtil.showErrorMessage(FileManager.this, "permissionDenied");
		}
	}

	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		getAdapter().add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.v(FMCommon.LOG, "onCreateOptionsMenu");
    	super.onCreateOptionsMenu(menu);
    	FileUtil.addMenuItem(menu, 0, myResource, "insert", R.drawable.ic_menu_sorting);
    	FileUtil.addMenuItem(menu, 1, myResource, "mkdir", R.drawable.ic_menu_mkdir);
    	FileUtil.addMenuItem(menu, 2, myResource, "sorting", R.drawable.ic_menu_sorting);
    	FileUtil.addMenuItem(menu, 3, myResource, "view", R.drawable.ic_menu_sorting);	
    	return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(FMCommon.LOG, "onPrepareOptionsMenu - start");
		super.onPrepareOptionsMenu(menu);
		
		if (FMCommon.InsertPath == null){
			menu.findItem(0).setVisible(false).setEnabled(false);
			menu.findItem(1).setVisible(false).setEnabled(false);
        }else{
        	menu.findItem(0).setVisible(true).setEnabled(true);
			menu.findItem(1).setVisible(true).setEnabled(true);
        }
		
		Log.v(FMCommon.LOG, "onPrepareOptionsMenu - finish");
		return true;
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
	    			FileUtil.moveFile(FMCommon.InsertPath, myPath);
	    			FMCommon.InsertPath = null;
	    			FileUtil.refreshActivity(this, myPath);
	    			runOnUiThread(messFileMoved);
	    		} catch (IOException e) {
    				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    			}
	    		return true;
        	case 1:
        		new MkDirDialog(this, myPath).show();
        		return true;
        	case 2:
        		new SortingDialog(this, myPath).show();
	            return true;
        	case 3:
        		new ViewChangeDialog(this, myPath).show();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
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

	@Override 
	public FMBaseAdapter getAdapter() {
		return (FMBaseAdapter)getListAdapter();
	}
	
	public static void launchFileManagerActivity(Context context, String path){
		Intent i = new Intent(context, FileManager.class)
			.putExtra(FILE_MANAGER_PATH, path)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		((Activity) context).startActivity(i);
	}
	
	private final class FileListAdapter extends FMBaseAdapter {

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

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			if (myPath == null)
				return;
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final FileItem item = getItem(position);

			menu.setHeaderTitle(item.getName());
			if (item.getFile().isDirectory()){
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				//menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
			}else{
				final Book book = item.getBook();
				if (book != null) {
					createBookContextMenu(menu, book); 
				}
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				//menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, MOVE_FILE_ITEM_ID, 0, myResource.getResource("move").getValue());
				if (book == null) {
					menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
				}
			}
		}
	}
}

interface HasAdapter{
	FMBaseAdapter getAdapter();
}

