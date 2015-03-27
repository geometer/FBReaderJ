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

package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.network.Util;

public class OpenInBrowserAction extends CatalogAction {
	public OpenInBrowserAction(Activity activity) {
		super(activity, ActionCode.OPEN_IN_BROWSER, "openInBrowser");
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!super.isVisible(tree)) {
			return false;
		}

		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return false;
		}

		return ((NetworkURLCatalogItem)item).getUrl(UrlInfo.Type.HtmlPage) != null;
	}

	@Override
	public void run(NetworkTree tree) {
		final String url =
			((NetworkURLCatalogItem)((NetworkCatalogTree)tree).Item).getUrl(UrlInfo.Type.HtmlPage);

		if (Util.isOurLink(url)) {
			Util.openInBrowser(myActivity, url);
		} else {
			final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
			final String message = NetworkLibrary.resource().getResource("confirmQuestions").getResource("openInBrowser").getValue();
			new AlertDialog.Builder(myActivity)
				.setTitle(tree.getName())
				.setMessage(message)
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Util.openInBrowser(myActivity, url);
					}
				})
				.setNegativeButton(buttonResource.getResource("no").getValue(), null)
				.create().show();
		}
	}
}
