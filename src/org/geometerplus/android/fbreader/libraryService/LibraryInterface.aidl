/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;

interface LibraryInterface {
	int size();
	String bookById(in long id);
	String recentBook(in int index);

	List<String> allBookmarks();
	String saveBookmark(in String bookmark);
	void deleteBookmark(in String bookmark);
}
