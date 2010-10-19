/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import java.util.HashMap;

import org.geometerplus.fbreader.bookmodel.BookModel;

import org.geometerplus.android.fbreader.LibraryTabActivity;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

class ShowLibraryAction extends FBAction {
	ShowLibraryAction(FBReader fbreader) {
		super(fbreader);
	}

	public void run() {
		final ZLAndroidDialogManager dialogManager =
			(ZLAndroidDialogManager)ZLAndroidDialogManager.Instance();
		final HashMap<String,String> data = new HashMap<String,String>();
		final BookModel model = Reader.Model;
		if (model != null) {
			data.put(LibraryTabActivity.CURRENT_BOOK_PATH_KEY, model.Book.File.getPath());
		}
		Runnable action = new Runnable() {
			public void run() {
				dialogManager.runActivity(LibraryTabActivity.class, data);
			}
		};
		dialogManager.wait("loadingBookList", action);
	}
}
