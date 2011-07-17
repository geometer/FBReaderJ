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
import android.os.*;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;

import org.geometerplus.android.fbreader.tree.BaseActivity;

public class NetworkBaseActivity extends BaseActivity implements NetworkView.EventListener {
	protected static final int BASIC_AUTHENTICATION_CODE = 1;
	protected static final int CUSTOM_AUTHENTICATION_CODE = 2;
	protected static final int SIGNUP_CODE = 3;

	private static final String ACTIVITY_BY_TREE_KEY = "ActivityByTree";

	static void setForTree(NetworkTree tree, NetworkBaseActivity activity) {
		if (tree != null) {
			tree.setUserData(ACTIVITY_BY_TREE_KEY, activity);
		}
	}

	static NetworkBaseActivity getByTree(NetworkTree tree) {
		return (NetworkBaseActivity)tree.getUserData(ACTIVITY_BY_TREE_KEY);
	}

	public BookDownloaderServiceConnection Connection;

	private volatile boolean myInProgress;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		OLD_STYLE_FLAG = true;

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
	}

	@Override
	protected FBTree getTreeByKey(FBTree.Key key) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		return key != null ? library.getTreeByKey(key) : library.getRootTree();
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
		setForTree((NetworkTree)getCurrentTree(), null);
		if (Connection != null) {
			unbindService(Connection);
			Connection = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		return false;
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
		} else if (getCurrentTree() instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)getCurrentTree()).Item.Link;
			if (Util.isTopupSupported(this, link)) {
				final TopupActions actions = NetworkView.Instance().getTopupActions();
				if (actions != null) {
					actions.buildContextMenu(this, menu, link);
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
		} else if (getCurrentTree() instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)getCurrentTree()).Item.Link;
			if (Util.isTopupSupported(this, link)) {
				final TopupActions actions = NetworkView.Instance().getTopupActions();
				if (actions != null && TopupActions.runAction(this, link, item.getItemId())) {
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

	private final AuthenticationActivity.CredentialsCreator myCredentialsCreator =
		new AuthenticationActivity.CredentialsCreator(this, BASIC_AUTHENTICATION_CODE);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case BASIC_AUTHENTICATION_CODE:
				myCredentialsCreator.onDataReceived(resultCode, data);
				break;
			case CUSTOM_AUTHENTICATION_CODE:
				Util.processCustomAuthentication(
					this, ((NetworkCatalogTree)getCurrentTree()).Item.Link, resultCode, data
				);
				break;
			case SIGNUP_CODE:
				Util.processSignup(((NetworkCatalogTree)getCurrentTree()).Item.Link, resultCode, data);
				break;
		}
	}

	private static final int MENU_SEARCH = 1;
	private static final int MENU_REFRESH = 2;
	private static final int MENU_ADD_CATALOG = 3;
	private static final int MENU_LANGUAGE_FILTER = 4;

	private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = NetworkLibrary.resource().getResource("menu").getResource(resourceKey).getValue();
		return menu.add(0, index, Menu.NONE, label).setIcon(iconId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (getCurrentTree() instanceof RootTree) {
			addMenuItem(menu, MENU_SEARCH, "networkSearch", R.drawable.ic_menu_search);
			addMenuItem(menu, MENU_ADD_CATALOG, "addCustomCatalog", R.drawable.ic_menu_add);
			addMenuItem(menu, MENU_REFRESH, "refreshCatalogsList", R.drawable.ic_menu_refresh);
			addMenuItem(menu, MENU_LANGUAGE_FILTER, "languages", R.drawable.ic_menu_languages);
			return true;
		} else {
			return NetworkView.Instance().createOptionsMenu(menu, (NetworkTree)getCurrentTree());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (getCurrentTree() instanceof RootTree) {
			menu.findItem(MENU_SEARCH).setEnabled(!searchIsInProgress());
			return true;
		} else {
			return NetworkView.Instance().prepareOptionsMenu(this, menu, (NetworkTree)getCurrentTree());
		}
	}

	protected static boolean searchIsInProgress() {
		return ItemsLoadingService.getRunnable(
			NetworkLibrary.Instance().getSearchItemTree()
		) != null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (getCurrentTree() instanceof RootTree) {
			switch (item.getItemId()) {
				case MENU_SEARCH:
					return onSearchRequested();
				case MENU_ADD_CATALOG:
					AddCustomCatalogItemActions.addCustomCatalog(this);
					return true;
				case MENU_REFRESH:
					refreshCatalogsList();
					return true;
				case MENU_LANGUAGE_FILTER:
					runLanguageFilterDialog();
					return true;
				default:
					return true;
			}
		} else {
			return NetworkView.Instance().runOptionsMenu(this, item, (NetworkTree)getCurrentTree());
		}
	}

	private void runLanguageFilterDialog() {
		final NetworkLibrary library = NetworkLibrary.Instance();

		final List<String> allLanguageCodes = library.languageCodes();
		Collections.sort(allLanguageCodes, new ZLLanguageUtil.CodeComparator());
		final Collection<String> activeLanguageCodes = library.activeLanguageCodes();
		final CharSequence[] languageNames = new CharSequence[allLanguageCodes.size()];
		final boolean[] checked = new boolean[allLanguageCodes.size()];

		for (int i = 0; i < allLanguageCodes.size(); ++i) {
			final String code = allLanguageCodes.get(i);
			languageNames[i] = ZLLanguageUtil.languageName(code);
			checked[i] = activeLanguageCodes.contains(code);
		}

		final DialogInterface.OnMultiChoiceClickListener listener =
			new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}
			};
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final AlertDialog dialog = new AlertDialog.Builder(this)
			.setMultiChoiceItems(languageNames, checked, listener)
			.setTitle(dialogResource.getResource("languageFilterDialog").getResource("title").getValue())
			.setPositiveButton(dialogResource.getResource("button").getResource("ok").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					final TreeSet<String> newActiveCodes = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
					for (int i = 0; i < checked.length; ++i) {
						if (checked[i]) {
							newActiveCodes.add(allLanguageCodes.get(i));
						}
					}
					library.setActiveLanguageCodes(newActiveCodes);
					library.synchronize();
					NetworkView.Instance().fireModelChanged();
				}
			})
			.create();
		dialog.show();
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
					new AlertDialog.Builder(NetworkBaseActivity.this)
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
}
