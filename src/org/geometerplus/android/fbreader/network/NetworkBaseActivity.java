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

package org.geometerplus.android.fbreader.network;

import java.util.HashSet;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkImage;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

abstract class NetworkBaseActivity extends ListActivity 
		implements NetworkView.EventListener, View.OnCreateContextMenuListener {

	protected final ZLResource myResource = ZLResource.resource("networkView");

	public BookDownloaderServiceConnection Connection;


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		Connection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			Connection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	public void onDestroy() {
		if (Connection != null) {
			unbindService(Connection);
			Connection = null;
		}
		super.onDestroy();
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
	protected void onStop() {
		NetworkView.Instance().removeEventListener(this);
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnCreateContextMenuListener(this);
		onModelChanged(); // do the same update actions as upon onModelChanged
	}


	// method from NetworkView.EventListener
	public void onModelChanged() {
	}


	// this set is used to track whether this activity will be notified, when specific cover will be synchronized.
	private HashSet<String> myAwaitedCovers = new HashSet<String>();

	private void setupCover(final ImageView coverView, NetworkTree tree, int width, int height) {
		if (tree instanceof ZLAndroidTree) {
			coverView.setImageResource(((ZLAndroidTree)tree).getCoverResourceId());
			return;
		}

		Bitmap coverBitmap = null;
		ZLImage cover = tree.getCover();
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
			if (cover instanceof NetworkImage) {
				final NetworkImage img = (NetworkImage) cover;
				if (img.isSynchronized()) {
					data = mgr.getImageData(img);
				} else if (!myAwaitedCovers.contains(img.Url)) {
					final Runnable runnable = new Runnable() {
						public void run() {
							myAwaitedCovers.remove(img.Url);
							final ListView view = NetworkBaseActivity.this.getListView();
							view.invalidateViews();
						}
					};
					final NetworkView networkView = NetworkView.Instance();
					networkView.performCoverSynchronization(img, runnable);
					myAwaitedCovers.add(img.Url);
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


	// from View.OnCreateContextMenuListener
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final NetworkTree tree = (NetworkTree) getListAdapter().getItem(position);
		final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
		if (actions != null) {
			actions.buildContextMenu(this, menu, tree);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree) getListAdapter().getItem(position);
		final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
		if (actions != null &&
				actions.runAction(this, tree, item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		final NetworkTree networkTree = (NetworkTree) getListAdapter().getItem(position);
		final NetworkView networkView = NetworkView.Instance();
		final NetworkTreeActions actions = networkView.getActions(networkTree);
		if (actions == null) {
			return;
		}
		final int actionCode = actions.getDefaultActionCode(networkTree);
		final String confirm = actions.getConfirmText(networkTree, actionCode);
		if (actionCode == NetworkTreeActions.TREE_SHOW_CONTEXT_MENU) {
			listView.showContextMenuForChild(view);
			return;
		}
		if (actionCode < 0) {
			return;
		}
		if (confirm != null) {
			//final ZLResource resource = myResource.getResource("confirmQuestions");
			final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
			new AlertDialog.Builder(this)
				.setTitle(networkTree.getName())
				.setMessage(confirm)
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						actions.runAction(NetworkBaseActivity.this, networkTree, actionCode);
					}
				})
				.setNegativeButton(buttonResource.getResource("no").getValue(), null)
				.create().show();
		} else {
			actions.runAction(this, networkTree, actionCode);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (!NetworkView.Instance().isInitialized()) {
			return null;
		}
		final NetworkDialog dlg = NetworkDialog.getDialog(id);
		if (dlg != null) {
			return dlg.createDialog(this);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		final NetworkDialog dlg = NetworkDialog.getDialog(id);
		if (dlg != null) {
			dlg.prepareDialog(this, dialog);
		}		
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}
}
