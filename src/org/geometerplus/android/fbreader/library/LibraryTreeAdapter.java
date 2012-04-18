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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.android.fbreader.covers.CoverManager;

class LibraryTreeAdapter extends TreeAdapter {
	private CoverManager myCoverManager;

	LibraryTreeAdapter(LibraryActivity activity) {
		super(activity);
	}

	private View createView(View convertView, ViewGroup parent, LibraryTree tree) {
		final View view = (convertView != null) ? convertView :
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

		if (myCoverManager == null) {
			view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			final int coverHeight = view.getMeasuredHeight();
			myCoverManager = new CoverManager(getActivity(), coverHeight * 15 / 32, coverHeight);
			view.requestLayout();
		}

		final ImageView coverView = (ImageView)view.findViewById(R.id.library_tree_item_icon);
		if (!myCoverManager.trySetCoverImage(coverView, tree)) {
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
