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

public interface ActionCode {
	int TREE_SHOW_CONTEXT_MENU = -2;
	int TREE_NO_ACTION = -1;

	int SEARCH = 0;
	int REFRESH = 1;
	int LANGUAGE_FILTER = 2;

	int RELOAD_CATALOG = 3;
	int OPEN_CATALOG = 4;
	int OPEN_IN_BROWSER = 5;

	int SIGNUP = 6;
	int SIGNIN = 7;
	int SIGNOUT = 8;
	int TOPUP = 9;

	int CUSTOM_CATALOG_ADD = 10;
	int CUSTOM_CATALOG_EDIT = 11;
	int CUSTOM_CATALOG_REMOVE = 12;

	int BASKET_CLEAR = 13;
	int BASKET_BUY_ALL_BOOKS = 14;
}
