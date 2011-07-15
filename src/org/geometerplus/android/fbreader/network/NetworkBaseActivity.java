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

package org.geometerplus.android.fbreader.network;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.Intent;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.tree.AddCustomCatalogItemTree;
import org.geometerplus.fbreader.network.tree.SearchItemTree;

abstract class NetworkBaseActivity extends ListActivity implements NetworkView.EventListener {
	protected static final int BASIC_AUTHENTICATION_CODE = 1;
	protected static final int CUSTOM_AUTHENTICATION_CODE = 2;
	protected static final int SIGNUP_CODE = 3;

	public BookDownloaderServiceConnection Connection;

	private FBTree myCurrentTree;

	protected FBTree getCurrentTree() {
		return myCurrentTree;
	}

	protected void setCurrentTree(FBTree tree) {
		myCurrentTree = tree;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		SQLiteCookieDatabase.init(this);

		Connection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			Connection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * Set listener in onStart() to give descendants initialize itself in
		 * onCreate methods before onModelChanged() will be called.
		 */
		NetworkView.Instance().addEventListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnCreateContextMenuListener(this);
		onModelChanged(); // do the same update actions as upon onModelChanged
	}

	@Override
	protected void onStop() {
		NetworkView.Instance().removeEventListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (Connection != null) {
			unbindService(Connection);
			Connection = null;
		}
		super.onDestroy();
	}

	//@Override
	public boolean isTreeSelected(FBTree tree) {
		return false;
	}

	// method from NetworkView.EventListener
	public void onModelChanged() {
	}

	private final Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			getListView().invalidateViews();
		}
	};

	private void setupCover(final ImageView coverView, NetworkTree tree, int width, int height) {
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
					img.startSynchronization(myInvalidateViewsRunnable);
				}
			} else {
				data = mgr.getImageData(cover);
			}
			if (data != null) {
				coverBitmap = data.getBitmap(2 * width, 2 * height);
			}
		}
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
		} else if (tree instanceof NetworkBookTree) {
			coverView.setImageResource(R.drawable.ic_list_library_book);
		} else if (tree instanceof AddCustomCatalogItemTree) {
			coverView.setImageResource(R.drawable.ic_list_plus);
		} else if (tree instanceof SearchItemTree) {
			coverView.setImageResource(R.drawable.ic_list_searchresult);
		} else {
			coverView.setImageResource(R.drawable.ic_list_library_books);
		}
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;

	protected View setupNetworkTreeItemView(View convertView, final ViewGroup parent, NetworkTree tree) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

		((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
		((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());

		if (myCoverWidth == -1) {
			view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			myCoverHeight = view.getMeasuredHeight();
			myCoverWidth = myCoverHeight * 15 / 32;
			view.requestLayout();
		}

		final ImageView coverView = (ImageView)view.findViewById(R.id.network_tree_item_icon);
		coverView.getLayoutParams().width = myCoverWidth;
		coverView.getLayoutParams().height = myCoverHeight;
		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		coverView.requestLayout();
		setupCover(coverView, tree, myCoverWidth, myCoverWidth);

		final ImageView statusView = (ImageView)view.findViewById(R.id.network_tree_item_status);
		final int status = (tree instanceof NetworkBookTree) ?
				NetworkBookActions.getBookStatus(((NetworkBookTree) tree).Book, Connection) : 0;
		if (status != 0) {
			statusView.setVisibility(View.VISIBLE);
			statusView.setImageResource(status);
		} else {
			statusView.setVisibility(View.GONE);
		}
		statusView.requestLayout();

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (menuInfo != null) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
			if (tree != null) {
				final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
				if (actions != null) {
					actions.buildContextMenu(this, menu, tree);
					return;
				}
			}
		}
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item != null && item.getMenuInfo() != null) {
			final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
			final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
			if (tree != null) {
				final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
				if (actions != null && actions.runAction(this, tree, item.getItemId())) {
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		final NetworkTree networkTree = (NetworkTree)getListAdapter().getItem(position);
		final NetworkView networkView = NetworkView.Instance();
		final NetworkTreeActions actions = networkView.getActions(networkTree);
		if (actions == null) {
			return;
		}
		final int actionCode = actions.getDefaultActionCode(this, networkTree);
		if (actionCode == NetworkTreeActions.TREE_SHOW_CONTEXT_MENU) {
			listView.showContextMenuForChild(view);
			return;
		}
		if (actionCode < 0) {
			return;
		}
		actions.runAction(this, networkTree, actionCode);
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}
}
