/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.collection.BookCollection;

class ShowBookInfoDialogAction extends FBAction {
	ShowBookInfoDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return Reader.getMode() == FBReader.ViewMode.BOOK_TEXT;
	}

	public void run() {
		final BookCollection collection = Reader.CollectionView.Collection;
		final String fileName = Reader.BookTextView.getFileName();
		Runnable action = new Runnable() {
			public void run() {
				Reader.openFile(fileName);
				collection.rebuild(false);
				Reader.refreshWindow();
			}
		};
		new BookInfoDialog(collection, fileName, action).getDialog().run();
	}
}
