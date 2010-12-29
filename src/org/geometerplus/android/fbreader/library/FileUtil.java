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
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

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
}

class FileComparator implements Comparator<ZLFile> {
	public int compare(ZLFile f0, ZLFile f1) {
		int result = -1;
		switch (FileManager.mySortType) {
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
		return date0.compareTo(date1);
	}
}
