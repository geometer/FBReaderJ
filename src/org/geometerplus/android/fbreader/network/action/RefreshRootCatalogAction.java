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

import android.app.Activity;
import android.app.AlertDialog;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

public class RefreshRootCatalogAction extends RootAction {
	public RefreshRootCatalogAction(Activity activity) {
		super(activity, ActionCode.REFRESH, "refreshCatalogsList", R.drawable.ic_menu_refresh);
	}

	@Override
	protected void run(NetworkTree tree) {
		UIUtil.wait("updatingCatalogsList", new Runnable() {
			public void run() {
				try {
					NetworkLibrary.Instance().runBackgroundUpdate(true);
				} catch (final ZLNetworkException e) {
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource boxResource = dialogResource.getResource("networkError");
					final ZLResource buttonResource = dialogResource.getResource("button");
					myActivity.runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(myActivity)
								.setTitle(boxResource.getResource("title").getValue())
								.setMessage(e.getMessage())
								.setIcon(0)
								.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
								.create().show();
						}
					});
				}
			}
		}, myActivity);
	}
}
