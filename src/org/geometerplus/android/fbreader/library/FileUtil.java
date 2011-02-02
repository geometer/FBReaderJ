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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class FileUtil {
	
	public static void copyFile(String src, String targetDirectory) throws IOException {
		final ZLResource myResource = ZLResource.resource("libraryView");

		if (ZLFile.createFileByPath(targetDirectory).isDirectory()){
			ZLFile srcFile = ZLFile.createFileByPath(src);
			String srcName = srcFile.getShortName();
			
			for (ZLFile f : ZLFile.createFileByPath(targetDirectory).children()){
				if (f.getShortName().equals(srcName))
					throw new IOException (myResource.getResource("messFileExists").getValue());
			}
			String target = targetDirectory + "/" + srcFile.getShortName(); 
			FileChannel ic = new FileInputStream(src).getChannel();
			FileChannel oc = new FileOutputStream(target).getChannel();
			ic.transferTo(0, ic.size(), oc);
			ic.close();
			oc.close();
		}else{
			throw new IOException(myResource.getResource("messInsertIntoArchive").getValue());
		}
	}
	
	public static void moveFile(String src, String targetDirectory) throws IOException {
		copyFile(src, targetDirectory);
		ZLFile.createFileByPath(src).getPhysicalFile().delete();
	}
	
	public static boolean contain(String fileName, ZLFile parent){
		for(ZLFile f : parent.children()){
			if (f.getShortName().equals(fileName))
				return true;
		}
		return false;
	}
	
	public static List<Book> getBooksList(ZLFile file){
		List<Book> books = null;
		if (file.isDirectory() || file.isArchive()){
			books = new ArrayList<Book>();
			getBooks(file, books);
		}
		return books;
	}
	
	private static void getBooks(ZLFile file, List<Book> books){
		for(ZLFile f : file.children()){
			if (PluginCollection.Instance().getPlugin(f) != null){
				books.add(Book.getByFile(f));
			} else if (f.isDirectory() || f.isArchive()) {
				getBooks(f, books);
			}
		}
	}
	
	public static ZLFile getParent(ZLFile file){
		if (file.isDirectory()){
			String path = file.getPath();
			path = path.substring(0, path.lastIndexOf("/"));
			return ZLFile.createFileByPath(path);
		}
		return ZLFile.createFileByPath(file.getParent().getPath());
	}
	
    public static void launchActivity(Activity parent, Class<?> cl, String path){
		parent.startActivityForResult(
				new Intent(parent, cl)
					.putExtra(FileManager.FILE_MANAGER_PATH, path)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
				FileManager.CHILD_LIST_REQUEST
		);
    }
    
    public static void refreshActivity(Activity parent, String path){
    	launchActivity(parent, parent.getClass(), path);
    }
    
    public static void deleteFileItem(Context parent, FileItem fileItem){
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");

		String message;
		if (fileItem.getFile().isDirectory()){
			message = dialogResource.getResource("deleteDirBox").getResource("message").getValue();
		} else {
			message = dialogResource.getResource("deleteFileBox").getResource("message").getValue();
		}
		FMBaseAdapter.HasAdapter activity = (FMBaseAdapter.HasAdapter) parent;
		new AlertDialog.Builder(parent)
			.setTitle(fileItem.getName())
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new FileDeleter(activity, fileItem))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
    }
    
}


class FileDeleter implements DialogInterface.OnClickListener {
	private final FMBaseAdapter.HasAdapter myActivity;
	private final FileItem myFileItem;

	FileDeleter(FMBaseAdapter.HasAdapter activity, FileItem fileItem) {
		myActivity = activity;
		myFileItem = fileItem;
	}

	public void onClick(DialogInterface dialog, int which) {
		for (Book book : FileUtil.getBooksList(myFileItem.getFile())){
			LibraryCommon.LibraryInstance.removeBook(book, Library.REMOVE_FROM_LIBRARY);
		}
		FMBaseAdapter adapter = myActivity.getAdapter();
		adapter.remove(myFileItem);
		adapter.notifyDataSetChanged();
		ZLFile file = myFileItem.getFile();
		if(file != null){
			file.getPhysicalFile().delete();
		}
	}
}

class FileComparator implements Comparator<ZLFile> {
	public int compare(ZLFile f0, ZLFile f1) {
		int result = -1;
		switch (LibraryCommon.SortTypeInstance) {
			case BY_NAME:
				result = compareByName(f0, f1);
				break;
			case BY_DATE:
				result = compareByDate(f0, f1);
				break;
			default:
				break;
		}
		return result; 
	}

	private int compareByName(ZLFile f0, ZLFile f1){
		if (f0.isDirectory() && !f1.isDirectory()){
			return -1;
		} else if (!f0.isDirectory() && f1.isDirectory()) {
			return 1;
		}
		return f0.getShortName().compareToIgnoreCase(f1.getShortName());
	}

	private int compareByDate(ZLFile f0, ZLFile f1){
		if (f0.isDirectory() && !f1.isDirectory()){
			return -1;
		} else if (!f0.isDirectory() && f1.isDirectory()) {
			return 1;
		}
		Date date0 = f0.getPhysicalFile().lastModified();
		Date date1 = f1.getPhysicalFile().lastModified();
		return date1.compareTo(date0);
	}
}



