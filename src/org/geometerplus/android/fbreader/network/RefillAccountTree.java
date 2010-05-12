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

import java.util.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.authentication.*;


class RefillAccountTree extends NetworkTree {

	public final NetworkLink Link;
	public final ZLImage Cover;

	public RefillAccountTree(NetworkCatalogTree parentTree) {
		super(parentTree.Level + 1);
		Link = parentTree.Item.Link;
		Cover = parentTree.getCover();
	}

	@Override
	public String getName() {
		final NetworkAuthenticationManager mgr = Link.authenticationManager();
		if (mgr.isAuthorised(false).Status == ZLBoolean3.B3_TRUE) {
			final String account = mgr.currentAccount();
			if (account != null) {
				return ZLResource.resource("networkView").getResource("refillAccount").getValue().replace("%s", account);
			}
		}
		return ZLResource.resource("networkView").getResource("refillShort").getValue();
	}

	@Override
	public String getSummary() {
		return ZLResource.resource("networkView").getResource("refillSummary").getValue();
	}

	@Override
	protected ZLImage createCover() {
		return Cover;
	}
}
