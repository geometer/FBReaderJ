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

import java.util.*;

import android.app.Activity;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.view.*;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.android.fbreader.ZLTreeAdapter;


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener, NetworkView.EventListener {

	private final ZLResource myResource = ZLResource.resource("networkView");

	public BookDownloaderServiceConnection Connection;


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		Connection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			Connection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	public void onDestroy() {
		unbindService(Connection);
		Connection = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
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
		final NetworkView networkView = NetworkView.Instance();
		if (!networkView.isInitialized()) {
			final Handler handler = new Handler() {
				public void handleMessage(Message message) {
					ListView view = getListView();
					new LibraryAdapter(view, NetworkLibrary.Instance().getTree());
					view.invalidateViews();
				}
			};
			((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("loadingNetworkLibrary", new Runnable() {
				public void run() {
					networkView.initialize();
					handler.sendEmptyMessage(0);
				}
			}, this);
		}
	}


	private final class LibraryAdapter extends ZLTreeAdapter {

		LibraryAdapter(ListView view, NetworkTree tree) {
			super(view, tree);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree) getItem(position);

			/*if (tree instanceof NetworkCatalogTree || tree instanceof NetworkBookTree) {
				menu.add(0, DBG_PRINT_ENTRY_ITEM_ID, 0, "dbg - Dump Entry");
			}*/

			final NetworkView networkView = NetworkView.Instance();

			final NetworkTreeActions actions = networkView.getActions(tree);
			if (actions != null) {
				actions.buildContextMenu(NetworkLibraryActivity.this, menu, tree);
			}
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

			final NetworkTree tree = (NetworkTree)getItem(position);

			((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());

			view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			final int maxHeight = view.getMeasuredHeight();
			final int maxWidth = maxHeight * 2 / 3;

			final ImageView iconView = (ImageView)view.findViewById(R.id.network_tree_item_icon);
			iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
								final ListView view = NetworkLibraryActivity.this.getListView();
								view.invalidateViews();
								view.requestLayout();
							}
						};
						final NetworkView networkView = NetworkView.Instance();
						if (!networkView.isCoverLoading(img.Url)) {
							networkView.performCoverSynchronization(img, runnable);
						} else {
							// TODO: reduce maximum numer of runnables per image for each activity to 1
							networkView.addCoverSynchronizationRunnable(img.Url, runnable);
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
				setIcon(iconView, tree, coverBitmap);
			} else {
				setIcon(iconView, tree);
			}

			return view;
		}

		@Override
		protected boolean runTreeItem(ZLTree tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			final NetworkTree networkTree = (NetworkTree) tree;
			final NetworkView networkView = NetworkView.Instance();
			final NetworkTreeActions actions = networkView.getActions(networkTree);
			if (actions == null) {
				return false;
			}
			final int actionCode = actions.getDefaultActionCode(networkTree);
			final String confirm = actions.getConfirmText(networkTree, actionCode);
			if (actionCode == NetworkTreeActions.TREE_SHOW_CONTEXT_MENU) {
				NetworkLibraryActivity.this.getListView().showContextMenuForChild(getClickedView());
				return true;
			}
			if (actionCode < 0) {
				return false;
			}
			if (confirm != null) {
				final ZLResource resource = myResource.getResource("confirmQuestions");
				final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
				new AlertDialog.Builder(NetworkLibraryActivity.this)
					.setTitle(networkTree.getName())
					.setMessage(confirm)
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							actions.runAction(NetworkLibraryActivity.this, networkTree, actionCode);
						}
					})
					.setNegativeButton(buttonResource.getResource("no").getValue(), null)
					.create().show();
			} else {
				actions.runAction(NetworkLibraryActivity.this, networkTree, actionCode);
			}
			return true;
		}
	}

	final ZLTreeAdapter getAdapter() {
		return (ZLTreeAdapter) getListView().getAdapter();
	}

	final void resetTree() {
		ZLTreeAdapter adapter = getAdapter();
		if (adapter != null) {
			adapter.resetTree();
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final ZLTreeAdapter adapter = getAdapter();
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree) adapter.getItem(position);
		final NetworkView networkView = NetworkView.Instance();
		final NetworkTreeActions actions = networkView.getActions(tree);
		if (actions != null &&
				actions.runAction(this, tree, item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
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


	private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, index, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		return item;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}
		addMenuItem(menu, 1, "networkSearch", R.drawable.ic_menu_networksearch);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!super.onPrepareOptionsMenu(menu)) {
			return false;
		}
		final boolean searchInProgress = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		menu.findItem(1).setEnabled(!searchInProgress);
		return true;
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			default:
				return true;
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY)) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	// methods from NetworkView.EventListener
	public void onModelChanged() {
		resetTree();
	}
}
