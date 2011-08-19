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
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuItem;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;

abstract class NetworkTreeActions {
	// special values to return from getDefaultActionCode(NetworkTree)
	public static final int TREE_NO_ACTION = -1;
	public static final int TREE_SHOW_CONTEXT_MENU = -2;

	protected final String getTitleValue(String key) {
		return NetworkLibrary.resource().getResource(key).getValue();
	}

	protected final String getTitleValue(String key, String arg) {
		return NetworkLibrary.resource().getResource(key).getValue().replace("%s", arg);
	}

	protected final String getOptionsValue(String key) {
		return NetworkLibrary.resource().getResource("menu").getResource(key).getValue();
	}

	protected final String getOptionsValue(String key, String arg) {
		return NetworkLibrary.resource().getResource("menu").getResource(key).getValue().replace("%s", arg);
	}

	protected final MenuItem addMenuItemWithText(ContextMenu menu, int id, String text) {
		return menu.add(0, id, 0, text).setEnabled(id != TREE_NO_ACTION);
	}

	protected final MenuItem addMenuItem(ContextMenu menu, int id, String key) {
		return addMenuItemWithText(menu, id, getTitleValue(key));
	}

	protected final MenuItem addMenuItem(ContextMenu menu, int id, String key, String arg) {
		return addMenuItemWithText(menu, id, getTitleValue(key, arg));
	}

	protected final MenuItem addOptionsItem(Menu menu, int id, String key/*, int iconId*/) {
		final MenuItem item = menu.add(0, id, 0, getOptionsValue(key));
		//item.setIcon(iconId);
		return item;
	}

	protected final MenuItem addOptionsItem(Menu menu, int id, String key, String arg/*, int iconId*/) {
		final MenuItem item = menu.add(0, id, 0, getOptionsValue(key, arg));
		//item.setIcon(iconId);
		return item;
	}

	protected final MenuItem prepareOptionsItem(Menu menu, int id, boolean state) {
		return menu.findItem(id).setVisible(state).setEnabled(state);
	}

	protected final MenuItem prepareOptionsItem(Menu menu, int id, boolean state, String key, String arg) {
		final MenuItem item = prepareOptionsItem(menu, id, state);
		if (state) {
			item.setTitle(getOptionsValue(key, arg));
		}
		return item;
	}


	public abstract boolean canHandleTree(NetworkTree tree);

	public abstract int getDefaultActionCode(NetworkLibraryActivity activity, NetworkTree tree);

	public abstract boolean runAction(NetworkLibraryActivity activity, NetworkTree tree, int actionCode);

	public void buildContextMenu(NetworkLibraryActivity activity, ContextMenu menu, NetworkTree tree) {
	}

	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	public boolean prepareOptionsMenu(NetworkLibraryActivity activity, Menu menu, NetworkTree tree) {
		return false;
	}
}
