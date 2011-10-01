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
import java.math.BigDecimal;

import android.app.ListActivity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.android.fbreader.api.PluginApi;

public class AccountMenuActivity extends ListActivity implements AdapterView.OnItemClickListener {
	private static final String ACCOUNT_ACTION = "android.fbreader.action.network.ACCOUNT";

	public static void runMenu(Context context, INetworkLink link) {
		context.startActivity(
			Util.intentByLink(new Intent(context, AccountMenuActivity.class), link)
		);
	}

	private INetworkLink myLink;
	private List<PluginApi.MenuActionInfo> myInfos;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(NetworkLibrary.resource().getResource("accountTitle").getValue());
		final String url = getIntent().getData().toString();
		myLink = NetworkLibrary.Instance().getLinkByUrl(url);

		myInfos = new ArrayList<PluginApi.MenuActionInfo>();
		if (myLink.getUrlInfo(UrlInfo.Type.SignIn) != null) {
			myInfos.add(new PluginApi.MenuActionInfo(
				Uri.parse(url + "/signIn"),
				NetworkLibrary.resource().getResource("signIn").getValue(),
				0
			));
		}

		try {
			startActivityForResult(new Intent(ACCOUNT_ACTION, getIntent().getData()), 0);
		} catch (ActivityNotFoundException e) {
			if (myInfos.size() == 1) {
				runAccountDialog(myInfos.get(0));
			}
			finish();
			return;
		}

		setListAdapter(new ActionListAdapter());
		getListView().setOnItemClickListener(this);
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runAccountDialog(myInfos.get(position));
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent != null) {
			final List<PluginApi.MenuActionInfo> actions =
				intent.<PluginApi.MenuActionInfo>getParcelableArrayListExtra(
					PluginApi.PluginInfo.KEY
				);
			if (actions != null) {
				myInfos.addAll(actions);
			}
			if (myInfos.size() == 0) {
				finish();
				return;
			} else if (myInfos.size() == 1) {
				runAccountDialog(myInfos.get(0));
				finish();
				return;
			}
			Collections.sort(myInfos);
			((ActionListAdapter)getListAdapter()).notifyDataSetChanged();
			getListView().invalidateViews();
		}
	}

	private void runAccountDialog(final PluginApi.MenuActionInfo info) {
		try {
			doAction(new Runnable() {
				public void run() {
					try {
						final NetworkAuthenticationManager mgr = myLink.authenticationManager();
						if (info.getId().toString().endsWith("/signIn")) {
							// TODO: put amount
							if (mgr != null) {
								//Util.openInBrowser(TopupMenuActivity.this, mgr.topupLink());
							}
						} else {
							/*
							final Intent intent = new Intent(TOPUP_ACTION, info.getId());
							if (mgr != null) {
								for (Map.Entry<String,String> entry : mgr.getTopupData().entrySet()) {
									intent.putExtra(entry.getKey(), entry.getValue());
								}
							}
							intent.putExtra(AMOUNT_KEY, myAmount);
							if (PackageUtil.canBeStarted(TopupMenuActivity.this, intent, true)) {
								startActivity(intent);
							}
							*/
						}
					} catch (ActivityNotFoundException e) {
					}
				}
			});
		} catch (Exception e) {
			// do nothing
		}
	}

	private void doAction(final Runnable action) {
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		if (mgr.mayBeAuthorised(false)) {
			action.run();
		} else {
			Util.runAuthenticationDialog(this, myLink, action);
		}
	}

	private class ActionListAdapter extends BaseAdapter {
		public final int getCount() {
			return myInfos.size();
		}

		public final Integer getItem(int position) {
			return position;
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
			((TextView)view).setText(myInfos.get(position).MenuItemName);
			return view;
		}
	}
}
