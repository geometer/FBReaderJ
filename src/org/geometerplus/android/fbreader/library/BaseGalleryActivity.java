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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseGalleryActivity extends Activity {
//	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";
	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
	private static final int DELETE_BOOK_ITEM_ID = 4;

	protected static final int CHILD_LIST_REQUEST = 0;
	protected static final int BOOK_INFO_REQUEST = 1;
	protected static final int RESULT_DONT_INVALIDATE_VIEWS = 0;
	protected static final int RESULT_DO_INVALIDATE_VIEWS = 1;

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
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		);
	}

	protected void createBookContextMenu(ContextMenu menu, Book book) {
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, OPEN_BOOK_ITEM_ID, 0, myResource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, myResource.getResource("showBookInfo").getValue());
		if (BaseActivity.LibraryInstance.isBookInFavorites(book)) {
			Log.v(FileManager.LOG, "LibraryInstance.isBookInFavorites(book)");
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, myResource.getResource("removeFromFavorites").getValue());
		} else {
			Log.v(FileManager.LOG, "not LibraryInstance.isBookInFavorites(book)");
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, myResource.getResource("addToFavorites").getValue());
		}
		if ((BaseActivity.LibraryInstance.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, myResource.getResource("deleteBook").getValue());
        }
	}

	
	// TODO recoding
	protected View createView(View convertView, ViewGroup parent, String name, String summary) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
		
		TextView nameTextView = (TextView)view.findViewById(R.id.library_tree_item_name);
		nameTextView.setText(name);

		TextView summaryTextView = (TextView)view.findViewById(R.id.library_tree_item_childrenlist); 
        summaryTextView.setText(summary);

        if (summary == null || summary.equals("")){
            summaryTextView.setVisibility(View.GONE);
        	nameTextView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 0.5f));
        	nameTextView.setPadding(0, 0, 0, 6);
        	nameTextView.setGravity(Gravity.CENTER_VERTICAL);
        } else {
        	summaryTextView.setVisibility(View.VISIBLE);
        	nameTextView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        }
		return view;
	}

	// TODO recoding
	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private final Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
//			getView().invalidateViews();
		}
	};

//	protected ImageView getCoverView(View parent) {
//		if (myCoverWidth == -1) {
//			parent.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//			myCoverHeight = parent.getMeasuredHeight();
//			myCoverWidth = myCoverHeight * 15 / 32;
//			parent.requestLayout();
//		}
//
//		final ImageView coverView = (ImageView)parent.findViewById(R.id.library_tree_item_icon);
//		coverView.getLayoutParams().width = myCoverWidth;
//		coverView.getLayoutParams().height = myCoverHeight;
//		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//		coverView.requestLayout();
//		return coverView;
//	}

//	protected Bitmap getCoverBitmap(ZLImage cover) {
//		if (cover == null) {
//			return null;
//		}
//
//		ZLAndroidImageData data = null;
//		final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
//		if (cover instanceof ZLLoadableImage) {
//			final ZLLoadableImage img = (ZLLoadableImage)cover;
//			if (img.isSynchronized()) {
//				data = mgr.getImageData(img);
//			} else {
//				img.startSynchronization(myInvalidateViewsRunnable);
//			}
//		} else {
//			data = mgr.getImageData(cover);
//		}
//		return data != null ? data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight) : null;
//	}

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
		BaseActivity.LibraryInstance.removeBook(book, mode);
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
				BaseActivity.LibraryInstance.addBookToFavorites(book);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				BaseActivity.LibraryInstance.removeBookFromFavorites(book);
				((FMBaseAdapter)myGallery.getAdapter()).notifyDataSetChanged();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}
}
