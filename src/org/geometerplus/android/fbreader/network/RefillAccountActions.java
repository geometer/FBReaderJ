/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class RefillAccountActions extends NetworkTreeActions {

	public static final int REFILL_ITEM_ID = 0;


	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof RefillAccountTree;
	}

	@Override
	public void buildContextMenu(NetworkBaseActivity activity, ContextMenu menu, NetworkTree tree) {
		final RefillAccountTree refillTree = (RefillAccountTree) tree;
		menu.setHeaderTitle(getTitleValue("refillTitle"));

		addMenuItem(menu, REFILL_ITEM_ID, "refillTitle");
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
		return REFILL_ITEM_ID;
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
	public boolean prepareOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case REFILL_ITEM_ID:
				doRefill(activity, (RefillAccountTree) tree);
				return true;
		}
		return false;
	}

	private void doRefill(final NetworkBaseActivity activity, final RefillAccountTree tree) {
		final NetworkAuthenticationManager mgr = tree.Link.authenticationManager();
		if (mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE) {
			NetworkView.Instance().openInBrowser(
				activity,
				tree.Link.authenticationManager().refillAccountLink()
			);
		} else {
			NetworkDialog.show(activity, NetworkDialog.DIALOG_AUTHENTICATION, tree.Link, new Runnable() {
				public void run() {
					if (mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE) {
						NetworkView.Instance().openInBrowser(
							activity,
							tree.Link.authenticationManager().refillAccountLink()
						);
					}
				}
			});
		}
	}
}
