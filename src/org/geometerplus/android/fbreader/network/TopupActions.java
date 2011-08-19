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
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.TopUpTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.api.PluginApi;

class TopupActions extends NetworkTreeActions {
	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof TopUpTree;
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
	}

	@Override
	public int getDefaultActionCode(NetworkLibraryActivity activity, NetworkTree tree) {
		return 0;
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
		runStandalone(activity, link);
		return true;
	}

	public void runStandalone(Activity activity, INetworkLink link) {
		activity.startActivity(
			new Intent(activity, TopupMenuActivity.class)
				.setData(Uri.parse(link.getUrlInfo(UrlInfo.Type.Catalog).Url))
		);
	}
}
