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

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.*;

class CollectionModel extends ZLTextTreeModel {
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
	private final HashMap myParagraphToTag = new HashMap();
	private final HashMap myBookToParagraph = new HashMap();

	public CollectionModel(CollectionView view, BookCollection collection) {
		myView = view;
		myCollection = collection;

		String prefix = ZLibrary.JAR_DATA_PREFIX + "icons/booktree/";
		myImageMap.put(RemoveBookImageId, new ZLFileImage("image/png", prefix + "tree-remove.png"));
		myImageMap.put(BookInfoImageId, new ZLFileImage("image/png", prefix + "tree-bookinfo.png"));
		myImageMap.put(AuthorInfoImageId, new ZLFileImage("image/png", prefix + "tree-authorinfo.png"));
		myImageMap.put(SeriesOrderImageId, new ZLFileImage("image/png", prefix + "tree-order.png"));
		myImageMap.put(TagInfoImageId, new ZLFileImage("image/png", prefix + "tree-taginfo.png"));
		myImageMap.put(RemoveTagImageId, new ZLFileImage("image/png", prefix + "tree-removetag.png"));
	}

	BookDescription getBookByParagraphIndex(int index) {
		if ((index < 0) || ((int)getParagraphsNumber() <= index)) {
			return null;
		}
		return (BookDescription)myParagraphToBook.get(getParagraph(index));
	}

	String getTagByParagraphIndex(int index) {
		if ((index < 0) || ((int)getParagraphsNumber() <= index)) {
			return null;
		}
		return (String)myParagraphToTag.get(getParagraph(index));
	}

	ArrayList paragraphIndicesByBook(BookDescription book) {
		return (ArrayList)myBookToParagraph.get(book);
	}

	private void build() {
		if (myCollection.books().isEmpty()) {
			createParagraph(null);
			insertText(FBTextKind.REGULAR, ZLResource.resource("library").getResource("noBooks").getValue());
		} else {
			if (myView.ShowTagsOption.getValue()) {
				buildWithTags();
			} else {
				buildWithoutTags();
			}
		}
	}

	private void buildWithTags() {
		final ZLResource resource = ZLResource.resource("library");
		final ArrayList books = myCollection.books();

		if (myView.ShowAllBooksTagOption.getValue()) {
			final ZLTextTreeParagraph allBooksParagraph = createParagraph(null);
			insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, resource.getResource("allBooks").getValue());
			insertImage(TagInfoImageId);
			myParagraphToTag.put(allBooksParagraph, CollectionView.SpecialTagAllBooks);
			addBooks(books, allBooksParagraph);
		}

		final TreeMap tagMap = new TreeMap();
		final ArrayList booksWithoutTags = new ArrayList();
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			final BookDescription description = (BookDescription)books.get(i);
			final ArrayList bookTags = description.getTags();
			if (bookTags.isEmpty()) {
				booksWithoutTags.add(description);
			} else {
				final int bookTagsLen = bookTags.size();
				for (int j = 0; j < bookTagsLen; ++j) {
					Object tg = bookTags.get(j);
					ArrayList list = (ArrayList)tagMap.get(tg);
					if (list == null) {
						list = new ArrayList();
						tagMap.put(tg, list);
					}
					list.add(description);
				}
			}
		}

		if (!booksWithoutTags.isEmpty()) {
			final ZLTextTreeParagraph booksWithoutTagsParagraph = createParagraph(null);
			insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, resource.getResource("booksWithoutTags").getValue());
			insertImage(TagInfoImageId);
			myParagraphToTag.put(booksWithoutTagsParagraph, CollectionView.SpecialTagNoTagsBooks);
			addBooks(booksWithoutTags, booksWithoutTagsParagraph);
		}

		final ArrayList tagStack = new ArrayList();
		final HashMap paragraphToTagMap = new HashMap();
		ZLTextTreeParagraph tagParagraph = null;
		for (Iterator it = tagMap.entrySet().iterator(); it.hasNext(); ) {
			final Map.Entry entry = (Map.Entry)it.next();
			final String fullTagName = (String)entry.getKey();
			boolean useExistingTagStack = true;
			for (int index = 0, depth = 0; index != -1; ++depth) {
				final int newIndex = fullTagName.indexOf('/', index);
				String subTag;
				if (newIndex != -1) {
					subTag = fullTagName.substring(index, newIndex);
					index = newIndex + 1;
				} else {
					subTag = fullTagName.substring(index);
					index = -1;
				}

				if (useExistingTagStack) {
					if (tagStack.size() == depth) {
						useExistingTagStack = false;
					} else if (!subTag.equals(tagStack.get(depth))) {
						for (int i = tagStack.size() - depth; i > 0; --i) {
							final String tg = (String)paragraphToTagMap.get(tagParagraph);
							if (tg != null) {
								final ArrayList list = (ArrayList)tagMap.get(tg);
								if (list != null) {
									addBooks(list, tagParagraph);
								}
							}
							tagParagraph = tagParagraph.getParent();
						}
						for (int i = tagStack.size() - 1; i >= depth; --i) {
							tagStack.remove(i);
						}
						useExistingTagStack = false;
					}
				}
				if (!useExistingTagStack) {
					tagStack.add(subTag);
					tagParagraph = createParagraph(tagParagraph);
					myParagraphToTag.put(tagParagraph, (newIndex != -1) ? fullTagName.substring(0, newIndex) : fullTagName);
					insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, subTag);
					insertImage(TagInfoImageId);
					insertImage(RemoveTagImageId);
				}
			}
			paragraphToTagMap.put(tagParagraph, fullTagName);
		}
		while (tagParagraph != null) {
			final String tg = (String)paragraphToTagMap.get(tagParagraph);
			if (tg != null) {
				addBooks((ArrayList)tagMap.get(tg), tagParagraph);
			}
			tagParagraph = tagParagraph.getParent();
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
			ArrayList numbers = (ArrayList)myBookToParagraph.get(description);
			if (numbers == null) {
				numbers = new ArrayList();
				myBookToParagraph.put(description, numbers);
			}
			numbers.add(getParagraphsNumber() - 1);
		}
	}

	public void update() {
		myParagraphToBook.clear();
		myParagraphToTag.clear();
		myBookToParagraph.clear();
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
		final ArrayList indices = (ArrayList)paragraphIndicesByBook(book);
		if (indices == null) {
			return;
		}
		myBookToParagraph.remove(book);
		final int len = indices.size();
		for (int i = len - 1; i >= 0; --i) {
			int index = ((Integer)indices.get(i)).intValue();
			ZLTextTreeParagraph paragraph = getTreeParagraph(index);
			int count = 1;
			for (ZLTextTreeParagraph parent = paragraph.getParent(); (parent != null) && (parent.childNumber() == 1); parent = parent.getParent()) {
				++count;
			}

			if (count > index) {
				count = index;
			}

			for (Iterator it = myBookToParagraph.entrySet().iterator(); it.hasNext(); ) {
				final Map.Entry entry = (Map.Entry)it.next();
				final ArrayList list = (ArrayList)entry.getValue();
				final int listLen = list.size();
				for (int j = 0; j < listLen; ++j) {
					final int v = ((Integer)list.get(j)).intValue();
					if (v >= index) {
						list.set(i, v - count);
					}
				}
			}
    
			for (; count > 0; --count) {
				removeParagraph(index--);
			}
		}
	}
}
