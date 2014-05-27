/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

class CatalogExpander extends NetworkItemsLoader {
	private final Authenticator myAuthenticator;
	private volatile boolean myAuthenticationRequested;
	private final boolean myResumeNotLoad;

	CatalogExpander(NetworkCatalogTree tree, Authenticator authenticator, boolean resumeNotLoad) {
		super(tree);
		myAuthenticator = authenticator;
		myResumeNotLoad = resumeNotLoad;
	}

	@Override
	public void doBefore() throws ZLNetworkException {
		final INetworkLink link = getTree().getLink();
		if (myAuthenticator != null && link != null && link.authenticationManager() != null) {
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
	public void load() throws ZLNetworkException {
		myAuthenticationRequested = false;
		if (myResumeNotLoad) {
			getTree().Item.resumeLoading(this);
		} else {
			try {
				getTree().Item.loadChildren(this);
			} catch (NetworkCatalogItem.AuthorisationFailed f) {
				myAuthenticationRequested = true;
				if (myAuthenticator != null) {
					myAuthenticator.run(f.URL);
				}
			}
		}
	}

	@Override
	protected void onFinish(ZLNetworkException exception, boolean interrupted) {
		if (interrupted && (!getTree().Item.supportsResumeLoading() || exception != null)) {
			getTree().clearCatalog();
		} else {
			getTree().removeUnconfirmedItems();
			if (!interrupted && !myAuthenticationRequested) {
				if (exception != null) {
					NetworkLibrary.Instance().fireModelChangedEvent(
						NetworkLibrary.ChangeListener.Code.NetworkError, exception.getMessage()
					);
				} else {
					getTree().updateLoadedTime();
					if (getTree().subtrees().isEmpty()) {
						NetworkLibrary.Instance().fireModelChangedEvent(
							NetworkLibrary.ChangeListener.Code.EmptyCatalog
						);
					}
				}
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidateVisibility();
			library.synchronize();
		}
	}
}
