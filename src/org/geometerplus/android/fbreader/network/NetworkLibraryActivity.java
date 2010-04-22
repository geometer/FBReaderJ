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
import android.content.ServiceConnection;
import android.content.ComponentName;
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


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener {

	// dialog identifiers
	static final int DIALOG_AUTHENTICATION = 0;


	static NetworkLibraryActivity Instance;

	private final ZLResource myResource = ZLResource.resource("networkView");

	private final ArrayList<NetworkTreeActions> myActions = new ArrayList<NetworkTreeActions>();

	private final HashMap<Uri, Runnable> myCatalogRunnables = new HashMap<Uri, Runnable>();
	private NetworkBookItem myBookInfoItem;
	private final HashSet<String> myIconsToSync = new HashSet<String>();
	private final HashMap<String, Runnable> myOnCoverSyncRunnables = new HashMap<String, Runnable>();

	private boolean myLibraryLoaded;

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


	public interface EventListener {
		void onModelChanged();
	}

	private LinkedList<EventListener> myEventListeners = new LinkedList<EventListener>();

	public final void addEventListener(EventListener listener) {
		myEventListeners.add(listener);
	}

	public final void removeEventListener(EventListener listener) {
		myEventListeners.remove(listener);
	}

	final void fireOnModelChanged() {
		for (EventListener listener: myEventListeners) {
			listener.onModelChanged();
		}
	}

	private Activity myTopLevelActivity;

	public final Activity getTopLevelActivity() {
		return myTopLevelActivity;
	}

	final void setTopLevelActivity(Activity activity) {
		if (activity == null) {
			myTopLevelActivity = this;
		} else {
			myTopLevelActivity = activity;
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		Instance = this;
		myTopLevelActivity = this;
		myEventListeners.clear();

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		myConnection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			myConnection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!myLibraryLoaded) {
			myLibraryLoaded = true;
			final Handler handler = new Handler() {
				public void handleMessage(Message message) {
					ListView view = getListView();
					new LibraryAdapter(view, NetworkLibrary.Instance().getTree());

					myActions.add(new NetworkBookActions());
					myActions.add(new NetworkCatalogActions());
					myActions.trimToSize();

					view.invalidateViews();
				}
			};
			((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("loadingNetworkLibrary", new Runnable() {
				public void run() {
					NetworkLibrary.Instance().synchronize();
					handler.sendEmptyMessage(0);
				}
			}, this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		unbindService(myConnection);
		myConnection = null;
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

		LibraryAdapter(ListView view, NetworkTree tree) {
			super(view, tree);
			myPool = Executors.newFixedThreadPool(3 /* TODO: how many threads ??? */, new ThreadFactory() {

				private final ThreadFactory myDefaultThreadFactory = Executors.defaultThreadFactory();

				public Thread newThread(Runnable r) {
					final Thread th = myDefaultThreadFactory.newThread(r);
					th.setPriority(Thread.MIN_PRIORITY);
					return th;
				}
			});
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

			view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			final int maxHeight = view.getMeasuredHeight();
			final int maxWidth = maxHeight * 2 / 3;
//System.err.println("FBREADER -- dims(" + maxWidth + ", " + maxHeight + ")");

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
					} else if (!myIconsToSync.contains(img.Url)) {
						myIconsToSync.add(img.Url);
						final Handler handler = new Handler() {
							public void handleMessage(Message message) {
								myIconsToSync.remove(img.Url);
								final ListView view = NetworkLibraryActivity.this.getListView();
								view.invalidateViews();
								view.requestLayout();
								final Runnable runnable = myOnCoverSyncRunnables.remove(img.Url);
								if (runnable != null) {
									runnable.run();
								}
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

	boolean isCoverLoading(String url) {
		return myIconsToSync.contains(url);
	}

	void setOnCoverSyncRunnable(String invalidateOnUrl, Runnable runnable) {
		myOnCoverSyncRunnables.put(invalidateOnUrl, runnable);
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

	void openInBrowser(String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}

	void loadCatalog(Uri uri, Runnable loadCatalogRunnable) {
		if (!myCatalogRunnables.containsKey(uri)) {
			myCatalogRunnables.put(uri, loadCatalogRunnable);
			startService(
				new Intent(Intent.ACTION_DEFAULT, uri, getApplicationContext(), CatalogDownloaderService.class)
			);
		}
	}

	Runnable getCatalogRunnable(Uri uri) {
		return myCatalogRunnables.remove(uri);
	}

	void showBookInfoActivity(NetworkBookItem book) {
		myBookInfoItem = book;
		startActivity(
			new Intent(getApplicationContext(), NetworkBookInfoActivity.class)
		);
	}

	NetworkBookItem getBookItem() {
		NetworkBookItem book = myBookInfoItem;
		myBookInfoItem = null;
		return book;
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

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_AUTHENTICATION:
			dialog = AuthenticationDialog.Instance().createDialog(this);
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_AUTHENTICATION:
			AuthenticationDialog.Instance().prepareDialog(dialog);
			break;
		}		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
}
