/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageLoader;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.android.fbreader.FBReader;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

abstract class LibraryBaseActivity extends ListActivity {
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";
	static final String TREE_PATH_KEY = "TreePath";
	static final String PARAMETER_KEY = "Parameter";

	static final String PATH_FAVORITES = "favorites";
	static final String PATH_SEARCH_RESULTS = "searchResults";
	static final String PATH_RECENT = "recent";
	static final String PATH_BY_AUTHOR = "byAuthor";
	static final String PATH_BY_TAG = "byTag";

	static Library Library;

	static final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	protected final ZLResource myResource = ZLResource.resource("libraryView");
	protected String mySelectedBookPath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		mySelectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	protected static final String ACTION_FOUND = "fbreader.library.intent.FOUND";

	protected boolean runSearch(Intent intent) {
	   	final String pattern = intent.getStringExtra(SearchManager.QUERY);
		if (pattern == null || pattern.length() == 0) {
			return false;
		}
		BookSearchPatternOption.setValue(pattern);
		return Library.searchBooks(pattern).hasChildren();
	}

	protected void showNotFoundToast() {
		Toast.makeText(
			this,
			ZLResource.resource("errorMessage").getResource("bookNotFound").getValue(),
			Toast.LENGTH_SHORT
		).show();
	}

	protected final class LibraryAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
		private final List<FBTree> myItems;

		public LibraryAdapter(List<FBTree> items) {
			myItems = items;
		}

		@Override
		public final int getCount() {
			return myItems.size();
		}

		@Override
		public final FBTree getItem(int position) {
			return myItems.get(position);
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final LibraryTree tree = (LibraryTree)getItem(position);
			if (tree instanceof BookTree) {
				menu.setHeaderTitle(tree.getName());
				menu.add(0, OPEN_BOOK_ITEM_ID, 0, myResource.getResource("openBook").getValue());
				if (Library.isBookInFavorites(((BookTree)tree).Book)) {
					menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, myResource.getResource("removeFromFavorites").getValue());
				} else {
					menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, myResource.getResource("addToFavorites").getValue());
				}
				if ((Library.getRemoveBookMode(((BookTree)tree).Book) & Library.REMOVE_FROM_DISK) != 0) {
					menu.add(0, DELETE_BOOK_ITEM_ID, 0, myResource.getResource("deleteBook").getValue());
                }
			}
		}

		private int myCoverWidth = -1;
		private int myCoverHeight = -1;

		private final Runnable myInvalidateViewsRunnable = new Runnable() {
			public void run() {
				getListView().invalidateViews();
			}
		};

		public View getView(int position, View convertView, final ViewGroup parent) {
			final FBTree tree = getItem(position);
			final View view = (convertView != null) ?  convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

			((TextView)view.findViewById(R.id.library_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(tree.getSecondString());

			if (myCoverWidth == -1) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				myCoverHeight = view.getMeasuredHeight();
				myCoverWidth = myCoverHeight * 15 / 32;
				view.requestLayout();
			}

			final ImageView coverView = (ImageView)view.findViewById(R.id.library_tree_item_icon);
			coverView.getLayoutParams().width = myCoverWidth;
			coverView.getLayoutParams().height = myCoverHeight;
			coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			coverView.requestLayout();

			if (tree instanceof ZLAndroidTree) {
				coverView.setImageResource(((ZLAndroidTree)tree).getCoverResourceId());
			} else {
				Bitmap coverBitmap = null;
				ZLImage cover = tree.getCover();
				if (cover != null) {
					ZLAndroidImageData data = null;
					final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
					if (cover instanceof ZLLoadableImage) {
						final ZLLoadableImage img = (ZLLoadableImage)cover;
						if (img.isSynchronized()) {
							data = mgr.getImageData(img);
						} else {
							ZLAndroidImageLoader.Instance().startImageLoading(img, myInvalidateViewsRunnable);
						}
					} else {
						data = mgr.getImageData(cover);
					}
					if (data != null) {
						coverBitmap = data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight);
					}
				}
				if (coverBitmap != null) {
					coverView.setImageBitmap(coverBitmap);
				} else if (tree instanceof AuthorTree) {
					coverView.setImageResource(R.drawable.ic_list_library_author);
				} else if (tree instanceof TagTree) {
					coverView.setImageResource(R.drawable.ic_list_library_tag);
				} else if (tree instanceof BookTree) {
					coverView.setImageResource(R.drawable.ic_list_library_book);
				} else {
					coverView.setImageResource(R.drawable.ic_list_library_books);
				}
			}
                
			return view;
		}
	}

	protected void openBook(Book book) {
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
		);
	}

	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 1;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 2;
	private static final int DELETE_BOOK_ITEM_ID = 3;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final FBTree tree = ((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			final BookTree bookTree = (BookTree)tree;
			switch (item.getItemId()) {
				case OPEN_BOOK_ITEM_ID:
					openBook(bookTree.Book);
					return true;
				case ADD_TO_FAVORITES_ITEM_ID:
					Library.addBookToFavorites(bookTree.Book);
					return true;
				case REMOVE_FROM_FAVORITES_ITEM_ID:
					Library.removeBookFromFavorites(bookTree.Book);
					getListView().invalidateViews();
					return true;
				case DELETE_BOOK_ITEM_ID:
					// TODO: implement
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	protected class OpenTreeRunnable implements Runnable {
		private final String myTreePath;
		private final String myParameter;
		private final String mySelectedBookPath;

		public OpenTreeRunnable(String treePath, String selectedBookPath) {
			this(treePath, null, selectedBookPath);
		}

		public OpenTreeRunnable(String treePath, String parameter, String selectedBookPath) {
			myTreePath = treePath;
			myParameter = parameter;
			mySelectedBookPath = selectedBookPath;
		}

		public void run() {
			final Runnable postRunnable = new Runnable() {
				public void run() {
					startActivity(
						new Intent(LibraryBaseActivity.this, LibraryTreeActivity.class)
							.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
							.putExtra(TREE_PATH_KEY, myTreePath)
							.putExtra(PARAMETER_KEY, myParameter)
					);
				}
			};
			if (Library.hasState(Library.STATE_FULLY_INITIALIZED)) {
				postRunnable.run();
			} else {
				UIUtil.runWithMessage(LibraryBaseActivity.this, "loadingBookList",
				new Runnable() {
					public void run() {
						Library.waitForState(Library.STATE_FULLY_INITIALIZED);
					}
				},
				postRunnable);
			}
		}
	}
}
