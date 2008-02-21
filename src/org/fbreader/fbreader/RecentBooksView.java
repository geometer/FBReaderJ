package org.fbreader.fbreader;

import java.util.ArrayList;

import org.fbreader.bookmodel.FBTextKind;
import org.fbreader.collection.BookCollection;
import org.fbreader.collection.BookCollection.LastOpenedBooks;
import org.fbreader.description.BookDescription;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

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
			ZLTextPlainModel recentBooksModel = ZLModelFactory.createPlainModel(0);;
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

	public LastOpenedBooks lastBooks() {
		return myLastBooks;
	}
}
