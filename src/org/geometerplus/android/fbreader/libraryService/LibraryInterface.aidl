/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;

import org.geometerplus.android.fbreader.libraryService.PositionWithTimestamp;

/**
 * Warning: this file is an inteface for communication with plugins
 *    NEVER change method signatures in this file
 *    NEVER change methods order in this file
 *    If you need to add new methods, ADD them AT THE END of the interface
 */
interface LibraryInterface {
	void reset(in boolean force);

	String status();

	int size();
	List<String> books(in String query);
	boolean hasBooks(in String query);

	List<String> recentBooks();

	String getBookByFile(in String file);
	String getBookById(in long id);
	String getBookByUid(in String type, in String id);
	String getBookByHash(in String hash);
	String getRecentBook(in int index);

	List<String> authors();
	boolean hasSeries();
	List<String> series();
	List<String> tags();
	List<String> labels();
	List<String> titles(in String query);
	List<String> firstTitleLetters();

	boolean saveBook(in String book);
	void removeBook(in String book, in boolean deleteFromDisk);
	void addToRecentlyOpened(in String book);

	String getHash(in String book, in boolean force);

	PositionWithTimestamp getStoredPosition(in long bookId);
	void storePosition(in long bookId, in PositionWithTimestamp position);

	boolean isHyperlinkVisited(in String book, in String linkId);
	void markHyperlinkAsVisited(in String book, in String linkId);

	Bitmap getCover(in String book, in int maxWidth, in int maxHeight, out boolean[] delayed);

	List<String> bookmarks(in String query);
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);

	String getHighlightingStyle(in int styleId);
	List<String> highlightingStyles();
	void saveHighlightingStyle(in String style);

	void rescan(in String path);

	void setHash(in String book, in String hash);

	String getCoverUrl(in String bookPath);
	String getDescription(in String book);

	List<String> recentlyAddedBooks(in int count);
	List<String> recentlyOpenedBooks(in int count);
	void removeFromRecentlyOpened(in String book);

	boolean canRemoveBook(in String book, in boolean deleteFromDisk);

	List<String> formats();
	boolean setActiveFormats(in List<String> formats);

	List<String> deletedBookmarkUids();
	void purgeBookmarks(in List<String> uids);

	int getDefaultHighlightingStyleId();
	void setDefaultHighlightingStyleId(in int styleId);
}
