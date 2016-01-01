/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.fbreader.util.Boolean3;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSCustomNetworkLink;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.action.*;
import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;
import org.geometerplus.android.fbreader.tree.TreeActivity;

import org.geometerplus.android.util.UIMessageUtil;

public abstract class NetworkLibraryActivity extends TreeActivity<NetworkTree> implements ListView.OnScrollListener, NetworkLibrary.ChangeListener {
	public static final int REQUEST_MANAGE_CATALOGS = 1;
	public static final String ENABLED_CATALOG_IDS_KEY = "android.fbreader.data.enabled_catalogs";
	public static final String DISABLED_CATALOG_IDS_KEY = "android.fbreader.data.disabled_catalogs";

	public static final int REQUEST_ACCOUNT_PICKER = 2;
	public static final int REQUEST_AUTHORISATION = 3;
	public static final int REQUEST_WEB_AUTHORISATION_SCREEN = 4;

	final BookCollectionShadow BookCollection = new BookCollectionShadow();
	final BookDownloaderServiceConnection Connection = new BookDownloaderServiceConnection();

	final List<Action> myOptionsMenuActions = new ArrayList<Action>();
	final List<Action> myContextMenuActions = new ArrayList<Action>();
	final List<Action> myListClickActions = new ArrayList<Action>();
	private boolean mySingleCatalog;

	final ActivityNetworkContext myNetworkContext = new ActivityNetworkContext(this);

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		BookCollection.bindToService(this, new Runnable() {
			public void run() {
				Util.networkLibrary(NetworkLibraryActivity.this).clearExpiredCache(25);
			}
		});

		AuthenticationActivity.initCredentialsCreator(this);
		SQLiteCookieDatabase.init(this);

		setListAdapter(new NetworkLibraryAdapter(this));
		final Intent intent = getIntent();

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		BookCollection.bindToService(this, new Runnable() {
			public void run() {
				init(intent);
				final NetworkLibrary library = Util.networkLibrary(NetworkLibraryActivity.this);
				library.addChangeListener(NetworkLibraryActivity.this);

				if (getCurrentTree() instanceof RootTree) {
					mySingleCatalog = intent.getBooleanExtra("SingleCatalog", false);
					if (!library.isInitialized()) {
						Util.initLibrary(NetworkLibraryActivity.this, myNetworkContext, new Runnable() {
							public void run() {
								library.runBackgroundUpdate(false);
								requestCatalogPlugins();
								if (intent != null) {
									openTreeByIntent(intent);
								}
							}
						});
					} else {
						onLibraryChanged(NetworkLibrary.ChangeListener.Code.SomeCode, new Object[0]);
						openTreeByIntent(intent);
					}
				}
			}
		});

		getListView().setOnScrollListener(this);
	}

	@Override
	protected NetworkTree getTreeByKey(FBTree.Key key) {
		final NetworkLibrary library = Util.networkLibrary(this);
		final NetworkTree tree = library.getTreeByKey(key);
		return tree != null ? tree : library.getRootTree();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Connection.bindToService(this, null);
	}

	@Override
	public void onResume() {
		super.onResume();
		myNetworkContext.onResume();
		getListView().setOnCreateContextMenuListener(this);
		Util.networkLibrary(this).fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	@Override
	protected void onStop() {
		Connection.unbind(this);

		super.onStop();
	}

	@Override
	public void onDestroy() {
		Util.networkLibrary(this).removeChangeListener(this);
		BookCollection.unbind();
		super.onDestroy();
	}

	private boolean openTreeByIntent(Intent intent) {
		if (FBReaderIntents.Action.OPEN_NETWORK_CATALOG.equals(intent.getAction())) {
			final Uri uri = intent.getData();
			if (uri == null) {
				return false;
			}
			final String id = uri.toString();
			addCustomLink(id, new Runnable() {
				public void run() {
					final NetworkLibrary library = Util.networkLibrary(NetworkLibraryActivity.this);
					library.setLinkActive(id, true);
					library.synchronize();
					onLibraryChanged(NetworkLibrary.ChangeListener.Code.SomeCode, new Object[0]);

					final NetworkTree tree = library.getCatalogTreeByUrl(id);
					if (tree != null) {
						checkAndRun(
							new OpenCatalogAction(NetworkLibraryActivity.this, myNetworkContext),
							tree
						);
					}
				}
			});
			return true;
		}
		return false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (!openTreeByIntent(intent)) {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Connection.bindToService(this, new Runnable() {
			public void run() {
				getListView().invalidateViews();
			}
		});

		if (myNetworkContext.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		if (resultCode != RESULT_OK || data == null) {
			return;
		}

		switch (requestCode) {
			case REQUEST_MANAGE_CATALOGS:
			{
				final ArrayList<String> myIds =
					data.getStringArrayListExtra(ENABLED_CATALOG_IDS_KEY);
				final NetworkLibrary library = Util.networkLibrary(this);
				library.setActiveIds(myIds);
				library.synchronize();
				break;
			}
		}
	}

	@Override
	public boolean onSearchRequested() {
		final NetworkTree tree = getCurrentTree();
		final RunSearchAction action = new RunSearchAction(this, false);
		if (action.isVisible(tree) && action.isEnabled(tree)) {
			action.run(tree);
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
		return tree instanceof RootTree && (mySingleCatalog || ((RootTree)tree).IsFake);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final NetworkItemsLoader loader =
				Util.networkLibrary(this).getStoredLoader(getCurrentTree());
			if (loader != null) {
				loader.interrupt();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void fillOptionsMenuList() {
		myOptionsMenuActions.add(new RunSearchAction(this, false));
		myOptionsMenuActions.add(new AddCustomCatalogAction(this));
		myOptionsMenuActions.add(new RefreshRootCatalogAction(this));
		myOptionsMenuActions.add(new ManageCatalogsAction(this));
		myOptionsMenuActions.add(new ReloadCatalogAction(this, myNetworkContext));
		myOptionsMenuActions.add(new SignInAction(this));
		myOptionsMenuActions.add(new SignUpAction(this));
		myOptionsMenuActions.add(new SignOutAction(this, myNetworkContext));
		myOptionsMenuActions.add(new TopupAction(this));
		myOptionsMenuActions.add(new BuyBasketBooksAction(this));
		myOptionsMenuActions.add(new ClearBasketAction(this));
		myOptionsMenuActions.add(new OpenRootAction(this));
	}

	private void fillContextMenuList() {
		myContextMenuActions.add(new OpenCatalogAction(this, myNetworkContext));
		myContextMenuActions.add(new OpenInBrowserAction(this));
		myContextMenuActions.add(new RunSearchAction(this, true));
		myContextMenuActions.add(new AddCustomCatalogAction(this));
		myContextMenuActions.add(new SignOutAction(this, myNetworkContext));
		myContextMenuActions.add(new TopupAction(this));
		myContextMenuActions.add(new SignInAction(this));
		myContextMenuActions.add(new EditCustomCatalogAction(this));
		myContextMenuActions.add(new DisableCatalogAction(this));
		myContextMenuActions.add(new RemoveCustomCatalogAction(this));
		myContextMenuActions.add(new BuyBasketBooksAction(this));
		myContextMenuActions.add(new ClearBasketAction(this));
	}

	private void fillListClickList() {
		myListClickActions.add(new OpenCatalogAction(this, myNetworkContext));
		myListClickActions.add(new OpenInBrowserAction(this));
		myListClickActions.add(new RunSearchAction(this, true));
		myListClickActions.add(new AddCustomCatalogAction(this));
		myListClickActions.add(new TopupAction(this));
		myListClickActions.add(new ShowBookInfoAction(this, myNetworkContext));
		myListClickActions.add(new ManageCatalogsAction(this));
	}

	private List<? extends Action> getContextMenuActions(NetworkTree tree) {
		return tree instanceof NetworkBookTree
			? NetworkBookActions.getContextMenuActions(this, (NetworkBookTree)tree, BookCollection, Connection)
			: myContextMenuActions;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (myContextMenuActions.isEmpty()) {
			fillContextMenuList();
		}

		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final NetworkTree tree = (NetworkTree)getTreeAdapter().getItem(position);
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
		final NetworkTree tree = (NetworkTree)getTreeAdapter().getItem(position);
		if (tree != null) {
			for (Action a : getContextMenuActions(tree)) {
				if (a.Code == item.getItemId()) {
					checkAndRun(a, tree);
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

		final NetworkTree tree = (NetworkTree)getTreeAdapter().getItem(position);
		for (Action a : myListClickActions) {
			if (a.isVisible(tree) && a.isEnabled(tree)) {
				checkAndRun(a, tree);
				return;
			}
		}

		listView.showContextMenuForChild(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (myOptionsMenuActions.isEmpty()) {
			fillOptionsMenuList();
		}

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

		final NetworkTree tree = getCurrentTree();
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
		final NetworkTree tree = getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			if (a.Code == item.getItemId()) {
				checkAndRun(a, tree);
				break;
			}
		}
		return true;
	}

	private void updateLoadingProgress() {
		final NetworkLibrary library = Util.networkLibrary(this);
		final NetworkTree tree = getCurrentTree();
		final NetworkTree lTree = getLoadableNetworkTree(tree);
		final NetworkTree sTree = RunSearchAction.getSearchTree(tree);
		setProgressBarIndeterminateVisibility(
			library.isUpdateInProgress() ||
			library.isLoadingInProgress(lTree) ||
			library.isLoadingInProgress(sTree)
		);
	}

	// method from NetworkLibrary.ChangeListener
	public void onLibraryChanged(final NetworkLibrary.ChangeListener.Code code, final Object[] params) {
		runOnUiThread(new Runnable() {
			public void run() {
				switch (code) {
					default:
						updateLoadingProgress();
						getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
						break;
					case InitializationFailed:
						showInitLibraryDialog((String)params[0]);
						break;
					case InitializationFinished:
						break;
					case Found:
						openTree((NetworkTree)params[0]);
						break;
					case NotFound:
						UIMessageUtil.showErrorMessage(NetworkLibraryActivity.this, "emptyNetworkSearchResults");
						getListView().invalidateViews();
						break;
					case EmptyCatalog:
						UIMessageUtil.showErrorMessage(NetworkLibraryActivity.this, "emptyCatalog");
						break;
					case NetworkError:
						UIMessageUtil.showMessageText(NetworkLibraryActivity.this, (String)params[0]);
						break;
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

	@Override
	protected void onCurrentTreeChanged() {
		BookCollection.bindToService(this, new Runnable() {
			public void run() {
				onLibraryChanged(NetworkLibrary.ChangeListener.Code.SomeCode, new Object[0]);
			}
		});
	}

	private void showInitLibraryDialog(String error) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Util.initLibrary(NetworkLibraryActivity.this, myNetworkContext, null);
				} else {
					finish();
				}
			}
		};

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource boxResource = dialogResource.getResource("networkError");
		final ZLResource buttonResource = dialogResource.getResource("button");
		new AlertDialog.Builder(this)
			.setTitle(boxResource.getResource("title").getValue())
			.setMessage(error)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("tryAgain").getValue(), listener)
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
				}
			})
			.create().show();
	}

	private void checkAndRun(final Action action, final NetworkTree tree) {
		if (tree instanceof NetworkCatalogTree) {
			final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;
			switch (catalogTree.getVisibility()) {
				case FALSE:
					break;
				case TRUE:
					action.run(tree);
					break;
				case UNDEFINED:
					Util.runAuthenticationDialog(this, tree.getLink(), new Runnable() {
						public void run() {
							if (catalogTree.getVisibility() != Boolean3.TRUE) {
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

	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
		if (firstVisible + visibleCount + 1 >= totalCount) {
			final NetworkTree tree = getCurrentTree();
			if (tree instanceof NetworkCatalogTree) {
				((NetworkCatalogTree)tree).loadMoreChildren(totalCount);
			}
		}
	}

	public void onScrollStateChanged(AbsListView view, int state) {
	}

	private final BroadcastReceiver myCatalogInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final List<String> urls =
				getResultExtras(true).getStringArrayList("fbreader.catalog.ids");
			if (urls == null || urls.isEmpty()) {
				return;
			}
			for (String u : urls) {
				addCustomLink(u, null);
			}
		}
	};

	private void addCustomLink(String url, final Runnable postAction) {
		final NetworkLibrary library = Util.networkLibrary(this);
		if (library.getLinkByUrl(url) != null) {
			if (postAction != null) {
				runOnUiThread(postAction);
			}
			return;
		}

		final ICustomNetworkLink link = new OPDSCustomNetworkLink(
			library,
			INetworkLink.INVALID_ID,
			INetworkLink.Type.Custom,
			null, null, null,
			new UrlInfoCollection<UrlInfoWithDate>(new UrlInfoWithDate(
				UrlInfo.Type.Catalog, url, MimeType.APP_ATOM_XML
			))
		);
		final Runnable loader = new Runnable() {
			public void run() {
				try {
					link.reloadInfo(myNetworkContext, false, false);
					library.addCustomLink(link);
					if (postAction != null) {
						runOnUiThread(postAction);
					}
				} catch (ZLNetworkException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(loader).start();
	}

	public void requestCatalogPlugins() {
		sendOrderedBroadcast(
			new Intent(Util.EXTRA_CATALOG_ACTION),
			null,
			myCatalogInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);
	}
}
