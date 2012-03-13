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

package org.geometerplus.android.fbreader.network;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.android.fbreader.covers.CoverManager;

import org.geometerplus.android.fbreader.network.action.NetworkBookActions;

class NetworkLibraryAdapter extends TreeAdapter {
	NetworkLibraryAdapter(NetworkLibraryActivity activity) {
		super(activity);
	}

	private CoverManager myCoverManager;

	private void setSubviewText(View view, int resourceId, String text) {
		((TextView)view.findViewById(resourceId)).setText(text);
	}

	public View getView(int position, View view, final ViewGroup parent) {
		final NetworkTree tree = (NetworkTree)getItem(position);
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}
		if (view == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);
			if (myCoverManager == null) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				final int coverHeight = view.getMeasuredHeight();
				myCoverManager = new CoverManager(getActivity(), coverHeight * 15 / 32, coverHeight);
				view.requestLayout();
			}
		}

		setSubviewText(view, R.id.network_tree_item_name, tree.getName());
		setSubviewText(view, R.id.network_tree_item_childrenlist, tree.getSummary());
		setupCover((ImageView)view.findViewById(R.id.network_tree_item_icon), tree);

		final ImageView statusView = (ImageView)view.findViewById(R.id.network_tree_item_status);
		final int status = (tree instanceof NetworkBookTree)
			? NetworkBookActions.getBookStatus(
				((NetworkBookTree)tree).Book,
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
		} else if (tree instanceof BasketCatalogTree) {
			coverView.setImageResource(R.drawable.ic_list_library_basket);
		} else if (tree instanceof AddCustomCatalogItemTree) {
			coverView.setImageResource(R.drawable.ic_list_plus);
		} else {
			coverView.setImageResource(R.drawable.ic_list_library_books);
		}
	}
}
