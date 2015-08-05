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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

class CatalogExpander extends NetworkItemsLoader {
	private final boolean myAuthenticate;
	private final boolean myResumeNotLoad;

	CatalogExpander(ZLNetworkContext nc, NetworkCatalogTree tree, boolean authenticate, boolean resumeNotLoad) {
		super(nc, tree);
		myAuthenticate = authenticate;
		myResumeNotLoad = resumeNotLoad;
	}

	@Override
	public void doBefore() throws ZLNetworkException {
		final INetworkLink link = Tree.getLink();
		if (myAuthenticate && link != null && link.authenticationManager() != null) {
			final NetworkAuthenticationManager mgr = link.authenticationManager();
			try {
				if (mgr.isAuthorised(true) && mgr.needsInitialization()) {
					new Thread() {
						public void run() {
							try {
								mgr.initialize();
							} catch (ZLNetworkException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			} catch (ZLNetworkException e) {
				mgr.logOut();
			}
		}
	}

	@Override
	public void load() throws ZLNetworkException {
		if (myResumeNotLoad) {
			Tree.Item.resumeLoading(this);
		} else {
			Tree.Item.loadChildren(this);
		}
	}

	@Override
	protected void onFinish(ZLNetworkException exception, boolean interrupted) {
		if (interrupted && (!Tree.Item.supportsResumeLoading() || exception != null)) {
			Tree.clearCatalog();
		} else {
			Tree.removeUnconfirmedItems();
			if (!interrupted) {
				if (exception != null) {
					Tree.Library.fireModelChangedEvent(
						NetworkLibrary.ChangeListener.Code.NetworkError, exception.getMessage()
					);
				} else {
					Tree.updateLoadedTime();
					if (Tree.subtrees().isEmpty()) {
						Tree.Library.fireModelChangedEvent(
							NetworkLibrary.ChangeListener.Code.EmptyCatalog
						);
					}
				}
			}
			Tree.Library.invalidateVisibility();
			Tree.Library.synchronize();
		}
	}
}
