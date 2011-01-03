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

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.BookInfoActivity;

abstract class BaseActivity extends ListActivity {
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";
	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
	private static final int DELETE_BOOK_ITEM_ID = 4;

	protected static final int CHILD_LIST_REQUEST = 0;
	protected static final int BOOK_INFO_REQUEST = 1;
	protected static final int RESULT_DONT_INVALIDATE_VIEWS = 0;
	protected static final int RESULT_DO_INVALIDATE_VIEWS = 1;

	static BooksDatabase DatabaseInstance;
	static Library LibraryInstance;

	protected final ZLResource myResource = ZLResource.resource("libraryView");
	protected String mySelectedBookPath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		mySelectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
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
		if (LibraryInstance.isBookInFavorites(book)) {
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, myResource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, myResource.getResource("addToFavorites").getValue());
		}
		if ((LibraryInstance.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, myResource.getResource("deleteBook").getValue());
        }
	}

	protected View createView(View convertView, ViewGroup parent, String name, String summary) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

        ((TextView)view.findViewById(R.id.library_tree_item_name)).setText(name);
		((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(summary);
		return view;
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private final Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			getListView().invalidateViews();
		}
	};

	protected ImageView getCoverView(View parent) {
		if (myCoverWidth == -1) {
			parent.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			myCoverHeight = parent.getMeasuredHeight();
			myCoverWidth = myCoverHeight * 15 / 32;
			parent.requestLayout();
		}

		final ImageView coverView = (ImageView)parent.findViewById(R.id.library_tree_item_icon);
		coverView.getLayoutParams().width = myCoverWidth;
		coverView.getLayoutParams().height = myCoverHeight;
		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		coverView.requestLayout();
		return coverView;
	}

	protected Bitmap getCoverBitmap(ZLImage cover) {
		if (cover == null) {
			return null;
		}

		ZLAndroidImageData data = null;
		final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
		if (cover instanceof ZLLoadableImage) {
			final ZLLoadableImage img = (ZLLoadableImage)cover;
			if (img.isSynchronized()) {
				data = mgr.getImageData(img);
			} else {
				img.startSynchronization(myInvalidateViewsRunnable);
			}
		} else {
			data = mgr.getImageData(cover);
		}
		return data != null ? data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight) : null;
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
		LibraryInstance.removeBook(book, mode);
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
				LibraryInstance.addBookToFavorites(book);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				LibraryInstance.removeBookFromFavorites(book);
				getListView().invalidateViews();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}
}
