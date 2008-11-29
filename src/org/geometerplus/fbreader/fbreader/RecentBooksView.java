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

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.collection.BookCollection;
import org.geometerplus.fbreader.description.BookDescription;

public class RecentBooksView extends FBView {
	private static final String LIBRARY = "Recent Books";

	private BookCollection.LastOpenedBooks myLastBooks = new BookCollection.LastOpenedBooks();

	public RecentBooksView(FBReader reader, ZLPaintContext context) {
		super(reader, context);	
	}
	
	public String getCaption() {
		return LIBRARY;
	}
    
	//TODO
	public void rebuild() {
		setModel(null);//, LIBRARY);
	}
	
	public boolean onStylusRelease(int x, int y) {
		if (super.onStylusRelease(x, y)) {
			return false;
		}

		final ArrayList/*<BookDescription>*/ books = myLastBooks.books();

		int index = getParagraphIndexByCoordinate(y);
		if ((index == -1) || (index >= (int)books.size())) {
			return false;
		}

		final FBReader fbreader = (FBReader)Application;
		fbreader.openBook((BookDescription)books.get(index));
		fbreader.showBookTextView();
		return true;
	}

	public void paint() {
		if (getModel() == null) {
			final ZLTextPlainModel recentBooksModel = new ZLTextPlainModel(1024);;
			final ArrayList books = myLastBooks.books();
			for (int i = 0 ; i < books.size(); i++) {
				BookDescription it = (BookDescription)books.get(i);
				recentBooksModel.createParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				recentBooksModel.addControl(FBTextKind.RECENT_BOOK_LIST, true);
				recentBooksModel.addControl(FBTextKind.LIBRARY_AUTHOR_ENTRY, true);
				recentBooksModel.addText((it.getAuthor().getDisplayName() + ". ").toCharArray());
				recentBooksModel.addControl(FBTextKind.LIBRARY_AUTHOR_ENTRY, false);
				recentBooksModel.addControl(FBTextKind.LIBRARY_BOOK_ENTRY, true);
				recentBooksModel.addText(it.getTitle().toCharArray());
			}
			setModel(recentBooksModel);
		}
		super.paint();
	}

	public BookCollection.LastOpenedBooks lastBooks() {
		return myLastBooks;
	}
}
