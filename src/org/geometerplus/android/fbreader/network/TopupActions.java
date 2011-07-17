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
import android.view.View;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.TopUpTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

class TopupActions extends NetworkTreeActions {
	public static final int TOPUP_VIA_SMS_ITEM_ID = 0;
	public static final int TOPUP_VIA_BROWSER_ITEM_ID = 1;
	public static final int TOPUP_VIA_CREDIT_CARD_ITEM_ID = 2;
	public static final int TOPUP_VIA_SELF_SERVICE_ITEM_ID = 3;

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof TopUpTree;
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
		buildContextMenu(activity, menu, ((TopUpTree)tree).Item.Link);
	}

	void buildContextMenu(Activity activity, ContextMenu menu, INetworkLink link) {
		menu.setHeaderTitle(getTitleValue("topupTitle"));

		if (Util.isTopupSupported(activity, link, Util.CREDIT_CARD_TOPUP_ACTION)) {
			addMenuItem(menu, TOPUP_VIA_CREDIT_CARD_ITEM_ID, "topupViaCreditCard");
		}
		if (Util.isTopupSupported(activity, link, Util.SMS_TOPUP_ACTION)) {
			addMenuItem(menu, TOPUP_VIA_SMS_ITEM_ID, "topupViaSms");
		}
		if (Util.isTopupSupported(activity, link, Util.SELF_SERVICE_KIOSK_TOPUP_ACTION)) {
			addMenuItem(menu, TOPUP_VIA_SELF_SERVICE_ITEM_ID, "topupViaSelfServiceKiosk");
		}
		if (Util.isBrowserTopupSupported(activity, link)) {
			addMenuItem(menu, TOPUP_VIA_BROWSER_ITEM_ID, "topupViaBrowser");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		return getDefaultActionCode(activity, ((TopUpTree)tree).Item.Link);
	}
	private int getDefaultActionCode(Activity activity, INetworkLink link) {
		final boolean browser = Util.isBrowserTopupSupported(activity, link);
		final boolean sms = Util.isTopupSupported(activity, link, Util.SMS_TOPUP_ACTION);
		final boolean creditCard = Util.isTopupSupported(activity, link, Util.CREDIT_CARD_TOPUP_ACTION);
		final boolean selfService = Util.isTopupSupported(activity, link, Util.SELF_SERVICE_KIOSK_TOPUP_ACTION);
		final int count =
			(sms ? 1 : 0) +
			(browser ? 1 : 0) +
			(creditCard ? 1 : 0) +
			(selfService ? 1 : 0);

		if (count > 1) {
			return TREE_SHOW_CONTEXT_MENU;
		} else if (sms) {
			return TOPUP_VIA_SMS_ITEM_ID;
		} else if (creditCard) {
			return TOPUP_VIA_CREDIT_CARD_ITEM_ID;
		} else if (selfService) {
			return TOPUP_VIA_SELF_SERVICE_ITEM_ID;
		} else /* if (browser) */ { 
			return TOPUP_VIA_BROWSER_ITEM_ID;
		}
	}

	@Override
	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		final INetworkLink link = ((TopUpTree)tree).Item.Link;
		return runAction(activity, link, actionCode);
	}

	static boolean runAction(Activity activity, INetworkLink link, int actionCode) {
		Runnable topupRunnable = null;
		switch (actionCode) {
			case TOPUP_VIA_SMS_ITEM_ID:
				topupRunnable = topupRunnable(activity, link, Util.SMS_TOPUP_ACTION);
				break;
			case TOPUP_VIA_BROWSER_ITEM_ID:
				topupRunnable = browserTopupRunnable(activity, link);
				break;
			case TOPUP_VIA_CREDIT_CARD_ITEM_ID:
				topupRunnable = topupRunnable(activity, link, Util.CREDIT_CARD_TOPUP_ACTION);
				break;
			case TOPUP_VIA_SELF_SERVICE_ITEM_ID:
				topupRunnable = topupRunnable(activity, link, Util.SELF_SERVICE_KIOSK_TOPUP_ACTION);
				break;
		}

		if (topupRunnable == null) {
			return false;
		}
		doTopup(activity, link, topupRunnable);
		return true;
	}

	private static Runnable browserTopupRunnable(final Activity activity, final INetworkLink link) {
		return new Runnable() {
			public void run() {
				Util.openInBrowser(
					activity,
					link.authenticationManager().topupLink()
				);
			}
		};
	}

	private static Runnable topupRunnable(final Activity activity, final INetworkLink link, final String action) {
		return new Runnable() {
			public void run() {
				Util.runTopupDialog(activity, link, action);
			}
		};
	}

	private static void doTopup(final Activity activity, final INetworkLink link, final Runnable action) {
		final NetworkAuthenticationManager mgr = link.authenticationManager();
		if (mgr.mayBeAuthorised(false)) {
			action.run();
		} else {
			Util.runAuthenticationDialog(activity, link, null, new Runnable() {
				public void run() {
					if (mgr.mayBeAuthorised(false)) {
						activity.runOnUiThread(action);
					}
				}
			});
		}
	}

	public void runStandalone(Activity activity, INetworkLink link) {
		final int topupActionCode = getDefaultActionCode(activity, link);
		if (topupActionCode == TREE_SHOW_CONTEXT_MENU) {
			View view = null;
			if (activity instanceof NetworkBaseActivity) {	
				view = ((NetworkBaseActivity)activity).getListView();
			} else if (activity instanceof NetworkBookInfoActivity) {
				view = ((NetworkBookInfoActivity)activity).getMainView();
			}
			if (view != null) {
				activity.registerForContextMenu(view);
				view.showContextMenu();
			}
		} else if (topupActionCode >= 0) {
			runAction(activity, link, topupActionCode);
		}
	}
}
