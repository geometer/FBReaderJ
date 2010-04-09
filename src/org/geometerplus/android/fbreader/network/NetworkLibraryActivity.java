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
import java.util.concurrent.*;

import android.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.ZLTreeAdapter;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener {
	static NetworkLibraryActivity Instance;

	private final ZLResource myResource = ZLResource.resource("networkView");

	private final ArrayList<NetworkTreeActions> myActions = new ArrayList<NetworkTreeActions>();

	private HashMap<Uri, Runnable> myCatalogRunnables = new HashMap<Uri, Runnable>();


	private class BookDownloaderServiceConnection implements ServiceConnection {

		private BookDownloaderInterface myInterface;

		public synchronized void onServiceConnected(ComponentName className, IBinder service) {
			myInterface = BookDownloaderInterface.Stub.asInterface(service);
		}

		public synchronized void onServiceDisconnected(ComponentName name) {
			myInterface = null;
		}

		public synchronized boolean isBeingDownloaded(String url) {
			if (myInterface != null) {
				try {
					return myInterface.isBeingDownloaded(url);
				} catch (android.os.RemoteException e) {
				}
			}
			return false;
		}
	}

	private BookDownloaderServiceConnection myConnection;

	public boolean isBeingDownloaded(String url) {
		return (myConnection == null) ? false : myConnection.isBeingDownloaded(url);
	}


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Instance = this;

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		LibraryAdapter adapter = new LibraryAdapter(getListView(), NetworkLibrary.Instance().getTree());

		myActions.add(new NetworkBookActions());
		myActions.add(new NetworkCatalogActions());
		myActions.trimToSize();
	}

	@Override
	public void onStart() {
		super.onStart();

		myConnection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(this, BookDownloaderService.class),
			myConnection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	public void onStop() {
		unbindService(myConnection);
		myConnection = null;
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Instance = null;
		super.onDestroy();
	}

	private NetworkTreeActions chooseActions(ZLTree tree) {
		NetworkTree networkTree = (NetworkTree) tree;
		for (NetworkTreeActions actions: myActions) {
			if (actions.canHandleTree(networkTree)) {
				return actions;
			}
		}
		return null;
	}


	private final class LibraryAdapter extends ZLTreeAdapter {

		private final ExecutorService myPool;
		private final HashSet<NetworkImage> myIconsToSync;

		LibraryAdapter(ListView view, NetworkTree tree) {
			super(view, tree);
			myPool = Executors.newFixedThreadPool(10); // TODO: how many threads ???
			myIconsToSync = new HashSet<NetworkImage>();
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree) getItem(position);

			/*if (tree instanceof NetworkCatalogTree || tree instanceof NetworkBookTree) {
				menu.add(0, DBG_PRINT_ENTRY_ITEM_ID, 0, "dbg - Dump Entry");
			}*/

			final NetworkTreeActions actions = chooseActions(tree);
			if (actions != null) {
				actions.buildContextMenu(menu, tree);
			}
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

			final NetworkTree tree = (NetworkTree)getItem(position);

			((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());

			view.measure(-1, -2);
			final int maxHeight = view.getMeasuredHeight();
			final int maxWidth = maxHeight * 2 / 3;
//System.err.println("FBREADER -- dims(" + maxWidth + ", " + maxHeight + ")");

			final ImageView iconView = (ImageView)view.findViewById(R.id.network_tree_item_icon);
			Bitmap coverBitmap = null;
			final ZLImage cover = tree.getCover();
			if (cover != null) {
				ZLAndroidImageData data = null;
				final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
				if (cover instanceof NetworkImage) {
					final NetworkImage img = (NetworkImage) cover;
					if (img.isSynchronized()) {
						data = mgr.getImageData(img);
					} else if (!myIconsToSync.contains(img)) {
						myIconsToSync.add(img);
						final Handler handler = new Handler() {
							public void handleMessage(Message message) {
								myIconsToSync.remove(img);
								ListView view = NetworkLibraryActivity.this.getListView();
								view.invalidateViews();
								view.requestLayout();
							}
						};
						myPool.execute(new Runnable() {
							public void run() {
								img.synchronize();
								handler.sendEmptyMessage(0);
							}
						});
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
			final NetworkTreeActions actions = chooseActions(networkTree);
			if (actions == null) {
				return false;
			}
			final int actionCode = actions.getDefaultActionCode(networkTree);
			final String confirm = actions.getConfirmText(networkTree, actionCode);
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
							actions.runAction(networkTree, actionCode);
						}
					})
					.setNegativeButton(buttonResource.getResource("no").getValue(), null)
					.create().show();
			} else {
				actions.runAction(networkTree, actionCode);
			}
			return true;
		}
	}

	//private static final int DBG_PRINT_ENTRY_ITEM_ID = 32000;

	public final ZLTreeAdapter getAdapter() {
		return (ZLTreeAdapter) getListView().getAdapter();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final LibraryAdapter adapter = (LibraryAdapter) getAdapter();
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree) adapter.getItem(position);

		/*if (actionCode == DBG_PRINT_ENTRY_ITEM_ID) {
			String msg = null;
			if (tree instanceof NetworkCatalogTree) {
				msg = ((NetworkCatalogTree) tree).Item.dbgEntry.toString();
			} else if (tree instanceof NetworkBookTree) {
				msg = ((NetworkBookTree) tree).Book.dbgEntry.toString();
			}
			new AlertDialog.Builder(this).setTitle("dbg entry").setMessage(msg).setIcon(0).setPositiveButton("ok", null).create().show();
			return true;
		}*/

		final NetworkTreeActions actions = chooseActions(tree);
		if (actions != null &&
				actions.runAction(tree, item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void openInBrowser(String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}

	public void loadCatalog(Uri uri, Runnable loadCatalogRunnable) {
		if (!myCatalogRunnables.containsKey(uri)) {
			myCatalogRunnables.put(uri, loadCatalogRunnable);
			startService(
				new Intent(Intent.ACTION_DEFAULT, uri, this, CatalogDownloaderService.class)
			);
		}
	}

	public Runnable getCatalogRunnable(Uri uri) {
		Runnable r = myCatalogRunnables.get(uri);
		myCatalogRunnables.remove(uri);
		return r;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
}
