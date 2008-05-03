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

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.model.impl.ZLTextTreeModelImpl;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.*;

class CollectionModel extends ZLTextTreeModelImpl {
	static final String RemoveBookImageId = "removeBook";
	static final String BookInfoImageId = "bookInfo";
	static final String AuthorInfoImageId = "authorInfo";
	static final String SeriesOrderImageId = "seriesOrder";
	static final String TagInfoImageId = "tagInfo";
	static final String RemoveTagImageId = "removeTag";

	private final BookCollection myCollection;
	private final CollectionView myView;

	private final ZLImageMap myImageMap = new ZLImageMap();
	private final HashMap myParagraphToBook = new HashMap();
	private HashMap myBookToParagraphNumber = new HashMap();

	public CollectionModel(CollectionView view, BookCollection collection) {
		myView = view;
		myCollection = collection;

		String prefix = ZLibrary.JAR_DATA_PREFIX + "icons/booktree/";
		myImageMap.put(RemoveBookImageId, new ZLFileImage("image/png", prefix + "tree-remove.png", 0));
		myImageMap.put(BookInfoImageId, new ZLFileImage("image/png", prefix + "tree-bookinfo.png", 0));
		myImageMap.put(AuthorInfoImageId, new ZLFileImage("image/png", prefix + "tree-authorinfo.png", 0));
		myImageMap.put(SeriesOrderImageId, new ZLFileImage("image/png", prefix + "tree-order.png", 0));
		myImageMap.put(TagInfoImageId, new ZLFileImage("image/png", prefix + "tree-taginfo.png", 0));
		myImageMap.put(RemoveTagImageId, new ZLFileImage("image/png", prefix + "tree-removetag.png", 0));
	}

	public BookDescription bookByParagraphNumber(int num) {
		if ((num < 0) || ((int)getParagraphsNumber() <= num)) {
			return null;
		}
		return (BookDescription)myParagraphToBook.get(getParagraph(num));
	}

	public int paragraphNumberByBook(BookDescription book) {
		Integer num = (Integer)myBookToParagraphNumber.get(book);
		return (num != null) ? num.intValue() : -1;
	}

	private void build() {
		if (myCollection.books().isEmpty()) {
			createParagraph(null);
			insertText(FBTextKind.REGULAR, ZLResource.resource("library").getResource("noBooks").getValue());
		} else {
			//if (myView.ShowTagsOption.value()) {
				//buildWithTags();
			//} else {
				buildWithoutTags();
			//}
		}
	}

	private void buildWithoutTags() {
		addBooks(myCollection.books(), null);
	}

	private void addBooks(ArrayList books, ZLTextTreeParagraph root) {
		Author author = null;
		ZLTextTreeParagraph authorParagraph = null;
		String currentSeriesName = null;
		ZLTextTreeParagraph seriesParagraph = null;

		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			BookDescription description = (BookDescription)books.get(i);

			final Author newAuthor = description.getAuthor();
			if (!newAuthor.equals(author)) {
				author = newAuthor;
				authorParagraph = createParagraph(root);
				insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, author.getDisplayName());
				//insertImage(AuthorInfoImageId);
				currentSeriesName = null;
				seriesParagraph = null;
			}

			final String seriesName = description.getSeriesName();
			if (seriesName.length() == 0) {
				currentSeriesName = null;
				seriesParagraph = null;
			} else if (!seriesName.equals(currentSeriesName)) {
				currentSeriesName = seriesName;
				seriesParagraph = createParagraph(authorParagraph);
				insertText(FBTextKind.LIBRARY_BOOK_ENTRY, seriesName);
				//insertImage(SeriesOrderImageId);
			}
			ZLTextTreeParagraph bookParagraph = createParagraph(
				(seriesParagraph == null) ? authorParagraph : seriesParagraph
			);
			insertText(FBTextKind.LIBRARY_BOOK_ENTRY, description.getTitle());
			insertImage(BookInfoImageId);
			if (myCollection.isBookExternal(description)) {
				insertImage(RemoveBookImageId);
			}
			myParagraphToBook.put(bookParagraph, description);
			myBookToParagraphNumber.put(description, getParagraphsNumber() - 1);
			//myBookToParagraph[description].push_back(paragraphsNumber() - 1);
		}
	}

//	private void build1() {
//		final ArrayList/*<Author>*/ authors = myCollection.authors();
//
//		if (authors.isEmpty()) {
//			createParagraph(null);
//			insertText(FBTextKind.REGULAR, ZLResource.resource("library").getResource("noBooks").getValue());
//		} else {
//			String currentSeriesName = "";
//			ZLTextTreeParagraph sequenceParagraph;
//			for (int i = 0; i < authors.size(); i++) {
//				Author it = (Author)authors.get(i);
//				final ArrayList/*<BookDescription>*/ books = myCollection.books(it);
//				if (!books.isEmpty()) {
//					currentSeriesName = "";
//					sequenceParagraph = null;
//                    //todo 
//					ZLTextTreeParagraph authorParagraph = createParagraph(null);
//					insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, it.getDisplayName());
//					//insertImage(AuthorInfoImageId);
//					for (int j = 0; j < books.size(); j++) {
//						BookDescription jt = (BookDescription)books.get(j);
//						final String sequenceName = jt.getSeriesName();
//						if (sequenceName.length() == 0) {
//							currentSeriesName = "";
//							sequenceParagraph = null;
//						} else if (sequenceName != currentSeriesName) {
//							currentSeriesName = sequenceName;
//							sequenceParagraph = createParagraph(authorParagraph);
//							insertText(FBTextKind.LIBRARY_BOOK_ENTRY, sequenceName);
//							//insertImage(SeriesOrderImageId);
//						}
//						ZLTextTreeParagraph bookParagraph = createParagraph(
//							(sequenceParagraph == null) ? authorParagraph : sequenceParagraph
//						);
//						insertText(FBTextKind.LIBRARY_BOOK_ENTRY, jt.getTitle());
//						insertImage(BookInfoImageId);
//						if (myCollection.isBookExternal(jt)) {
//							insertImage(RemoveBookImageId);
//						}
//						myParagraphToBook.put(bookParagraph, jt);
//						myBookToParagraphNumber.put(jt, getParagraphsNumber() - 1);
//					}
//				}
//			}
//		}
//	}

	public void update() {
		myParagraphToBook.clear();
		myBookToParagraphNumber.clear();
		super.clear();
		build();
	}

	private void insertText(byte kind, String text) {
		addControl(kind, true);
		addText(text.toCharArray());
	}

	private void insertImage(String id) {
		addFixedHSpace((short)1);
		addImage(id, myImageMap, (short)0);
	}

	void removeBook(BookDescription book) {
//		removeAllMarks();
//
//		int index = paragraphNumberByBook(book);
//		if (index == 0) {
//			return;
//		}
//		myBookToParagraphNumber.remove(book);
//
//		ZLTextTreeParagraph paragraph = getTreeParagraph(index);
//		int count = 1;
//		for (ZLTextTreeParagraph parent = paragraph.getParent(); (parent != null) && (parent.childNumber() == 1); parent = parent.getParent()) {
//			++count;
//		}
//    
//		if (count > index) {
//			count = index;
//		}
//    
//		HashMap newMap = new HashMap();
//		for (Object b : myBookToParagraphNumber.keySet()) {
//			Integer i = (Integer)myBookToParagraphNumber.get(b);
//			if (i.intValue() < index) {
//				newMap.put(b, i);
//			} else {
//				newMap.put(b, i.intValue() - count);
//			}
//		}
//		myBookToParagraphNumber = newMap;
//
//		for (; count > 0; --count) {
//			removeParagraph(index--);
//		}
	}
}
