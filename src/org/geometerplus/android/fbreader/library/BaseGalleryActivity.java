package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.BookInfoActivity;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, OPEN_BOOK_ITEM_ID, 0, myResource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, myResource.getResource("showBookInfo").getValue());
		if (LibraryCommon.LibraryInstance.isBookInFavorites(book)) {
			Log.v(FMCommon.LOG, "LibraryInstance.isBookInFavorites(book)");
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, myResource.getResource("removeFromFavorites").getValue());
		} else {
			Log.v(FMCommon.LOG, "not LibraryInstance.isBookInFavorites(book)");
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, myResource.getResource("addToFavorites").getValue());
		}
		if ((LibraryCommon.LibraryInstance.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, myResource.getResource("deleteBook").getValue());
        }
	}
	
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;
		private final int myMode;

		BookDeleter(Book book, int removeMode) {
			myBook = book;
			myMode = removeMode;
		}

		public void onClick(DialogInterface dialog, int which) {
			deleteBook(myBook, myMode);
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		}
	}

	private void tryToDeleteBook(Book book) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
			.setTitle(book.getTitle())
			.setMessage(boxResource.getResource("message").getValue())
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book, Library.REMOVE_FROM_DISK))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	protected void deleteBook(Book book, int mode) {
		LibraryCommon.LibraryInstance.removeBook(book, mode);
	}

	protected void showBookInfo(Book book) {
		startActivityForResult(
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, book.File.getPath()),
			BOOK_INFO_REQUEST
		);
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
