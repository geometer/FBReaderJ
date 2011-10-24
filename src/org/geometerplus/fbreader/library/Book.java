/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.formats.*;

import org.geometerplus.fbreader.Paths;

public class Book {
	public static Book getById(long bookId) {
		final Book book = BooksDatabase.Instance().loadBook(bookId);
		if (book == null) {
			return null;
		}
		book.loadLists();

		final ZLFile bookFile = book.File;
		final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
		if (physicalFile == null) {
			return book;
		}
		if (!physicalFile.exists()) {
			return null;
		}

		FileInfoSet fileInfos = new FileInfoSet(physicalFile);
		if (fileInfos.check(physicalFile, physicalFile != bookFile)) {
			return book;
		}
		fileInfos.save();

		return book.readMetaInfo() ? book : null;
	}

	public static Book getByFile(ZLFile bookFile) {
		if (bookFile == null) {
			return null;
		}

		final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
		if (physicalFile != null && !physicalFile.exists()) {
			return null;
		}

		final FileInfoSet fileInfos = new FileInfoSet(bookFile);

		Book book = BooksDatabase.Instance().loadBookByFile(fileInfos.getId(bookFile), bookFile);
		if (book != null) {
			book.loadLists();
		}

		if (book != null && fileInfos.check(physicalFile, physicalFile != bookFile)) {
			return book;
		}
		fileInfos.save();

		if (book == null) {
			book = new Book(bookFile);
		}
		if (book.readMetaInfo()) {
			book.save();
			return book;
		}
		return null;
	}

	public final ZLFile File;

	private long myId;

	private String myEncoding;
	private String myLanguage;
	private String myTitle;
	private List<Author> myAuthors;
	private List<Tag> myTags;
	private SeriesInfo mySeriesInfo;

	private boolean myIsSaved;

	Book(long id, ZLFile file, String title, String encoding, String language) {
		myId = id;
		File = file;
		myTitle = title;
		myEncoding = encoding;
		myLanguage = language;
		myIsSaved = true;
	}

	Book(ZLFile file) {
		myId = -1;
		File = file;
	}

	public void reloadInfoFromFile() {
		if (readMetaInfo()) {
			save();
		}
	}

	public void reloadInfoFromDatabase() {
		final BooksDatabase database = BooksDatabase.Instance();
		database.reloadBook(this);
		myAuthors = database.loadAuthors(myId);
		myTags = database.loadTags(myId);
		mySeriesInfo = database.loadSeriesInfo(myId);
		myIsSaved = true;
	}

	boolean readMetaInfo() {
		myEncoding = null;
		myLanguage = null;
		myTitle = null;
		myAuthors = null;
		myTags = null;
		mySeriesInfo = null;

		myIsSaved = false;

		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(File);
		if (plugin == null || !plugin.readMetaInfo(this)) {
			return false;
		}
		if (myTitle == null || myTitle.length() == 0) {
			final String fileName = File.getShortName();
			final int index = fileName.lastIndexOf('.');
			setTitle(index > 0 ? fileName.substring(0, index) : fileName);
		}
		final String demoPathPrefix = Paths.BooksDirectoryOption().getValue() + java.io.File.separator + "Demos" + java.io.File.separator;
		if (File.getPath().startsWith(demoPathPrefix)) {
			final String demoTag = Library.resource().getResource("demo").getValue();
			setTitle(getTitle() + " (" + demoTag + ")");
			addTag(demoTag);
		}
		return true;
	}

	private void loadLists() {
		final BooksDatabase database = BooksDatabase.Instance();
		myAuthors = database.loadAuthors(myId);
		myTags = database.loadTags(myId);
		mySeriesInfo = database.loadSeriesInfo(myId);
		myIsSaved = true;
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

	public long getId() {
		return myId;
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

	void setSeriesInfoWithNoCheck(String name, float index) {
		mySeriesInfo = new SeriesInfo(name, index);
	}

	public void setSeriesInfo(String name, float index) {
		if (mySeriesInfo == null) {
			if (name != null) {
				mySeriesInfo = new SeriesInfo(name, index);
				myIsSaved = false;
			}
		} else if (name == null) {
			mySeriesInfo = null;
			myIsSaved = false;
		} else if (!name.equals(mySeriesInfo.Name) || mySeriesInfo.Index != index) {
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

	boolean matches(String pattern) {
		if (myTitle != null && ZLMiscUtil.matchesIgnoreCase(myTitle, pattern)) {
			return true;
		}
		if (mySeriesInfo != null && ZLMiscUtil.matchesIgnoreCase(mySeriesInfo.Name, pattern)) {
			return true;
		}
		if (myAuthors != null) {
			for (Author author : myAuthors) {
				if (ZLMiscUtil.matchesIgnoreCase(author.DisplayName, pattern)) {
					return true;
				}
			}
		}
		if (myTags != null) {
			for (Tag tag : myTags) {
				if (ZLMiscUtil.matchesIgnoreCase(tag.Name, pattern)) {
					return true;
				}
			}
		}
		if (ZLMiscUtil.matchesIgnoreCase(File.getLongName(), pattern)) {
			return true;
		}
		return false;
	}

	public boolean save() {
		if (myIsSaved) {
			return false;
		}
		final BooksDatabase database = BooksDatabase.Instance();
		database.executeAsATransaction(new Runnable() {
			public void run() {
				if (myId >= 0) {
					final FileInfoSet fileInfos = new FileInfoSet(File);
					database.updateBookInfo(myId, fileInfos.getId(File), myEncoding, myLanguage, myTitle);
				} else {
					myId = database.insertBookInfo(File, myEncoding, myLanguage, myTitle);
					storeAllVisitedHyperinks();
				}

				long index = 0;
				database.deleteAllBookAuthors(myId);
				for (Author author : authors()) {
					database.saveBookAuthorInfo(myId, index++, author);
				}
				database.deleteAllBookTags(myId);
				for (Tag tag : tags()) {
					database.saveBookTagInfo(myId, tag);
				}
				database.saveBookSeriesInfo(myId, mySeriesInfo);
			}
		});

		myIsSaved = true;
		return true;
	}

	public ZLTextPosition getStoredPosition() {
		return BooksDatabase.Instance().getStoredPosition(myId);
	}

	public void storePosition(ZLTextPosition position) {
		if (myId != -1) {
			BooksDatabase.Instance().storePosition(myId, position);
		}
	}

	private Set<String> myVisitedHyperlinks;
	private void initHyperlinkSet() {
		if (myVisitedHyperlinks == null) {
			myVisitedHyperlinks = new TreeSet<String>();
			if (myId != -1) {
				myVisitedHyperlinks.addAll(BooksDatabase.Instance().loadVisitedHyperlinks(myId));
			}
		}
	}

	public boolean isHyperlinkVisited(String linkId) {
		initHyperlinkSet();
		return myVisitedHyperlinks.contains(linkId);
	}

	public void markHyperlinkAsVisited(String linkId) {
		initHyperlinkSet();
		if (!myVisitedHyperlinks.contains(linkId)) {
			myVisitedHyperlinks.add(linkId);
			if (myId != -1) {
				BooksDatabase.Instance().addVisitedHyperlink(myId, linkId);
			}
		}
	}

	private void storeAllVisitedHyperinks() {
		if (myId != -1 && myVisitedHyperlinks != null) {
			for (String linkId : myVisitedHyperlinks) {
				BooksDatabase.Instance().addVisitedHyperlink(myId, linkId);
			}
		}
	}

	public void insertIntoBookList() {
		if (myId != -1) {
			BooksDatabase.Instance().insertIntoBookList(myId);
		}
	}

	@Override
	public int hashCode() {
		return (int)myId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Book)) {
			return false;
		}
		return File.equals(((Book)o).File);
	}
}
