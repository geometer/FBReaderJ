/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.collection;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.formats.*;

public class BookDescription {
	public static BookDescription getDescription(String fileName) {
		return getDescription(fileName, null, null, true);
	}

	static BookDescription getDescription(String fileName, ZLFile file, BookDescription description, boolean readFromDB) {
		if (fileName == null) {
			return null;
		}

		final ZLFile bookFile = new ZLFile(fileName);
		String physicalFileName;
		if (file == null) {
			physicalFileName = bookFile.getPhysicalFilePath();
			file = new ZLFile(physicalFileName);
			if (!file.exists()) {
				return null;
			}
		} else {
			physicalFileName = file.getPath();
		}

		if (description == null) {
			description = new BookDescription(fileName, readFromDB);
		}

		if (BookDescriptionUtil.checkInfo(file) && description.myIsSaved) {
			return description;
		}

		if (physicalFileName != fileName) {
			BookDescriptionUtil.resetZipInfo(file);
		}
		BookDescriptionUtil.saveInfo(file);

		final FormatPlugin plugin = PluginCollection.instance().getPlugin(bookFile);
		if ((plugin == null) || !plugin.readDescription(fileName, description)) {
			return null;
		}

		String title = description.getTitle();
		if ((title == null) || (title.length() == 0)) {
			description.setTitle(bookFile.getName(true));
		}
		return description;
	}

	public final String FileName;

	private long myBookId;

	private String myEncoding;
	private String myLanguage;
	private String myTitle;
	private List<Author> myAuthors;
	private ArrayList<Tag> myTags;
	private SeriesInfo mySeriesInfo;

	private boolean myIsSaved;

	BookDescription(long bookId, String fileName, String title, String encoding, String language) {
		myBookId = bookId;
		FileName = fileName;
		myTitle = title;
		myEncoding = encoding;
		myLanguage = language;
		myIsSaved = true;
	}

	private BookDescription(String fileName, boolean createFromDatabase) {
		FileName = fileName;
		if (createFromDatabase) {
			final BooksDatabase database = BooksDatabase.Instance();
			myBookId = database.loadBook(this);
			if (myBookId >= 0) {
				myAuthors = database.loadAuthors(myBookId);
				myTags = database.loadTags(myBookId);
				mySeriesInfo = database.loadSeriesInfo(myBookId);
				myIsSaved = true;
			}
		} else {
			myBookId = -1;
		}
	}

	public List<Author> authors() {
		return (myAuthors != null) ? Collections.unmodifiableList(myAuthors) : Collections.<Author>emptyList();
	}

	void addAuthorWithNoCheck(Author author) {
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
		}
		myAuthors.add(author);
	}

	private void addAuthor(Author author) {
		if (author == null) {
			return;
		}
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
			myAuthors.add(author);
			myIsSaved = false;
		} else if (!myAuthors.contains(author)) {
			myAuthors.add(author);
			myIsSaved = false;
		}
	}

	public void addAuthor(String name) {
		addAuthor(name, "");
	}

	public void addAuthor(String name, String sortKey) {
		String strippedName = name;
		strippedName.trim();
		if (strippedName.length() == 0) {
			return;
		}

		String strippedKey = sortKey;
		strippedKey.trim();
		if (strippedKey.length() == 0) {
			int index = strippedName.lastIndexOf(' ');
			if (index == -1) {
				strippedKey = strippedName;
			} else {
				strippedKey = strippedName.substring(index + 1);
				while ((index >= 0) && (strippedName.charAt(index) == ' ')) {
					--index;
				}
				strippedName = strippedName.substring(0, index + 1) + ' ' + strippedKey;
			}
		}

		addAuthor(new Author(strippedName, strippedKey));
	}

	public long getBookId() {
		return myBookId;
	}

	public String getTitle() {
		return myTitle;
	}

	public void setTitle(String title) {
		if (!ZLMiscUtil.equals(myTitle, title)) {
			myTitle = title;
			myIsSaved = false;
		}
	}

	public SeriesInfo getSeriesInfo() {
		return mySeriesInfo;
	}

	void setSeriesInfoWithNoCheck(String name, long index) {
		mySeriesInfo = new SeriesInfo(name, index);
	}

	public void setSeriesInfo(String name, long index) {
		if (mySeriesInfo == null) {
			if (name != null) {
				mySeriesInfo = new SeriesInfo(name, index);
				myIsSaved = false;
			}
		} else if (name == null) {
			mySeriesInfo = null;
			myIsSaved = false;
		} else if (!mySeriesInfo.Name.equals(name) || (mySeriesInfo.Index != index)) {
			mySeriesInfo = new SeriesInfo(name, index);
			myIsSaved = false;
		}
	}

	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!ZLMiscUtil.equals(myLanguage, language)) {
			myLanguage = language;
			myIsSaved = false;
		}
	}

	public String getEncoding() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!ZLMiscUtil.equals(myEncoding, encoding)) {
			myEncoding = encoding;
			myIsSaved = false;
		}
	}

	public List<Tag> tags() {
		return (myTags != null) ? Collections.unmodifiableList(myTags) : Collections.<Tag>emptyList();
	}

	void addTagWithNoCheck(Tag tag) {
		if (myTags == null) {
			myTags = new ArrayList<Tag>();
		}
		myTags.add(tag);
	}

	public void addTag(Tag tag) {
		if (tag != null) {
			if (myTags == null) {
				myTags = new ArrayList<Tag>();
			}
			if (!myTags.contains(tag)) {
				myTags.add(tag);
				myIsSaved = false;
			}
		}
	}

	public void addTag(String tagName) {
		addTag(Tag.getTag(null, tagName));
	}

	public boolean save() {
		if (myIsSaved) {
			return false;
		}
		final BooksDatabase database = BooksDatabase.Instance();
		database.executeAsATransaction(new Runnable() {
			public void run() {
				if (myBookId >= 0) {
					database.updateBookInfo(myBookId, myEncoding, myLanguage, myTitle);
				} else {
					myBookId = database.insertBookInfo(FileName, myEncoding, myLanguage, myTitle);
				}
            
				long index = 0;
				database.deleteAllBookAuthors(myBookId);
				for (Author author : authors()) {
					database.saveBookAuthorInfo(myBookId, index++, author);
				}
				database.deleteAllBookTags(myBookId);
				for (Tag tag : tags()) {
					database.saveBookTagInfo(myBookId, tag);
				}
				database.saveBookSeriesInfo(myBookId, mySeriesInfo);
			}
		});

		myIsSaved = true;
		return true;
	}
}
