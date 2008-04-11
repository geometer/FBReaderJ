package org.geometerplus.fbreader.fbreader;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextPlainModel;
import org.geometerplus.zlibrary.text.model.impl.ZLModelFactory;

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
	
	public boolean onStylusPress(int x, int y) {
		final ArrayList/*<BookDescription>*/ books = myLastBooks.books();

		int index = getParagraphIndexByCoordinate(y);
		if ((index == -1) || (index >= (int)books.size())) {
			return false;
		}

		getFBReader().openBook((BookDescription)books.get(index));
		getFBReader().showBookTextView();
		return true;
	}

	public void paint() {
		if (getModel() == null) {
			//TODO
			ZLTextPlainModel recentBooksModel = ZLModelFactory.createPlainModel(1024);;
			final ArrayList/*<BookDescription>*/ books = myLastBooks.books();
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
			//setModel(recentBooksModel, LIBRARY);
		}
		super.paint();
	}

	public BookCollection.LastOpenedBooks lastBooks() {
		return myLastBooks;
	}
}
