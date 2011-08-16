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
import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.TopUpTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.api.PluginApi;

class TopupActions extends NetworkTreeActions {
	static class ActionInfo {
		final String IntentAction;
		final String ResourceId;

		ActionInfo(String intentAction, String resourceId) {
			IntentAction = intentAction;
			ResourceId = resourceId;
		}

		boolean isSupported(Activity activity, INetworkLink link) {
			return Util.isTopupSupported(activity, link, IntentAction);
		}

		Runnable getRunnable(Activity activity, INetworkLink link) {
			return topupRunnable(activity, link, IntentAction);
		}
	};

	private final ArrayList<ActionInfo> myActionInfos = new ArrayList<ActionInfo>();

	{
		//myActionInfos.add(new ActionInfo(Util.CREDIT_CARD_ACTION_ID, "topupViaCreditCard"));
		//myActionInfos.add(new ActionInfo(Util.SMS_ACTION_ID, "topupViaSms"));
		//myActionInfos.add(new ActionInfo(Util.SELF_SERVICE_ACTION_ID, "topupViaSelfServiceKiosk"));
		//myActionInfos.add(new ActionInfo(Util.BROWSER_ACTION_ID, "topupViaBrowser"));
	}

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

		int i = 0;
		for (; i < myActionInfos.size(); ++i) {
			final ActionInfo info = myActionInfos.get(i);
			if (info.isSupported(activity, link)) {
				addMenuItem(menu, i, info.ResourceId);
			}
		}
		final List<PluginApi.TopupActionInfo> infos =
			NetworkView.Instance().TopupActionInfos.get(link.getUrlInfo(UrlInfo.Type.Catalog).Url);
		if (infos != null) {
			for (PluginApi.TopupActionInfo info : infos) {
				addMenuItemWithText(menu, i++, info.MenuItemName);
			}
		}
	}

	@Override
	public int getDefaultActionCode(NetworkLibraryActivity activity, NetworkTree tree) {
		return getDefaultActionCode(activity, ((TopUpTree)tree).Item.Link);
	}

	private int getDefaultActionCode(Activity activity, INetworkLink link) {
		final List<PluginApi.TopupActionInfo> infos =
			NetworkView.Instance().TopupActionInfos.get(link.getUrlInfo(UrlInfo.Type.Catalog).Url);
		if (infos == null || infos.size() == 0) {
			return TREE_NO_ACTION;
		} else if (infos.size() == 1) {
			return 0; // TODO: replace by action code
		} else {
			return TREE_SHOW_CONTEXT_MENU;
		}
	}

	@Override
	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkLibraryActivity activity, Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkLibraryActivity activity, NetworkTree tree, int actionCode) {
		final INetworkLink link = ((TopUpTree)tree).Item.Link;
		return runAction(activity, link, actionCode);
	}

	boolean runAction(final Activity activity, final INetworkLink link, int actionCode) {
		try {
			if (actionCode < myActionInfos.size()) {
				doTopup(activity, link, myActionInfos.get(actionCode).getRunnable(activity, link));
			} else {
				actionCode -= myActionInfos.size();
				final List<PluginApi.TopupActionInfo> infos =
					NetworkView.Instance().TopupActionInfos.get(link.getUrlInfo(UrlInfo.Type.Catalog).Url);
				final String url = infos.get(actionCode).getId().toString();
				doTopup(activity, link, new Runnable() {
					public void run() {
						Util.runTopupDialogWithUrl(activity, link, url);
					}
				});
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static Runnable topupRunnable(final Activity activity, final INetworkLink link, final String actionId) {
		if (Util.BROWSER_ACTION_ID.equals(actionId)) {
			return new Runnable() {
				public void run() {
					Util.openInBrowser(
						activity,
						link.authenticationManager().topupLink()
					);
				}
			};
		} else {
			return new Runnable() {
				public void run() {
					Util.runTopupDialog(activity, link, actionId);
				}
			};
		}
	}

	private void doTopup(final Activity activity, final INetworkLink link, final Runnable action) {
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
			if (activity instanceof NetworkLibraryActivity) {	
				view = ((NetworkLibraryActivity)activity).getListView();
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
