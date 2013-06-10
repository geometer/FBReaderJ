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

	List<String> recentBooks();

	String getBookByFile(in String file);
	String getBookById(in long id);
	String getBookByUid(in String type, in String id);
	String getRecentBook(in int index);

	List<String> authors();
	boolean hasSeries();
	List<String> series();
	List<String> tags();
	List<String> labels();
	List<String> titles(in String query);
	List<String> firstTitleLetters();

	boolean saveBook(in String book, in boolean force);
	void removeBook(in String book, in boolean deleteFromDisk);
	void addBookToRecentList(in String book);

	TextPosition getStoredPosition(in long bookId);
	void storePosition(in long bookId, in TextPosition position);

	boolean isHyperlinkVisited(in String book, in String linkId);
	void markHyperlinkAsVisited(in String book, in String linkId);

	List<String> bookmarks(in String query);
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);

	String getHighlightingStyle(in int styleId);
	List<String> highlightingStyles();
	void saveHighlightingStyle(in String style);
}
