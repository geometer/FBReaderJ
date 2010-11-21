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

package org.geometerplus.android.fbreader.library;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.BookTree;

import org.geometerplus.android.fbreader.FBReader;

abstract class LibraryTreeActivity extends LibraryBaseActivity {
	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		LibraryTree tree = (LibraryTree)((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			final Book book = ((BookTree)tree).Book;
			startActivity(getFBReaderIntent(book.File));
		}
	}

	private Intent getFBReaderIntent(final ZLFile file) {
		final Intent intent = new Intent(getApplicationContext(), FBReader.class);
		intent.setAction(Intent.ACTION_VIEW);
		final ZLFile physicalFile = file.getPhysicalFile();
		if (physicalFile != null) {
			intent.setData(Uri.fromFile(new File(physicalFile.getPath())));
		} else {
			intent.setData(Uri.parse("file:///"));
		}
		intent.putExtra(FBReader.BOOK_PATH_KEY, file.getPath());
		return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}
}
