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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geometerplus.android.fbreader.library.SortingDialog.SortType;
import org.geometerplus.android.fbreader.library.ViewChangeDialog.ViewType;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public final class FileManager extends BaseActivity implements HasAdapter {
	public static String LOG = "FileManager";
	
//	public static String FILE_MANAGER_INSERT_MODE = "FileManagerInsertMode";
	
	private static final int DELETE_FILE_ITEM_ID = 10;
//	private static final int RENAME_FILE_ITEM_ID = 11; //FIXME delete later
	private static final int MOVE_FILE_ITEM_ID = 12;
	public static String FILE_MANAGER_PATH = "FileManagerPath";
	
	private String myPath;
//	private String myInsertPath;
	public static String myInsertPathStatic;
	public static SortType mySortType;
	public static ViewType myViewType;
	
	@Override 
	public FMBaseAdapter getAdapter() {
		return (FMBaseAdapter)getListAdapter();
	}
	
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
//		myInsertPath = getIntent().getStringExtra(FILE_MANAGER_INSERT_MODE);
		mySortType = SortingDialog.getOprionSortType();
		myViewType = ViewChangeDialog.getOprionViewType(); 

		// TODO 
		if (myViewType == ViewType.SKETCH){
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
		if (myInsertPathStatic != null) {
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
				Log.v(LOG, "MOVE_FILE_ITEM_ID");
				myInsertPathStatic = fileItem.getFile().getPhysicalFile().getPath();
				refresh();
				return true;
//			case RENAME_FILE_ITEM_ID:
//				new RenameDialog(this, fileItem.getFile()).show();
//				return true;
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
			for (Book book : FileUtil.getBooksList(myFileItem.getFile())){
				LibraryInstance.removeBook(book, Library.REMOVE_FROM_LIBRARY);
			}
			FMBaseAdapter adapter = getAdapter();
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

		String message;
		if (fileItem.getFile().isDirectory()){
			message = dialogResource.getResource("deleteDirBox").getResource("message").getValue();
		} else {
			message = dialogResource.getResource("deleteFileBox").getResource("message").getValue();
		}
		new AlertDialog.Builder(this)
			.setTitle(fileItem.getName())
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new FileDeleter(fileItem))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
		
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
    	Log.v(LOG, "onCreateOptionsMenu");
    	super.onCreateOptionsMenu(menu);
    	addMenuItem(menu, 0, "insert", R.drawable.ic_menu_sorting);
    	addMenuItem(menu, 1, "mkdir", R.drawable.ic_menu_mkdir);
    	addMenuItem(menu, 2, "sorting", R.drawable.ic_menu_sorting);
    	addMenuItem(menu, 3, "view", R.drawable.ic_menu_sorting);	
    	return true;
    }

    private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
        final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
        final MenuItem item = menu.add(0, index, Menu.NONE, label);
        item.setIcon(iconId);
        return item;
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(LOG, "onPrepareOptionsMenu - start");
		super.onPrepareOptionsMenu(menu);
		
		if (myInsertPathStatic == null){
			menu.findItem(0).setVisible(false).setEnabled(false);
			menu.findItem(1).setVisible(false).setEnabled(false);
        }else{
        	menu.findItem(0).setVisible(true).setEnabled(true);
			menu.findItem(1).setVisible(true).setEnabled(true);
        }
		
		Log.v(LOG, "onPrepareOptionsMenu - finish");
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
	    			FileUtil.moveFile(myInsertPathStatic, myPath);
	    			myInsertPathStatic = null;
	    			refresh();
	    			runOnUiThread(messFileMoved);
	    		} catch (IOException e) {
    				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    			}
	    		return true;
        	case 1:
        		new MkDirDialog(this, myPath, myInsertPathStatic).show();
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
    
    public void refresh(){
		startActivityForResult(
				new Intent(this, FileManager.class)
					.putExtra(FILE_MANAGER_PATH, myPath)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
				FileManager.CHILD_LIST_REQUEST
		);
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

