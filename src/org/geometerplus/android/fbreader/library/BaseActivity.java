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

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

abstract class BaseActivity extends ListActivity 
	implements HasBaseConstants {
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
		LibraryUtil.openBook(this, book);
	}

	protected void createBookContextMenu(ContextMenu menu, Book book) {
		LibraryUtil.createBookContextMenu(menu, book, myResource);
	}

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
				getListView().invalidateViews();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}
}
