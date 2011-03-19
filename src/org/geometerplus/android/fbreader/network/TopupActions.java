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

class RefillAccountActions extends NetworkTreeActions {
	public static final int REFILL_VIA_SMS_ITEM_ID = 0;
	public static final int REFILL_VIA_BROWSER_ITEM_ID = 1;


	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof TopUpTree;
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
		buildContextMenu(activity, menu, ((TopUpTree)tree).Item.Link);
	}

	void buildContextMenu(Activity activity, ContextMenu menu, INetworkLink link) {
		menu.setHeaderTitle(getTitleValue("refillTitle"));

		if (Util.isSmsAccountRefillingSupported(activity, link)) {
			addMenuItem(menu, REFILL_VIA_SMS_ITEM_ID, "refillViaSms");
		}
		if (Util.isBrowserAccountRefillingSupported(activity, link)) {
			addMenuItem(menu, REFILL_VIA_BROWSER_ITEM_ID, "refillViaBrowser");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		return getDefaultActionCode(activity, ((TopUpTree)tree).Item.Link);
	}
	private int getDefaultActionCode(Activity activity, INetworkLink link) {
		final boolean sms = Util.isSmsAccountRefillingSupported(activity, link);
		final boolean browser = Util.isBrowserAccountRefillingSupported(activity, link);

		if (sms && browser) {
			return TREE_SHOW_CONTEXT_MENU;
		} else if (sms) {
			return REFILL_VIA_SMS_ITEM_ID;
		} else /* if (browser) */ { 
			return REFILL_VIA_BROWSER_ITEM_ID;
		}
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		return null;
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
		Runnable refillRunnable = null;
		switch (actionCode) {
			case REFILL_VIA_SMS_ITEM_ID:
				refillRunnable = smsRefillRunnable(activity, link);
				break;
			case REFILL_VIA_BROWSER_ITEM_ID:
				refillRunnable = browserRefillRunnable(activity, link);
				break;
		}

		if (refillRunnable == null) {
			return false;
		}
		doRefill(activity, link, refillRunnable);
		return true;
	}

	private static Runnable browserRefillRunnable(final Activity activity, final INetworkLink link) {
		return new Runnable() {
			public void run() {
				Util.openInBrowser(
					activity,
					link.authenticationManager().refillAccountLink()
				);
			}
		};
	}

	private static Runnable smsRefillRunnable(final Activity activity, final INetworkLink link) {
		return new Runnable() {
			public void run() {
				Util.runSmsDialog(activity, link);
			}
		};
	}

	private static void doRefill(final Activity activity, final INetworkLink link, final Runnable refiller) {
		final NetworkAuthenticationManager mgr = link.authenticationManager();
		if (mgr.mayBeAuthorised(false)) {
			refiller.run();
		} else {
			AuthenticationDialog.show(activity, link, new Runnable() {
				public void run() {
					if (mgr.mayBeAuthorised(false)) {
						refiller.run();
					}
				}
			});
		}
	}

	public void runStandalone(Activity activity, INetworkLink link) {
		final int refillActionCode = getDefaultActionCode(activity, link);
		if (refillActionCode == TREE_SHOW_CONTEXT_MENU) {
			//activity.getListView().showContextMenu();
			View view = null;
			if (activity instanceof NetworkBaseActivity) {	
				view = ((NetworkBaseActivity)activity).getListView();
			} else if (activity instanceof NetworkBookInfoActivity) {
				view = ((NetworkBookInfoActivity)activity).getMainView();
			}
			if (view != null) {
				view.showContextMenu();
			}
		} else if (refillActionCode >= 0) {
			runAction(activity, link, refillActionCode);
		}
	}
}
