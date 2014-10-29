/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.api;

import android.content.Intent;

import org.geometerplus.fbreader.book.*;

public abstract class FBReaderIntents {
	public static final String DEFAULT_PACKAGE = "org.geometerplus.zlibrary.ui.android";

	public interface Action {
		String API                      = "android.fbreader.action.API";
		String API_CALLBACK             = "android.fbreader.action.API_CALLBACK";
		String VIEW                     = "android.fbreader.action.VIEW";
		String CANCEL_MENU              = "android.fbreader.action.CANCEL_MENU";
		String CONFIG_SERVICE           = "android.fbreader.action.CONFIG_SERVICE";
		String LIBRARY_SERVICE          = "android.fbreader.action.LIBRARY_SERVICE";
		String BOOK_INFO                = "android.fbreader.action.BOOK_INFO";
		String LIBRARY                  = "android.fbreader.action.LIBRARY";
		String EXTERNAL_LIBRARY         = "android.fbreader.action.EXTERNAL_LIBRARY";
		String BOOKMARKS                = "android.fbreader.action.BOOKMARKS";
		String EXTERNAL_BOOKMARKS       = "android.fbreader.action.EXTERNAL_BOOKMARKS";
		String PREFERENCES              = "android.fbreader.action.PREFERENCES";
		String NETWORK_LIBRARY          = "android.fbreader.action.NETWORK_LIBRARY";
		String OPEN_NETWORK_CATALOG     = "android.fbreader.action.OPEN_NETWORK_CATALOG";
		String ERROR                    = "android.fbreader.action.ERROR";
		String CRASH                    = "android.fbreader.action.CRASH";
		String PLUGIN                   = "android.fbreader.action.PLUGIN";
		String CLOSE                    = "android.fbreader.action.CLOSE";
		String PLUGIN_CRASH             = "android.fbreader.action.PLUGIN_CRASH";
		String EDIT_STYLES              = "android.fbreader.action.EDIT_STYLES";
		String SWITCH_YOTA_SCREEN       = "android.fbreader.action.SWITCH_YOTA_SCREEN";
	}

	public interface Key {
		String BOOK                     = "fbreader.book";
		String BOOKMARK                 = "fbreader.bookmark";
		String PLUGIN                   = "fbreader.plugin";
		String TYPE                     = "fbreader.type";
	}

	public static Intent defaultInternalIntent(String action) {
		return internalIntent(action).addCategory(Intent.CATEGORY_DEFAULT);
	}

	public static Intent internalIntent(String action) {
		return new Intent(action).setPackage(DEFAULT_PACKAGE);
	}

	public static void putBookExtra(Intent intent, String key, Book book) {
		intent.putExtra(key, SerializerUtil.serialize(book));
	}

	public static void putBookExtra(Intent intent, Book book) {
		putBookExtra(intent, Key.BOOK, book);
	}

	public static Book getBookExtra(Intent intent, String key) {
		return SerializerUtil.deserializeBook(intent.getStringExtra(key));
	}

	public static Book getBookExtra(Intent intent) {
		return getBookExtra(intent, Key.BOOK);
	}

	public static void putBookmarkExtra(Intent intent, String key, Bookmark bookmark) {
		intent.putExtra(key, SerializerUtil.serialize(bookmark));
	}

	public static void putBookmarkExtra(Intent intent, Bookmark bookmark) {
		putBookmarkExtra(intent, Key.BOOKMARK, bookmark);
	}

	public static Bookmark getBookmarkExtra(Intent intent, String key) {
		return SerializerUtil.deserializeBookmark(intent.getStringExtra(key));
	}

	public static Bookmark getBookmarkExtra(Intent intent) {
		return getBookmarkExtra(intent, Key.BOOKMARK);
	}
}
