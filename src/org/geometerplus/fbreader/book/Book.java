/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.math.BigDecimal;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.RationalNumber;

import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.sort.TitledEntity;

public class Book extends TitledEntity {
	public static final String FAVORITE_LABEL = "favorite";
	public static final String READ_LABEL = "read";
	public static final String SYNCHRONISED_LABEL = "sync-success";
	public static final String SYNC_FAILURE_LABEL = "sync-failure";
	public static final String SYNC_DELETED_LABEL = "sync-deleted";
	public static final String SYNC_TOSYNC_LABEL = "sync-tosync";

	public final ZLFile File;

	private volatile long myId;

	private volatile String myEncoding;
	private volatile String myLanguage;
	private volatile List<Author> myAuthors;
	private volatile List<Tag> myTags;
	private volatile List<String> myLabels;
	private volatile SeriesInfo mySeriesInfo;
	private volatile List<UID> myUids;
	private volatile RationalNumber myProgress;

	public volatile boolean HasBookmark;

	private volatile boolean myIsSaved;

	Book(long id, ZLFile file, String title, String encoding, String language) {
		super(title);
		if (file == null) {
			throw new IllegalArgumentException("Creating book with no file");
		}
		myId = id;
		File = file;
		myEncoding = encoding;
		myLanguage = language;
		myIsSaved = true;
	}

	Book(ZLFile file) throws BookReadingException {
		super(null);
		if (file == null) {
			throw new IllegalArgumentException("Creating book with no file");
		}
		myId = -1;
		final FormatPlugin plugin = getPlugin(file);
		File = plugin.realBookFile(file);
		readMetainfo(plugin);
		myIsSaved = false;
	}

	boolean hasSameMetainfoAs(Book other) {
		return
			MiscUtil.equals(getTitle(), other.getTitle()) &&
			MiscUtil.equals(myEncoding, other.myEncoding) &&
			MiscUtil.equals(myLanguage, other.myLanguage) &&
			MiscUtil.equals(myAuthors, other.myAuthors) &&
			MiscUtil.listsEquals(myTags, other.myTags) &&
			MiscUtil.equals(mySeriesInfo, other.mySeriesInfo) &&
			MiscUtil.equals(myUids, other.myUids);
	}

	void merge(Book other, Book base) {
		if (!MiscUtil.equals(getTitle(), other.getTitle()) &&
			MiscUtil.equals(getTitle(), base.getTitle())) {
			setTitle(other.getTitle());
		}
		if (!MiscUtil.equals(myEncoding, other.myEncoding) &&
			MiscUtil.equals(myEncoding, base.myEncoding)) {
			setEncoding(other.myEncoding);
		}
		if (!MiscUtil.equals(myLanguage, other.myLanguage) &&
			MiscUtil.equals(myLanguage, base.myLanguage)) {
			setLanguage(other.myLanguage);
		}
		if (!MiscUtil.listsEquals(myTags, other.myTags) &&
			MiscUtil.listsEquals(myTags, base.myTags)) {
			myTags = other.myTags != null ? new ArrayList<Tag>(other.myTags) : null;
			myIsSaved = false;
		}
		if (!MiscUtil.equals(mySeriesInfo, other.mySeriesInfo) &&
			MiscUtil.equals(mySeriesInfo, base.mySeriesInfo)) {
			mySeriesInfo = other.mySeriesInfo;
			myIsSaved = false;
		}
		if (!MiscUtil.listsEquals(myUids, other.myUids) &&
			MiscUtil.listsEquals(myUids, base.myUids)) {
			myUids = other.myUids != null ? new ArrayList<UID>(other.myUids) : null;
			myIsSaved = false;
		}
	}

	public void updateFrom(Book book) {
		if (book == null || myId != book.myId) {
			return;
		}
		setTitle(book.getTitle());
		setEncoding(book.myEncoding);
		setLanguage(book.myLanguage);
		if (!MiscUtil.equals(myAuthors, book.myAuthors)) {
			myAuthors = book.myAuthors != null ? new ArrayList<Author>(book.myAuthors) : null;
			myIsSaved = false;
		}
		if (!MiscUtil.equals(myTags, book.myTags)) {
			myTags = book.myTags != null ? new ArrayList<Tag>(book.myTags) : null;
			myIsSaved = false;
		}
		if (!MiscUtil.listsEquals(myLabels, book.myLabels)) {
			myLabels = book.myLabels != null ? new ArrayList<String>(book.myLabels) : null;
			myIsSaved = false;
		}
		if (!MiscUtil.equals(mySeriesInfo, book.mySeriesInfo)) {
			mySeriesInfo = book.mySeriesInfo;
			myIsSaved = false;
		}
		if (!MiscUtil.listsEquals(myUids, book.myUids)) {
			myUids = book.myUids != null ? new ArrayList<UID>(book.myUids) : null;
			myIsSaved = false;
		}
		setProgress(book.myProgress);
		if (HasBookmark != book.HasBookmark) {
			HasBookmark = book.HasBookmark;
			myIsSaved = false;
		}
	}

	public void reloadInfoFromFile() {
		try {
			readMetainfo();
		} catch (BookReadingException e) {
			// ignore
		}
	}

	private static FormatPlugin getPlugin(ZLFile file) throws BookReadingException {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
		if (plugin == null) {
			throw new BookReadingException("pluginNotFound", file);
		}
		return plugin;
	}

	public FormatPlugin getPlugin() throws BookReadingException {
		return getPlugin(File);
	}

	public FormatPlugin getPluginOrNull() {
		return PluginCollection.Instance().getPlugin(File);
	}

	void readMetainfo() throws BookReadingException {
		readMetainfo(getPlugin());
	}

	private void readMetainfo(FormatPlugin plugin) throws BookReadingException {
		myEncoding = null;
		myLanguage = null;
		setTitle(null);
		myAuthors = null;
		myTags = null;
		mySeriesInfo = null;
		myUids = null;

		myIsSaved = false;

		plugin.readMetainfo(this);
		if (myUids == null || myUids.isEmpty()) {
			plugin.readUids(this);
		}

		if (isTitleEmpty()) {
			final String fileName = File.getShortName();
			final int index = fileName.lastIndexOf('.');
			setTitle(index > 0 ? fileName.substring(0, index) : fileName);
		}
	}

	void loadLists(BooksDatabase database) {
		myAuthors = database.listAuthors(myId);
		myTags = database.listTags(myId);
		myLabels = database.listLabels(myId);
		mySeriesInfo = database.getSeriesInfo(myId);
		myUids = database.listUids(myId);
		myProgress = database.getProgress(myId);
		HasBookmark = database.hasVisibleBookmark(myId);
		myIsSaved = true;
		if (myUids == null || myUids.isEmpty()) {
			try {
				final FormatPlugin plugin = getPlugin();
				if (plugin != null) {
					plugin.readUids(this);
					save(database, false);
				}
			} catch (BookReadingException e) {
			}
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

	public void removeAllAuthors() {
		if (myAuthors != null) {
			myAuthors = null;
			myIsSaved = false;
		}
	}

	public void addAuthor(Author author) {
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
		if (name == null) {
			return;
		}
		String strippedName = name.trim();
		if (strippedName.length() == 0) {
			return;
		}

		String strippedKey = sortKey != null ? sortKey.trim() : "";
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

	@Override
	public void setTitle(String title) {
		if (title == null) {
			return;
		}
		title = title.trim();
		if (title.length() == 0) {
			return;
		}
		if (!getTitle().equals(title)) {
			super.setTitle(title);
			myIsSaved = false;
		}
	}

	public SeriesInfo getSeriesInfo() {
		return mySeriesInfo;
	}

	void setSeriesInfoWithNoCheck(String name, String index) {
		mySeriesInfo = SeriesInfo.createSeriesInfo(name, index);
	}

	public void setSeriesInfo(String name, String index) {
		setSeriesInfo(name, SeriesInfo.createIndex(index));
	}

	public void setSeriesInfo(String name, BigDecimal index) {
		if (mySeriesInfo == null) {
			if (name != null) {
				mySeriesInfo = new SeriesInfo(name, index);
				myIsSaved = false;
			}
		} else if (name == null) {
			mySeriesInfo = null;
			myIsSaved = false;
		} else if (!name.equals(mySeriesInfo.Series.getTitle()) || mySeriesInfo.Index != index) {
			mySeriesInfo = new SeriesInfo(name, index);
			myIsSaved = false;
		}
	}

	@Override
	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!MiscUtil.equals(myLanguage, language)) {
			myLanguage = language;
			resetSortKey();
			myIsSaved = false;
		}
	}

	public String getEncoding() {
		if (myEncoding == null) {
			try {
				getPlugin().detectLanguageAndEncoding(this);
			} catch (BookReadingException e) {
			}
			if (myEncoding == null) {
				setEncoding("utf-8");
			}
		}
		return myEncoding;
	}

	public String getEncodingNoDetection() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!MiscUtil.equals(myEncoding, encoding)) {
			myEncoding = encoding;
			myIsSaved = false;
		}
	}

	public List<Tag> tags() {
		return myTags != null ? Collections.unmodifiableList(myTags) : Collections.<Tag>emptyList();
	}

	void addTagWithNoCheck(Tag tag) {
		if (myTags == null) {
			myTags = new ArrayList<Tag>();
		}
		myTags.add(tag);
	}

	public void removeAllTags() {
		if (myTags != null) {
			myTags = null;
			myIsSaved = false;
		}
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

	public List<String> labels() {
		return myLabels != null ? Collections.unmodifiableList(myLabels) : Collections.<String>emptyList();
	}

	void addLabelWithNoCheck(String label) {
		if (myLabels == null) {
			myLabels = new ArrayList<String>();
		}
		myLabels.add(label);
	}

	public void addLabel(String label) {
		if (myLabels == null) {
			myLabels = new ArrayList<String>();
		}
		if (!myLabels.contains(label)) {
			myLabels.add(label);
			myIsSaved = false;
		}
	}

	public void removeLabel(String label) {
		if (myLabels != null && myLabels.remove(label)) {
			myIsSaved = false;
		}
	}

	public List<UID> uids() {
		return myUids != null ? Collections.unmodifiableList(myUids) : Collections.<UID>emptyList();
	}

	public void addUid(String type, String id) {
		addUid(new UID(type, id));
	}

	void addUidWithNoCheck(UID uid) {
		if (uid == null) {
			return;
		}
		if (myUids == null) {
			myUids = new ArrayList<UID>();
		}
		myUids.add(uid);
	}

	public void addUid(UID uid) {
		if (uid == null) {
			return;
		}
		if (myUids == null) {
			myUids = new ArrayList<UID>();
		}
		if (!myUids.contains(uid)) {
			myUids.add(uid);
			myIsSaved = false;
		}
	}

	public boolean matchesUid(UID uid) {
		return myUids.contains(uid);
	}

	public RationalNumber getProgress() {
		return myProgress;
	}

	public void setProgress(RationalNumber progress) {
		if (!MiscUtil.equals(myProgress, progress)) {
			myProgress = progress;
			myIsSaved = false;
		}
	}

	public void setProgressWithNoCheck(RationalNumber progress) {
		myProgress = progress;
	}

	public boolean matches(String pattern) {
		if (MiscUtil.matchesIgnoreCase(getTitle(), pattern)) {
			return true;
		}
		if (mySeriesInfo != null && MiscUtil.matchesIgnoreCase(mySeriesInfo.Series.getTitle(), pattern)) {
			return true;
		}
		if (myAuthors != null) {
			for (Author author : myAuthors) {
				if (MiscUtil.matchesIgnoreCase(author.DisplayName, pattern)) {
					return true;
				}
			}
		}
		if (myTags != null) {
			for (Tag tag : myTags) {
				if (MiscUtil.matchesIgnoreCase(tag.Name, pattern)) {
					return true;
				}
			}
		}
		if (MiscUtil.matchesIgnoreCase(File.getLongName(), pattern)) {
			return true;
		}
		return false;
	}

	boolean save(final BooksDatabase database, boolean force) {
		if (!force && myId != -1 && myIsSaved) {
			return false;
		}

		database.executeAsTransaction(new Runnable() {
			public void run() {
				if (myId >= 0) {
					final FileInfoSet fileInfos = new FileInfoSet(database, File);
					database.updateBookInfo(myId, fileInfos.getId(File), myEncoding, myLanguage, getTitle());
				} else {
					myId = database.insertBookInfo(File, myEncoding, myLanguage, getTitle());
					if (myId != -1 && myVisitedHyperlinks != null) {
						for (String linkId : myVisitedHyperlinks) {
							database.addVisitedHyperlink(myId, linkId);
						}
					}
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
				final List<String> labelsInDb = database.listLabels(myId);
				for (String label : labelsInDb) {
					if (myLabels == null || !myLabels.contains(label)) {
						database.removeLabel(myId, label);
					}
				}
				if (myLabels != null) {
					for (String label : myLabels) {
						database.setLabel(myId, label);
					}
				}
				database.saveBookSeriesInfo(myId, mySeriesInfo);
				database.deleteAllBookUids(myId);
				for (UID uid : uids()) {
					database.saveBookUid(myId, uid);
				}
				if (myProgress != null) {
					database.saveBookProgress(myId, myProgress);
				}
			}
		});

		myIsSaved = true;
		return true;
	}

	private Set<String> myVisitedHyperlinks;
	private void initHyperlinkSet(BooksDatabase database) {
		if (myVisitedHyperlinks == null) {
			myVisitedHyperlinks = new TreeSet<String>();
			if (myId != -1) {
				myVisitedHyperlinks.addAll(database.loadVisitedHyperlinks(myId));
			}
		}
	}

	boolean isHyperlinkVisited(BooksDatabase database, String linkId) {
		initHyperlinkSet(database);
		return myVisitedHyperlinks.contains(linkId);
	}

	void markHyperlinkAsVisited(BooksDatabase database, String linkId) {
		initHyperlinkSet(database);
		if (!myVisitedHyperlinks.contains(linkId)) {
			myVisitedHyperlinks.add(linkId);
			if (myId != -1) {
				database.addVisitedHyperlink(myId, linkId);
			}
		}
	}

	@Override
	public int hashCode() {
		return File.getShortName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Book)) {
			return false;
		}
		final Book obook = ((Book)o);
		final ZLFile ofile = obook.File;
		if (File.equals(ofile)) {
			return true;
		}
		if (!File.getShortName().equals(ofile.getShortName())) {
			return false;
		}
		if (myUids == null || obook.myUids == null) {
			return false;
		}
		for (UID uid : obook.myUids) {
			if (myUids.contains(uid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return new StringBuilder("Book[")
			.append(File.getPath())
			.append(", ")
			.append(myId)
			.append(", ")
			.append(getTitle())
			.append("]")
			.toString();
	}
}
