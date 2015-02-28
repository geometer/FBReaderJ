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

public interface ActionCode {
	int TREE_SHOW_CONTEXT_MENU = -2;
	int TREE_NO_ACTION = -1;

	int SEARCH = 1;
	int REFRESH = 2;
	int LANGUAGE_FILTER = 3;

	int RELOAD_CATALOG = 11;
	int OPEN_CATALOG = 12;
	int OPEN_IN_BROWSER = 13;
	int OPEN_ROOT = 14;

	int SIGNUP = 21;
	int SIGNIN = 22;
	int SIGNOUT = 23;
	int TOPUP = 24;

	int CUSTOM_CATALOG_ADD = 31;
	int CUSTOM_CATALOG_EDIT = 32;
	int CUSTOM_CATALOG_REMOVE = 33;
	int MANAGE_CATALOGS = 34;
	int DISABLE_CATALOG = 35;

	int BASKET_CLEAR = 41;
	int BASKET_BUY_ALL_BOOKS = 42;

	int DOWNLOAD_BOOK = 51;
	int DOWNLOAD_DEMO = 52;
	int READ_BOOK = 53;
	int READ_DEMO = 54;
	int DELETE_BOOK = 55;
	int DELETE_DEMO = 56;
	int BUY_DIRECTLY = 57;
	int BUY_IN_BROWSER = 58;
	int SHOW_BOOK_ACTIVITY = 59;
	int SHOW_BOOKS = 60;
	int ADD_BOOK_TO_BASKET = 61;
	int REMOVE_BOOK_FROM_BASKET = 62;
	int OPEN_BASKET = 63;
}
