/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;

interface LibraryInterface {
	int size();
	List<String> books(String pattern);
	List<String> recentBooks();
	List<String> favorites();
	String getBookById(in long id);
	String getRecentBook(in int index);

	void removeBook(in String book, in boolean deleteFromDisk);
	void addBookToRecentList(in String book);
	void setBookFavorite(in String book, in boolean favorite);

	List<String> invisibleBookmarks(in String book);
	List<String> allBookmarks();
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);
}
