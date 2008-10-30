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

import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;

class AddBookAction extends FBAction {
	AddBookAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return Reader.getMode() != FBReader.ViewMode.FOOTNOTE;
	}

	public void run() {
		final FBFileHandler handler = new FBFileHandler();
		Runnable actionOnAccept = new Runnable() {
			public void run() {
				final BookDescription description = handler.getDescription();
				if (description == null) {
					return;
				}
				final BookCollection collection = Reader.CollectionView.Collection;
				final String fileName = description.FileName;
				final Runnable action = new Runnable() {
					public void run() {
						Reader.openFile(fileName);
						collection.rebuild(false);
						new BookList().addFileName(fileName);
						Reader.setMode(FBReader.ViewMode.BOOK_TEXT);
						Reader.refreshWindow();
					}
				};
				//new BookInfoDialog(collection, fileName, action).getDialog().run();
				action.run();
			}
		};
		ZLDialogManager.getInstance().runSelectionDialog("addFileDialog", handler, actionOnAccept);
	}
}
