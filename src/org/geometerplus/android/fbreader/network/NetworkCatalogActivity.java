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

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.BaseAdapter;
import android.content.Intent;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.tree.FBTree;

public class NetworkCatalogActivity extends NetworkBaseActivity implements UserRegistrationConstants {
	private static final String ACTIVITY_BY_TREE_KEY = "ActivityByTree";

	static void setForTree(NetworkTree tree, NetworkCatalogActivity activity) {
		if (tree != null) {
			tree.setUserData(ACTIVITY_BY_TREE_KEY, activity);
		}
	}

	static NetworkCatalogActivity getByTree(NetworkTree tree) {
		return (NetworkCatalogActivity)tree.getUserData(ACTIVITY_BY_TREE_KEY);
	}

	private NetworkTree myTree;
	private volatile boolean myInProgress;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		myTree = Util.getTreeFromIntent(getIntent());

		if (myTree == null) {
			finish();
			return;
		}

		setForTree(myTree, this);

		setListAdapter(new CatalogAdapter());
		getListView().invalidateViews();
		setupTitle();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (menuInfo == null && myTree instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)myTree).Item.Link;
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
		if ((item == null || item.getMenuInfo() == null) && myTree instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)myTree).Item.Link;
			if (Util.isTopupSupported(this, link)) {
				final TopupActions actions = NetworkView.Instance().getTopupActions();
				if (actions != null && TopupActions.runAction(this, link, item.getItemId())) {
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	private final MyCredentialsCreator myCredentialsCreator = new MyCredentialsCreator();

	private class MyCredentialsCreator implements ZLNetworkManager.CredentialsCreator {
		private volatile String myUsername;
		private volatile String myPassword;
        
		public Credentials createCredentials(String scheme, AuthScope scope) {
			if (!"basic".equalsIgnoreCase(scope.getScheme())) {
				return null;
			}

			final Intent intent = new Intent();
			final String host = scope.getHost();
			final String area = scope.getRealm();
			final ZLStringOption option = new ZLStringOption("username", host + ":" + area, "");
			intent.setClass(NetworkCatalogActivity.this, AuthenticationActivity.class);
			intent.putExtra(AuthenticationActivity.HOST_KEY, host);
			intent.putExtra(AuthenticationActivity.AREA_KEY, area);
			intent.putExtra(AuthenticationActivity.SCHEME_KEY, scheme);
			intent.putExtra(AuthenticationActivity.USERNAME_KEY, option.getValue());
			startActivityForResult(intent, BASIC_AUTHENTICATION_CODE);
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
        
			Credentials creds = null;
			if (myUsername != null && myPassword != null) {
				option.setValue(myUsername);
				creds = new UsernamePasswordCredentials(myUsername, myPassword);
			}
			myUsername = null;
			myPassword = null;
			return creds;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case BASIC_AUTHENTICATION_CODE:
				synchronized (myCredentialsCreator) {
					if (resultCode == AuthenticationActivity.RESULT_OK && data != null) {
						myCredentialsCreator.myUsername =
							data.getStringExtra(AuthenticationActivity.USERNAME_KEY);
						myCredentialsCreator.myPassword =
							data.getStringExtra(AuthenticationActivity.PASSWORD_KEY);
					}
					myCredentialsCreator.notify();
				}
				break;
			case CUSTOM_AUTHENTICATION_CODE:
				Util.processCustomAuthentication(
					this, ((NetworkCatalogTree)myTree).Item.Link, resultCode, data
				);
				break;
			case SIGNUP_CODE:
				Util.processSignup(((NetworkCatalogTree)myTree).Item.Link, resultCode, data);
				break;
		}
	}

	private final void setupTitle() {
		String title = null;
		final NetworkView networkView = NetworkView.Instance();
		if (networkView.isInitialized()) {
			final NetworkTreeActions actions = networkView.getActions(myTree);
			if (actions != null) {
				title = actions.getTreeTitle(myTree);
			}
		}
		if (title == null) {
			title = myTree.getName();
		}
		setTitle(title);
		setProgressBarIndeterminateVisibility(myInProgress);
	}

	@Override
	public void onDestroy() {
		setForTree(myTree, null);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		ZLNetworkManager.Instance().setCredentialsCreator(myCredentialsCreator);
	}

	private final class CatalogAdapter extends BaseAdapter {
		public final int getCount() {
			return myTree.subTrees().size();
		}

		public final NetworkTree getItem(int position) {
			if (position < 0 || position >= myTree.subTrees().size()) {
				return null;
			}
			return (NetworkTree)myTree.subTrees().get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final NetworkTree tree = getItem(position);
			return setupNetworkTreeItemView(convertView, parent, tree);
		}

		void onModelChanged() {
			notifyDataSetChanged();
			for (FBTree child : myTree.subTrees()) {
				if (child instanceof TopUpTree) {
					child.invalidateChildren();
				}
			}
		}
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
	public void onModelChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				final NetworkTree tree = getLoadableNetworkTree(myTree);
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
				((CatalogAdapter)getListAdapter()).onModelChanged();
            
				setupTitle();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doStopLoading();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doStopLoading() {
		final ItemsLoadingRunnable runnable = ItemsLoadingService.getRunnable(myTree);
		if (runnable != null) {
			runnable.interruptLoading();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return NetworkView.Instance().createOptionsMenu(menu, myTree);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return NetworkView.Instance().prepareOptionsMenu(this, menu, myTree);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (NetworkView.Instance().runOptionsMenu(this, item, myTree)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
