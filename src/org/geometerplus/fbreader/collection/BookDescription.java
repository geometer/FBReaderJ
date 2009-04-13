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
	private final static HashMap<String,BookDescription> ourDescriptions = new HashMap<String,BookDescription>();

	public static BookDescription getDescription(String fileName) {
		return getDescription(fileName, true); 
	} 

	public static BookDescription getDescription(String fileName, boolean checkFile) {
		if (fileName == null) {
			return null;
		}
		String physicalFileName = new ZLFile(fileName).getPhysicalFilePath();
		ZLFile file = new ZLFile(physicalFileName);
		if (checkFile && !file.exists()) {
			return null;
		}
		BookDescription description = ourDescriptions.get(fileName);
		if (description == null) {
			description = new BookDescription(fileName);
			ourDescriptions.put(fileName, description);
		}
		if ((!checkFile || BookDescriptionUtil.checkInfo(file)) && description.isSaved()) {
			return description;
		}

		if (physicalFileName != fileName) {
			BookDescriptionUtil.resetZipInfo(file);
		}
		BookDescriptionUtil.saveInfo(file);

		ZLFile bookFile = new ZLFile(fileName);

		FormatPlugin plugin = PluginCollection.instance().getPlugin(bookFile, false);
		if ((plugin == null) || !plugin.readDescription(fileName, description)) {
			return null;
		}

		String title = description.getTitle();
		if ((title == null) || (title.length() == 0)) {
			description.setTitle(bookFile.getName(true));
		}
		description.save();
		return description;
	}

	public final String FileName;

	private long myBookId;

	private String myEncoding;
	private String myLanguage;
	private String myTitle;
	private ArrayList<Author> myAuthors;
	private ArrayList<Tag> myTags;
	private SeriesInfo mySeriesInfo;

	private boolean myIsSaved;
	private boolean myIsChanged;

	private BookDescription(String fileName) {
		FileName = fileName;
		final BooksDatabase database = BooksDatabase.Instance();
		myBookId = database.loadBook(this);
		if (myBookId >= 0) {
			myAuthors = database.loadAuthors(myBookId);
			myTags = database.loadTags(myBookId);
			mySeriesInfo = database.loadSeriesInfo(myBookId);
			myIsSaved = true;
			myIsChanged = false;
		}
	}

	public boolean isSaved() {
		return myIsSaved;
	}

	public List<Author> authors() {
		return (myAuthors != null) ? Collections.unmodifiableList(myAuthors) : Collections.<Author>emptyList();
	}

	private void addAuthor(Author author) {
		if (author == null) {
			return;
		}
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
			myAuthors.add(author);
			myIsChanged = true;
		} else {
			if (!myAuthors.contains(author)) {
				myAuthors.add(author);
				myIsChanged = true;
			}
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

	public String getTitle() {
		return myTitle;
	}

	public void setTitle(String title) {
		if (!ZLMiscUtil.equals(myTitle, title)) {
			myTitle = title;
			myIsChanged = true;
		}
	}

	public SeriesInfo getSeriesInfo() {
		return mySeriesInfo;
	}

	public void setSeriesInfo(String name, long index) {
		if (mySeriesInfo == null) {
			if (name != null) {
				mySeriesInfo = new SeriesInfo(name, index);
			}
		} else if (name == null) {
			mySeriesInfo = null;
		} else if (!mySeriesInfo.Name.equals(name) || (mySeriesInfo.Index != index)) {
			mySeriesInfo = new SeriesInfo(name, index);
		}
	}

	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!ZLMiscUtil.equals(myLanguage, language)) {
			myLanguage = language;
			myIsChanged = true;
		}
	}

	public String getEncoding() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!ZLMiscUtil.equals(myEncoding, encoding)) {
			myEncoding = encoding;
			myIsChanged = true;
		}
	}

	public List<Tag> tags() {
		return (myTags != null) ? Collections.unmodifiableList(myTags) : Collections.<Tag>emptyList();
	}

	public void addTag(Tag tag) {
		if (tag != null) {
			if (myTags == null) {
				myTags = new ArrayList<Tag>();
			}
			if (!myTags.contains(tag)) {
				myTags.add(tag);
				myIsChanged = true;
			}
		}
	}

	public void addTag(String tagName) {
		addTag(Tag.getTag(null, tagName));
	}

	public boolean save() {
		if (!myIsChanged) {
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

		myIsChanged = false;
		myIsSaved = true;
		return true;
	}
}
