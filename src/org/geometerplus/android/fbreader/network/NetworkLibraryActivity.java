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

import java.util.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.opds.BasketItem;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.tree.BaseActivity;
import org.geometerplus.android.fbreader.api.PluginApi;

import org.geometerplus.android.fbreader.network.action.*;

public class NetworkLibraryActivity extends BaseActivity implements NetworkView.EventListener {
	protected static final int BASIC_AUTHENTICATION_CODE = 1;
	protected static final int SIGNUP_CODE = 2;

	private static final String ACTIVITY_BY_TREE_KEY = "ActivityByTree";

	static void setForTree(NetworkTree tree, NetworkLibraryActivity activity) {
		if (tree != null) {
			tree.setUserData(ACTIVITY_BY_TREE_KEY, activity);
		}
	}

	static NetworkLibraryActivity getByTree(NetworkTree tree) {
		return (NetworkLibraryActivity)tree.getUserData(ACTIVITY_BY_TREE_KEY);
	}

	public BookDownloaderServiceConnection Connection;

	private volatile Intent myIntent;
	private volatile boolean myInProgress;

	final List<Action> myOptionsMenuActions = new ArrayList<Action>();
	final List<Action> myContextMenuActions = new ArrayList<Action>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		SQLiteCookieDatabase.init(this);

		Connection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			Connection,
			BIND_AUTO_CREATE
		);

		setListAdapter(new NetworkLibraryAdapter(this));
		init(getIntent());

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		setForTree((NetworkTree)getCurrentTree(), this);

		setProgressBarIndeterminateVisibility(myInProgress);

		if (getCurrentTree() instanceof RootTree) {
			myIntent = getIntent();

			if (!NetworkView.Instance().isInitialized()) {
				if (NetworkInitializer.Instance == null) {
					new NetworkInitializer(this);
					NetworkInitializer.Instance.start();
				} else {
					NetworkInitializer.Instance.setActivity(this);
				}
			} else {
				onModelChanged();
				if (myIntent != null) {
					processIntent(myIntent);
					myIntent = null;
				}
			}
		}
	}

	@Override
	protected FBTree getTreeByKey(FBTree.Key key) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		FBTree tree = null;
		if (key != null) {
			tree = library.getTreeByKey(key);
		}
		return tree != null ? tree : library.getRootTree();
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
		ZLNetworkManager.Instance().setCredentialsCreator(myCredentialsCreator);
	}

	@Override
	protected void onStop() {
		NetworkView.Instance().removeEventListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (getCurrentTree() instanceof RootTree) {
			if (!NetworkView.Instance().isInitialized() && NetworkInitializer.Instance != null) {
				NetworkInitializer.Instance.setActivity(null);
			}
		}
		setForTree((NetworkTree)getCurrentTree(), null);
		if (Connection != null) {
			unbindService(Connection);
			Connection = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		processIntent(intent);
	}

	@Override
	public boolean onSearchRequested() {
		if (getCurrentTree() instanceof RootTree) {
			if (searchIsInProgress()) {
				return false;
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		return false;
	}

	@Override
	protected boolean isTreeInvisible(FBTree tree) {
		return tree instanceof RootTree && ((RootTree)tree).IsFake;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final ItemsLoader runnable =
				ItemsLoadingService.getRunnable((NetworkTree)getCurrentTree());
			if (runnable != null) {
				runnable.interruptLoading();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void fillContextMenuList() {
		myContextMenuActions.add(new OpenCatalogAction(this));
		myContextMenuActions.add(new OpenInBrowserAction(this));
		myContextMenuActions.add(new ShowBooksAction(this));
		myContextMenuActions.add(new AddCustomCatalogAction(this));
		myContextMenuActions.add(new SignOutAction(this));
		myContextMenuActions.add(new TopupAction(this));
		myContextMenuActions.add(new SignInAction(this));
		myContextMenuActions.add(new EditCustomCatalogAction(this));
		myContextMenuActions.add(new RemoveCustomCatalogAction(this));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (myContextMenuActions.isEmpty()) {
			fillContextMenuList();
		}

		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			int count = 0;
			for (Action a : myContextMenuActions) {
				if (a.isVisible(tree) && a.isEnabled(tree)) {
					++count;
				}
			}
			if (count > 1) {
				menu.setHeaderTitle(tree.getName());
				for (Action a : myContextMenuActions) {
					if (a.isVisible(tree) && a.isEnabled(tree)) {
						menu.add(0, a.Code, 0, a.getContextLabel(tree));
					}
				}
			}
		}
	}

	private void runAction(final Action action, final NetworkTree tree) {
		if (tree instanceof NetworkCatalogTree) {
			final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
			switch (item.getVisibility()) {
				case B3_TRUE:
					action.run(tree);
					break;
				case B3_UNDEFINED:
					Util.runAuthenticationDialog(this, item.Link, new Runnable() {
						public void run() {
							if (item.getVisibility() != ZLBoolean3.B3_TRUE) {
								return;
							}
							if (action.Code != ActionCode.SIGNIN) {
								action.run(tree);
							}
						}
					});
					break;
			}
		} else {
			action.run(tree);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			for (Action a : myContextMenuActions) {
				if (a.Code == item.getItemId()) {
					runAction(a, tree);
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		if (myContextMenuActions.isEmpty()) {
			fillContextMenuList();
		}

		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		Action defaultAction = null;
		for (Action a : myContextMenuActions) {
			if (a.isVisible(tree) && a.isEnabled(tree)) {
				runAction(a, tree);
				return;
			}
		}
		/*
			final NetworkView networkView = NetworkView.Instance();
			final NetworkTreeActions actions = networkView.getActions(tree);
			if (actions == null) {
				return;
			}
			final int actionCode = actions.getDefaultActionCode(this, tree);
			if (actionCode == ActionCode.TREE_SHOW_CONTEXT_MENU) {
				listView.showContextMenuForChild(view);
				return;
			}
			if (actionCode < 0) {
				return;
			}
			actions.runAction(this, tree, actionCode);
		*/
	}

	private final AuthenticationActivity.CredentialsCreator myCredentialsCreator =
		new AuthenticationActivity.CredentialsCreator(this, BASIC_AUTHENTICATION_CODE);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case BASIC_AUTHENTICATION_CODE:
				myCredentialsCreator.onDataReceived(resultCode, intent);
				break;
			case SIGNUP_CODE:
				Util.processSignup(((NetworkCatalogTree)getCurrentTree()).Item.Link, resultCode, intent);
				break;
		}
	}

	protected final String getOptionsValue(String key) {
		return NetworkLibrary.resource().getResource("menu").getResource(key).getValue();
	}

	protected final String getOptionsValue(String key, String arg) {
		return NetworkLibrary.resource().getResource("menu").getResource(key).getValue().replace("%s", arg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		myOptionsMenuActions.clear();
		myOptionsMenuActions.add(new RootAction(this, ActionCode.SEARCH, "networkSearch", R.drawable.ic_menu_search) {
			@Override
			public boolean isEnabled(NetworkTree tree) {
				return !searchIsInProgress();
			}

			@Override
			public void run(NetworkTree tree) {
				onSearchRequested();
			}
		});
		myOptionsMenuActions.add(new AddCustomCatalogAction(this));
		myOptionsMenuActions.add(new RootAction(this, ActionCode.REFRESH, "refreshCatalogsList", R.drawable.ic_menu_refresh) {
			@Override
			public void run(NetworkTree tree) {
				refreshCatalogsList();
			}
		});
		myOptionsMenuActions.add(new LanguageFilterAction(this));
		myOptionsMenuActions.add(new ReloadCatalogAction(this));
		myOptionsMenuActions.add(new SignInAction(this));
		myOptionsMenuActions.add(new SignUpAction(this));
		myOptionsMenuActions.add(new SignOutAction(this));
		myOptionsMenuActions.add(new TopupAction(this));

		final NetworkTree tree = (NetworkTree)getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			final MenuItem item = menu.add(0, a.Code, Menu.NONE, "");
			if (a.IconId != -1) {
				item.setIcon(a.IconId);
			}
		}
		//if (tree instanceof NetworkCatalogTree) {
			//if (((NetworkCatalogTree)tree).Item instanceof BasketItem) {
			//	addOptionsItem(menu, ActionCode.BASKET_CLEAR, "clearBasket");
			//	addOptionsItem(menu, ActionCode.BASKET_BUY_ALL_BOOKS, "buyAllBooks");
			//}
		//}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		final NetworkTree tree = (NetworkTree)getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			final MenuItem item = menu.findItem(a.Code);
			if (a.isVisible(tree)) {
				item.setVisible(true);
				item.setEnabled(a.isEnabled(tree));
				item.setTitle(a.getOptionsLabel(tree));
			} else {
				item.setVisible(false);
			}
		}
		return true;
	}

	protected static boolean searchIsInProgress() {
		return ItemsLoadingService.getRunnable(
			NetworkLibrary.Instance().getSearchItemTree()
		) != null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final NetworkTree tree = (NetworkTree)getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			if (a.Code == item.getItemId()) {
				a.run(tree);
				break;
			}
		}
		return true;
	}

	private void refreshCatalogsList() {
		final NetworkView view = NetworkView.Instance();

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == null) {
					view.finishBackgroundUpdate();
				} else {
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource boxResource = dialogResource.getResource("networkError");
					final ZLResource buttonResource = dialogResource.getResource("button");
					new AlertDialog.Builder(NetworkLibraryActivity.this)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage((String) msg.obj)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
						.create().show();
				}
			}
		};

		UIUtil.wait("updatingCatalogsList", new Runnable() {
			public void run() {
				String error = null;
				try {
					view.runBackgroundUpdate(true);
				} catch (ZLNetworkException e) {
					error = e.getMessage();
				}
				handler.sendMessage(handler.obtainMessage(0, error));
			}
		}, this);
	}

	// method from NetworkView.EventListener
	public void onModelChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				final NetworkTree tree = getLoadableNetworkTree((NetworkTree)getCurrentTree());
				myInProgress =
					tree != null &&
					ItemsLoadingService.getRunnable(tree) != null;
				getListView().invalidateViews();

				/*
				 * getListAdapter() always returns CatalogAdapter because onModelChanged()
				 * can be called only after Activity's onStart() method (where NetworkView's
				 * addEventListener() is called). Therefore CatalogAdapter will be set as
				 * adapter in onCreate() method before any calls to onModelChanged().
				 */
				((NetworkLibraryAdapter)getListAdapter()).replaceAll(getCurrentTree().subTrees());
				for (FBTree child : getCurrentTree().subTrees()) {
					if (child instanceof TopUpTree) {
						child.invalidateChildren();
					}
				}

				setProgressBarIndeterminateVisibility(myInProgress);
			}
		});
	}

	private static NetworkTree getLoadableNetworkTree(NetworkTree tree) {
		while (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			if (tree.Parent instanceof NetworkTree) {
				tree = (NetworkTree)tree.Parent;
			} else {
				return null;
			}
		}
		return tree;
	}

	void processSavedIntent() {
		if (myIntent != null) {
			processIntent(myIntent);
			myIntent = null;
		}
	}

	private void processIntent(Intent intent) {
		if (AddCustomCatalogActivity.ADD_CATALOG.equals(intent.getAction())) {
			final ICustomNetworkLink link = AddCustomCatalogActivity.getLinkFromIntent(intent);
			if (link != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						final NetworkLibrary library = NetworkLibrary.Instance();
						library.addCustomLink(link);
						library.synchronize();
						onModelChanged();
					}
				});
			}
		}
	}
}
