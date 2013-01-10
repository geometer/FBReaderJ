/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.libraryService;

import java.util.List;

interface LibraryInterface {
	String bookById(in long id);
	List<String> allBookmarks();
}
