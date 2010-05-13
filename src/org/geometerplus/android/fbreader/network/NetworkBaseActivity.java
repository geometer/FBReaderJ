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

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkImage;


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

	protected void setupCover(final ImageView coverView, NetworkTree tree, int maxWidth, int maxHeight) {
		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		Bitmap coverBitmap = null;
		final ZLImage cover = tree.getCover();
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
			if (cover instanceof NetworkImage) {
				final NetworkImage img = (NetworkImage) cover;
				if (img.isSynchronized()) {
					data = mgr.getImageData(img);
				} else {
					final Runnable runnable = new Runnable() {
						public void run() {
							myAwaitedCovers.remove(img.Url);
							final ListView view = NetworkBaseActivity.this.getListView();
							view.invalidateViews();
						}
					};
					final NetworkView networkView = NetworkView.Instance();
					if (!networkView.isCoverLoading(img.Url)) {
						networkView.performCoverSynchronization(img, runnable);
						myAwaitedCovers.add(img.Url);
					} else if (!myAwaitedCovers.contains(img.Url)) {
						networkView.addCoverSynchronizationRunnable(img.Url, runnable);
						myAwaitedCovers.add(img.Url);
					}
				}
			} else {
				data = mgr.getImageData(cover);
			}
			if (data != null) {
				coverBitmap = data.getBitmap(maxWidth, maxHeight);
			}
		}
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
		} else {
			coverView.setImageDrawable(null);
		}
	}

	protected View setupNetworkTreeItemView(View convertView, final ViewGroup parent, NetworkTree tree) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

		((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
		((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());

		view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		final int maxHeight = view.getMeasuredHeight();
		final int maxWidth = maxHeight * 2 / 3;

		final ImageView iconView = (ImageView)view.findViewById(R.id.network_tree_item_icon);
		setupCover(iconView, tree, maxWidth, maxHeight);

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
			final ZLResource resource = myResource.getResource("confirmQuestions");
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
			dlg.prepareDialog(dialog);
		}		
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}
}
