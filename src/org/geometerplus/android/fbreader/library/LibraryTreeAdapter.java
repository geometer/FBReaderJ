/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.tree.TreeAdapter;

class LibraryTreeAdapter extends TreeAdapter {
	LibraryTreeAdapter(LibraryActivity activity) {
		super(activity);
	}

	private Bitmap getCoverBitmap(ZLImage cover) {
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

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private final Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			getActivity().getListView().invalidateViews();
		}
	};

	private ImageView getCoverView(View parent) {
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

	private View createView(View convertView, ViewGroup parent, LibraryTree tree) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

        ((TextView)view.findViewById(R.id.library_tree_item_name)).setText(tree.getName());
		((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(tree.getSummary());
		return view;
	}

	public View getView(int position, View convertView, final ViewGroup parent) {
		final LibraryTree tree = (LibraryTree)getItem(position);
		final View view = createView(convertView, parent, tree);
		if (getActivity().isTreeSelected(tree)) {
			view.setBackgroundColor(0xff555555);
		} else {
			view.setBackgroundColor(0);
		}

		final ImageView coverView = getCoverView(view);
		final Bitmap coverBitmap = getCoverBitmap(tree.getCover());
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
		} else {
			coverView.setImageResource(getCoverResourceId(tree));
		}

		return view;
	}

	private int getCoverResourceId(LibraryTree tree) {
		if (tree.getBook() != null) {
			return R.drawable.ic_list_library_book;
		} else if (tree instanceof FirstLevelTree) {
			final String id = tree.getUniqueKey().Id;
			if (Library.ROOT_FAVORITES.equals(id)) {
				return R.drawable.ic_list_library_favorites;
			} else if (Library.ROOT_RECENT.equals(id)) {
				return R.drawable.ic_list_library_recent;
			} else if (Library.ROOT_BY_AUTHOR.equals(id)) {
				return R.drawable.ic_list_library_authors;
			} else if (Library.ROOT_BY_TITLE.equals(id)) {
				return R.drawable.ic_list_library_books;
			} else if (Library.ROOT_BY_TAG.equals(id)) {
				return R.drawable.ic_list_library_tags;
			} else if (Library.ROOT_FILE_TREE.equals(id)) {
				return R.drawable.ic_list_library_folder;
			} else if (Library.ROOT_FOUND.equals(id)) {
				return R.drawable.ic_list_library_search;
			}
		} else if (tree instanceof FileTree) {
			final ZLFile file = ((FileTree)tree).getFile();
			if (file.isArchive()) {
				return R.drawable.ic_list_library_zip;
			} else if (file.isDirectory() && file.isReadable()) {
				return R.drawable.ic_list_library_folder;
			} else {
				return R.drawable.ic_list_library_permission_denied;
			}
		} else if (tree instanceof AuthorTree) {
			return R.drawable.ic_list_library_author;
		} else if (tree instanceof TagTree) {
			return R.drawable.ic_list_library_tag;
		}

		return R.drawable.ic_list_library_books;
	}
}
