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


import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.BookTree;

import org.geometerplus.android.fbreader.FBReader;

abstract class LibraryTreeActivity extends LibraryBaseActivity {
	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		LibraryTree tree = (LibraryTree)((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			startActivity(
				new Intent(getApplicationContext(), FBReader.class)
					.setAction(Intent.ACTION_VIEW)
					.putExtra(FBReader.BOOK_PATH_KEY, ((BookTree)tree).Book.File.getPath())
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
			);
		}
	}
}
