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

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.android.fbreader.tree.BaseActivity;
import org.geometerplus.android.fbreader.network.action.*;

public class NetworkLibraryActivity extends BaseActivity implements NetworkLibrary.ChangeListener {
	protected static final int BASIC_AUTHENTICATION_CODE = 1;
	protected static final int SIGNUP_CODE = 2;
	protected static final int AUTO_SIGNIN_CODE = 3;

	BookDownloaderServiceConnection Connection;

	private volatile Intent myIntent;

	final List<Action> myOptionsMenuActions = new ArrayList<Action>();
	final List<Action> myContextMenuActions = new ArrayList<Action>();
	final List<Action> myListClickActions = new ArrayList<Action>();

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

		if (getCurrentTree() instanceof RootTree) {
			myIntent = getIntent();

			if (!NetworkLibrary.Instance().isInitialized()) {
				new NetworkInitializer(this).start();
			} else {
				NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
				new NetworkInitializer(this).end(null);
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

		NetworkLibrary.Instance().addChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnCreateContextMenuListener(this);
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		ZLNetworkManager.Instance().setCredentialsCreator(myCredentialsCreator);
	}

	@Override
	protected void onStop() {
		NetworkLibrary.Instance().removeChangeListener(this);
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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		processIntent(intent);
	}

	@Override
	public boolean onSearchRequested() {
		final NetworkTree tree = (NetworkTree)getCurrentTree();
		if (!new RunSearchAction(this).isEnabled(tree)) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
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

	private void fillOptionsMenuList() {
		myOptionsMenuActions.add(new RunSearchAction(this));
		myOptionsMenuActions.add(new AddCustomCatalogAction(this));
		myOptionsMenuActions.add(new RefreshRootCatalogAction(this));
		myOptionsMenuActions.add(new LanguageFilterAction(this));
		myOptionsMenuActions.add(new ReloadCatalogAction(this));
		myOptionsMenuActions.add(new SignInAction(this));
		myOptionsMenuActions.add(new SignUpAction(this));
		myOptionsMenuActions.add(new SignOutAction(this));
		myOptionsMenuActions.add(new TopupAction(this));
		myOptionsMenuActions.add(new BuyBasketBooksAction(this));
		myOptionsMenuActions.add(new ClearBasketAction(this));
	}

	private void fillContextMenuList() {
		myContextMenuActions.add(new OpenCatalogAction(this));
		myContextMenuActions.add(new OpenInBrowserAction(this));
		myContextMenuActions.add(new AddCustomCatalogAction(this));
		myContextMenuActions.add(new SignOutAction(this));
		myContextMenuActions.add(new TopupAction(this));
		myContextMenuActions.add(new SignInAction(this));
		myContextMenuActions.add(new EditCustomCatalogAction(this));
		myContextMenuActions.add(new RemoveCustomCatalogAction(this));
		myContextMenuActions.add(new BuyBasketBooksAction(this));
		myContextMenuActions.add(new ClearBasketAction(this));
	}

	private void fillListClickList() {
		myListClickActions.add(new OpenCatalogAction(this));
		myListClickActions.add(new OpenInBrowserAction(this));
		myListClickActions.add(new ShowBooksAction(this));
		myListClickActions.add(new AddCustomCatalogAction(this));
		myListClickActions.add(new TopupAction(this));
		myListClickActions.add(new ShowBookInfoAction(this));
	}

	private List<? extends Action> getContextMenuActions(NetworkTree tree) {
		return tree instanceof NetworkBookTree
			? NetworkBookActions.getContextMenuActions(this, ((NetworkBookTree)tree).Book, Connection)
			: myContextMenuActions;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (myContextMenuActions.isEmpty()) {
			fillContextMenuList();
		}

		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			menu.setHeaderTitle(tree.getName());
			for (Action a : getContextMenuActions(tree)) {
				if (a.isVisible(tree) && a.isEnabled(tree)) {
					menu.add(0, a.Code, 0, a.getContextLabel(tree));
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			for (Action a : getContextMenuActions(tree)) {
				if (a.Code == item.getItemId()) {
					a.checkAndRun(tree);
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		if (myListClickActions.isEmpty()) {
			fillListClickList();
		}

		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		for (Action a : myListClickActions) {
			if (a.isVisible(tree) && a.isEnabled(tree)) {
				a.checkAndRun(tree);
				return;
			}
		}

		listView.showContextMenuForChild(view);
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
			case AUTO_SIGNIN_CODE:
				Util.processAutoSignIn(this, ((NetworkCatalogTree)getCurrentTree()).Item.Link, resultCode, intent);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (myOptionsMenuActions.isEmpty()) {
			fillOptionsMenuList();
		}

//		final NetworkTree tree = (NetworkTree)getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			final MenuItem item = menu.add(0, a.Code, Menu.NONE, "");
			if (a.IconId != -1) {
				item.setIcon(a.IconId);
			}
		}
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final NetworkTree tree = (NetworkTree)getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			if (a.Code == item.getItemId()) {
				a.checkAndRun(tree);
				break;
			}
		}
		return true;
	}

	// method from NetworkLibrary.ChangeListener
	public void onLibraryChanged(NetworkLibrary.ChangeListener.Code code) {
		runOnUiThread(new Runnable() {
			public void run() {
				final NetworkTree tree = getLoadableNetworkTree((NetworkTree)getCurrentTree());
				final boolean inProgress =
					tree != null &&
					ItemsLoadingService.getRunnable(tree) != null;
				setProgressBarIndeterminateVisibility(inProgress);

				getListAdapter().replaceAll(getCurrentTree().subTrees());
				getListView().invalidateViews();

				for (FBTree child : getCurrentTree().subTrees()) {
					if (child instanceof TopUpTree) {
						child.invalidateSummary();
					}
				}
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
					}
				});
			}
		}
	}
}
