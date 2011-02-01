package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.library.BaseActivity.BookDeleter;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.widget.Gallery;

public class BaseGalleryActivity extends Activity 
	implements HasBaseConstants {
	
	protected final ZLResource myResource = ZLResource.resource("libraryView");
	protected String mySelectedBookPath;
	protected Gallery myGallery;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.gallery);
	    myGallery = (Gallery) findViewById(R.id.gallery);

	    Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		mySelectedBookPath = getIntent().getStringExtra(FileManager.SELECTED_BOOK_PATH_KEY);
		setResult(RESULT_DONT_INVALIDATE_VIEWS);
	}

	
	protected void openBook(Book book) {
		LibraryUtil.openBook(this, book);
	}

	protected void createBookContextMenu(ContextMenu menu, Book book) {
		LibraryUtil.createBookContextMenu(menu, book, myResource);
	}
	
	private class BookDeleter extends AbstractBookDeleter {
		BookDeleter(Book book, int removeMode) {
			super(book, removeMode);
		}

		public void onClick(DialogInterface dialog, int which) {
			deleteBook(myBook, myMode);
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		}
	}
	
	private void tryToDeleteBook(Book book) {
		LibraryUtil.tryToDeleteBook(this, book,  new BookDeleter(book, Library.REMOVE_FROM_DISK));
	}
	protected void deleteBook(Book book, int mode) {
		LibraryCommon.LibraryInstance.removeBook(book, mode);
	}

	protected void showBookInfo(Book book) {
		LibraryUtil.showBookInfo(this, book);
	}

	protected boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case OPEN_BOOK_ITEM_ID:
				openBook(book);
				return true;
			case SHOW_BOOK_INFO_ITEM_ID:
				showBookInfo(book);
				return true;
			case ADD_TO_FAVORITES_ITEM_ID:
				LibraryCommon.LibraryInstance.addBookToFavorites(book);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				LibraryCommon.LibraryInstance.removeBookFromFavorites(book);
				((FMBaseAdapter)myGallery.getAdapter()).notifyDataSetChanged();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}

}
