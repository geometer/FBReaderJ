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

import java.util.ArrayList;

import org.geometerplus.fbreader.description.BookDescription;

class OpenPreviousBookAction extends FBAction {
	OpenPreviousBookAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		switch (Reader.getMode()) {
			case FBReader.ViewMode.BOOK_TEXT:
			case FBReader.ViewMode.CONTENTS:
				return Reader.RecentBooksView.lastBooks().books().size() > 1;
			default:
				return false;
		}
	}

	public void run() {
		final ArrayList books = Reader.RecentBooksView.lastBooks().books();
		Reader.openBook((BookDescription)books.get(1));
		Reader.refreshWindow();
	}
}
