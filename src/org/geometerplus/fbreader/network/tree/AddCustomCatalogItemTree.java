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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.NetworkTree;

public class AddCustomCatalogItemTree extends NetworkTree {
	public AddCustomCatalogItemTree(NetworkTree parent) {
		super(parent);
	}

	@Override
	public String getName() {
		return ZLResource.resource("networkView").getResource("addCustomCatalog").getValue();
	}

	@Override
	public String getSummary() {
		return ZLResource.resource("networkView").getResource("addCustomCatalogSummary").getValue();
	}

	@Override
	public NetworkItem getHoldedItem() {
		return null;
	}

	@Override
	protected String getStringId() {
		return "@Add Custom Catalog";
	}
}
