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

import android.content.*;
import android.net.Uri;

import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.android.fbreader.api.PluginApi;

public class TopupMenuActivity extends MenuActivity {
	private static final String AMOUNT_KEY = "topup:amount";
	private static final String CURRENCY_KEY = "topup:currency";

	public static boolean isTopupSupported(INetworkLink link) {
		// TODO: more correct check
		return link.getUrlInfo(UrlInfo.Type.TopUp) != null;
	}

	public static void runMenu(Context context, INetworkLink link, Money amount) {
		final Intent intent =
			Util.intentByLink(new Intent(context, TopupMenuActivity.class), link);
		intent.putExtra(AMOUNT_KEY, amount);
		context.startActivity(intent);
	}

	private INetworkLink myLink;
	private Money myAmount;

	@Override
	protected void init() {
		setTitle(NetworkLibrary.resource().getResource("topupTitle").getValue());
		final String url = getIntent().getData().toString();
		myLink = Util.networkLibrary(this).getLinkByUrl(url);
		myAmount = (Money)getIntent().getSerializableExtra(AMOUNT_KEY);

		if (myLink.getUrlInfo(UrlInfo.Type.TopUp) != null) {
			myInfos.add(new PluginApi.MenuActionInfo(
				Uri.parse(url + "/browser"),
				NetworkLibrary.resource().getResource("topupViaBrowser").getValue(),
				100
			));
		}
	}

	@Override
	protected String getAction() {
		return Util.TOPUP_ACTION;
	}

	@Override
	protected void runItem(final PluginApi.MenuActionInfo info) {
		try {
			doTopup(new Runnable() {
				public void run() {
					try {
						final NetworkAuthenticationManager mgr = myLink.authenticationManager();
						if (info.getId().toString().endsWith("/browser")) {
							// TODO: put amount
							if (mgr != null) {
								Util.openInBrowser(TopupMenuActivity.this, mgr.topupLink(myAmount));
							}
						} else {
							final Intent intent = new Intent(getAction(), info.getId());
							if (mgr != null) {
								for (Map.Entry<String,String> entry : mgr.getTopupData().entrySet()) {
									intent.putExtra(entry.getKey(), entry.getValue());
								}
							}
							if (myAmount != null) {
								intent.putExtra(AMOUNT_KEY, myAmount.Amount);
							}
							if (PackageUtil.canBeStarted(TopupMenuActivity.this, intent, true)) {
								startActivity(intent);
							}
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
			Util.runAuthenticationDialog(this, myLink, action);
		}
	}
}
