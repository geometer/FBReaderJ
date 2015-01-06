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

package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;

abstract class CatalogAction extends Action {
	protected CatalogAction(Activity activity, int code, String resourceKey, int iconId) {
		super(activity, code, resourceKey, iconId);
	}

	protected CatalogAction(Activity activity, int code, String resourceKey) {
		super(activity, code, resourceKey, -1);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}
}
