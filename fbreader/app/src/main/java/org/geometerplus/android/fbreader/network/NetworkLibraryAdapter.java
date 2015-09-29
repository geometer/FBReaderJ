/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.network;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.android.fbreader.covers.CoverManager;

import org.geometerplus.android.fbreader.network.action.NetworkBookActions;

import org.geometerplus.android.util.ViewUtil;

class NetworkLibraryAdapter extends TreeAdapter {
	NetworkLibraryAdapter(NetworkLibraryActivity activity) {
		super(activity);
	}

	private CoverManager myCoverManager;

	public View getView(int position, View view, final ViewGroup parent) {
		final NetworkTree tree = (NetworkTree)getItem(position);
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}
		if (view == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
			if (myCoverManager == null) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				final int coverHeight = view.getMeasuredHeight();
				final TreeActivity activity = getActivity();
				myCoverManager = new CoverManager(activity, activity.ImageSynchronizer, coverHeight * 15 / 32, coverHeight);
				view.requestLayout();
			}
		}

		ViewUtil.setSubviewText(view, R.id.library_tree_item_name, tree.getName());
		ViewUtil.setSubviewText(view, R.id.library_tree_item_childrenlist, tree.getSummary());
		setupCover(ViewUtil.findImageView(view, R.id.library_tree_item_icon), tree);

		final ImageView statusView = ViewUtil.findImageView(view, R.id.library_tree_item_status);
		final int status = (tree instanceof NetworkBookTree)
			? NetworkBookActions.getBookStatus(
				((NetworkBookTree)tree).Book,
				((NetworkLibraryActivity)getActivity()).BookCollection,
				((NetworkLibraryActivity)getActivity()).Connection
			  )
			: 0;
		if (status != 0) {
			statusView.setVisibility(View.VISIBLE);
			statusView.setImageResource(status);
		} else {
			statusView.setVisibility(View.GONE);
		}
		statusView.requestLayout();

		return view;
	}

	private void setupCover(final ImageView coverView, NetworkTree tree) {
		if (myCoverManager.trySetCoverImage(coverView, tree)) {
			return;
		}

		if (tree instanceof NetworkBookTree) {
			coverView.setImageResource(R.drawable.ic_list_library_book);
		} else if (tree instanceof SearchCatalogTree) {
			coverView.setImageResource(R.drawable.ic_list_library_search);
		} else if (tree instanceof RecentCatalogListTree) {
			coverView.setImageResource(R.drawable.ic_list_library_recent);
		} else if (tree instanceof BasketCatalogTree) {
			coverView.setImageResource(R.drawable.ic_list_library_basket);
		} else if (tree instanceof AddCustomCatalogItemTree) {
			coverView.setImageResource(R.drawable.ic_list_plus);
		} else if (tree instanceof ManageCatalogsItemTree) {
			coverView.setImageResource(R.drawable.ic_menu_filter);
		} else {
			coverView.setImageResource(R.drawable.ic_list_library_books);
		}
	}
}
