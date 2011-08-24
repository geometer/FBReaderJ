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

package org.geometerplus.android.fbreader.network.action;

import java.util.*;

import android.app.Activity;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;

import org.geometerplus.android.fbreader.network.ItemsLoader;

class CatalogExpander extends ItemsLoader {
	private final NetworkCatalogTree myTree;
	private final boolean myCheckAuthentication;
	private final boolean myResumeNotLoad;

	public CatalogExpander(Activity activity,
			NetworkCatalogTree tree, boolean checkAuthentication, boolean resumeNotLoad) {
		super(activity);
		myTree = tree;
		myCheckAuthentication = checkAuthentication;
		myResumeNotLoad = resumeNotLoad;
	}

	@Override
	public void doBefore() throws ZLNetworkException {
		final INetworkLink link = myTree.Item.Link;
		if (myCheckAuthentication && link.authenticationManager() != null) {
			final NetworkAuthenticationManager mgr = link.authenticationManager();
			try {
				if (mgr.isAuthorised(true) && mgr.needsInitialization()) {
					mgr.initialize();
				}
			} catch (ZLNetworkException e) {
				mgr.logOut();
			}
		}
	}

	@Override
	public void doLoading(NetworkOperationData.OnNewItemListener doWithListener) throws ZLNetworkException {
		if (myResumeNotLoad) {
			myTree.Item.resumeLoading(doWithListener);
		} else {
			myTree.Item.loadChildren(doWithListener);
		}
	}

	@Override
	protected void updateItems(List<NetworkItem> items) {
		for (NetworkItem item: items) {
			myTree.ChildrenItems.add(item);
			NetworkTreeFactory.createNetworkTree(myTree, item);
		}
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	@Override
	protected void onFinish(String errorMessage, boolean interrupted,
			Set<NetworkItem> uncommitedItems) {
		if (interrupted && (!myTree.Item.supportsResumeLoading() || errorMessage != null)) {
			myTree.clearCatalog();
		} else {
			myTree.removeItems(uncommitedItems);
			myTree.updateLoadedTime();
			if (!interrupted) {
				if (errorMessage != null) {
					UIUtil.showMessageText(myActivity, errorMessage);
				} else if (myTree.ChildrenItems.isEmpty()) {
					UIUtil.showErrorMessage(myActivity, "emptyCatalog");
				}
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidateVisibility();
			library.synchronize();
		}
	}
}
