/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;
import org.geometerplus.android.fbreader.api.TextPosition;

interface LibraryInterface {
	String status();
	int size();
	List<String> books();
	List<String> booksForAuthor(in String author);
	List<String> booksForTag(in String tag);
	List<String> booksForSeries(in String series);
	List<String> booksForSeriesAndAuthor(in String series, in String author);
	List<String> booksForTitlePrefix(in String prefix);
	boolean hasBooksForPattern(in String pattern);
	List<String> booksForPattern(in String pattern);
	List<String> recentBooks();
	List<String> favorites();
	String getBookByFile(in String file);
	String getBookById(in long id);
	String getRecentBook(in int index);

	List<String> authors();
	boolean hasSeries();
	List<String> series();
	List<String> tags();
	List<String> titles();
	List<String> titlesForAuthor(in String author, int limit);
	List<String> titlesForSeries(in String series, int limit);
	List<String> titlesForSeriesAndAuthor(in String series, in String author, int limit);
	List<String> titlesForTag(in String tag, int limit);
	List<String> titlesForTitlePrefix(in String prefix, int limit);

	boolean saveBook(in String book, in boolean force);
	void removeBook(in String book, in boolean deleteFromDisk);
	void addBookToRecentList(in String book);

	boolean hasFavorites();
	boolean isFavorite(in String book);
	void setBookFavorite(in String book, in boolean favorite);

	TextPosition getStoredPosition(in long bookId);
	void storePosition(in long bookId, in TextPosition position);

	boolean isHyperlinkVisited(in String book, in String linkId);
	void markHyperlinkAsVisited(in String book, in String linkId);

	List<String> invisibleBookmarks(in String book);
	List<String> allBookmarks();
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);
}
