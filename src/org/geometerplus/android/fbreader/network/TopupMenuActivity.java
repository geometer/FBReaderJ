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

import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.android.fbreader.api.PluginApi;

public class TopupMenuActivity extends ListActivity implements AdapterView.OnItemClickListener {
	static final String TOPUP_ACTION =
		"android.fbreader.action.network.TOPUP";

	static boolean isTopupSupported(INetworkLink link) {
		final List<PluginApi.TopupActionInfo> infos =
			NetworkView.Instance().TopupActionInfos.get(link.getUrlInfo(UrlInfo.Type.Catalog).Url);
		return infos != null && infos.size() > 0;
	}

	static void runMenu(Context context, INetworkLink link) {
		context.startActivity(
			new Intent(context, TopupMenuActivity.class)
				.setData(Uri.parse(link.getUrlInfo(UrlInfo.Type.Catalog).Url))
		);
	}

	private INetworkLink myLink;
	private List<PluginApi.TopupActionInfo> myInfos;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(NetworkLibrary.resource().getResource("topupTitle").getValue());
		final String url = getIntent().getData().toString();
		myLink = NetworkLibrary.Instance().getLinkByUrl(url);
		myInfos = NetworkView.Instance().TopupActionInfos.get(url);
		if (myInfos == null || myInfos.size() == 0) {
			finish();
			return;
		} else if (myInfos.size() == 1) {
			runTopupDialog(myInfos.get(0));
			finish();
			return;
		}
		setListAdapter(new ActionListAdapter());
		getListView().setOnItemClickListener(this);
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runTopupDialog(myInfos.get(position));
		finish();
	}

	private void runTopupDialog(final PluginApi.TopupActionInfo info) {
		try {
			doTopup(new Runnable() {
				public void run() {
					try {
						final Intent intent = new Intent(TOPUP_ACTION, info.getId());
						final NetworkAuthenticationManager mgr = myLink.authenticationManager();
						if (mgr != null) {
							for (Map.Entry<String,String> entry : mgr.getTopupData().entrySet()) {
								intent.putExtra(entry.getKey(), entry.getValue());
							}
						}
						// TODO: put amount
						intent.putExtra("topup:amount", (String)null);
						if (PackageUtil.canBeStarted(TopupMenuActivity.this, intent, true)) {
							startActivity(intent);
						}
					} catch (ActivityNotFoundException e) {
					}
				}
			});
		} catch (Exception e) {
			// do nothing
		}
	}

	private void doTopup(final Runnable action) {
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		if (mgr.mayBeAuthorised(false)) {
			action.run();
		} else {
			Util.runAuthenticationDialog(this, myLink, null, new Runnable() {
				public void run() {
					if (mgr.mayBeAuthorised(false)) {
						runOnUiThread(action);
					}
				}
			});
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
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.topup_menu_item, parent, false);
			((TextView)view).setText(myInfos.get(position).MenuItemName);
			return view;
		}
	}
}
