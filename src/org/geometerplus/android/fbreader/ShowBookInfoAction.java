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

package org.geometerplus.android.fbreader;

import android.content.Intent;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.BookModel;

import org.geometerplus.android.fbreader.preferences.BookInfoActivity;

class ShowBookInfoAction extends FBAction {
	private final FBReader myBaseActivity;

	ShowBookInfoAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(fbreader);
		myBaseActivity = baseActivity;
	}

	public boolean isVisible() {
		return Reader.Model != null;
	}

	public void run() {
		final Intent intent = new Intent(myBaseActivity.getApplicationContext(), BookInfoActivity.class);
		final BookModel model = Reader.Model;
		if (model != null && model.Book != null) {
			final ZLFile file = model.Book.File;
			final ZLFile physicalFile = file.getPhysicalFile();
			if (physicalFile == null || physicalFile == file) {
				intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, file.getPath());
			} else {
				intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, physicalFile.getPath());
				intent.putExtra(BookInfoActivity.CURRENT_BOOK_ARCHIVE_ENTRY_KEY, file.getName(false));
			}
		}
		myBaseActivity.startActivityForResult(
			intent, FBReader.REPAINT_CODE
		);
	}
}
