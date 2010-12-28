package org.geometerplus.android.fbreader.library;

import java.util.List;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.ui.android.R;

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
