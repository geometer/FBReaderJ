/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;
import org.geometerplus.android.fbreader.api.TextPosition;

interface LibraryInterface {
	void reset(in List<String> bookDirectories, in boolean force);

	String status();

	int size();
	List<String> books(in String query);
	boolean hasBooks(in String query);
	List<String> booksForLabel(in String label);
	List<String> recentBooks();

	String getBookByFile(in String file);
	String getBookById(in long id);
	String getBookByUid(in String type, in String id);
	String getRecentBook(in int index);

	List<String> authors();
	boolean hasSeries();
	List<String> series();
	List<String> tags();
	List<String> titles(in String query);
	List<String> firstTitleLetters();

	List<String> labels();
	List<String> labelsForBook(in String book);
	void setLabel(in String book, in String label);
	void removeLabel(in String book, in String label);

	boolean saveBook(in String book, in boolean force);
	void removeBook(in String book, in boolean deleteFromDisk);
	void addBookToRecentList(in String book);
	void removeBookFromRecentList(in String book);

	TextPosition getStoredPosition(in long bookId);
	void storePosition(in long bookId, in TextPosition position);

	boolean isHyperlinkVisited(in String book, in String linkId);
	void markHyperlinkAsVisited(in String book, in String linkId);

	List<String> invisibleBookmarks(in String book);
	List<String> bookmarks(in long fromId, in int limitCount);
	List<String> bookmarksForBook(in String book, in long fromId, in int limitCount);
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);
}
