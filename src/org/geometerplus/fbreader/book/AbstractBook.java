/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.RationalNumber;

import org.geometerplus.fbreader.sort.TitledEntity;

public abstract class AbstractBook extends TitledEntity<AbstractBook> {
	public static final String FAVORITE_LABEL = "favorite";
	public static final String READ_LABEL = "read";
	public static final String SYNCHRONISED_LABEL = "sync-success";
	public static final String SYNC_FAILURE_LABEL = "sync-failure";
	public static final String SYNC_DELETED_LABEL = "sync-deleted";
	public static final String SYNC_TOSYNC_LABEL = "sync-tosync";

	protected volatile long myId;

	protected volatile String myEncoding;
	protected volatile String myLanguage;
	protected volatile List<Author> myAuthors;
	protected volatile List<Tag> myTags;
	protected volatile List<Label> myLabels;
	protected volatile SeriesInfo mySeriesInfo;
	protected volatile List<UID> myUids;
	protected volatile RationalNumber myProgress;

	public volatile boolean HasBookmark;

	protected volatile boolean myIsSaved;

	AbstractBook(long id, String title, String encoding, String language) {
		super(title);
		myId = id;
		myEncoding = encoding;
		myLanguage = language;
		myIsSaved = true;
	}

	public abstract String getPath();

	public void updateFrom(AbstractBook book) {
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
			myLabels = book.myLabels != null ? new ArrayList<Label>(book.myLabels) : null;
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

	public boolean hasLabel(String name) {
		for (Label l : labels()) {
			if (name.equals(l.Name)) {
				return true;
			}
		}
		return false;
	}

	public List<Label> labels() {
		return myLabels != null ? Collections.unmodifiableList(myLabels) : Collections.<Label>emptyList();
	}

	void addLabelWithNoCheck(Label label) {
		if (myLabels == null) {
			myLabels = new ArrayList<Label>();
		}
		myLabels.add(label);
	}

	public void addNewLabel(String label) {
		addLabel(new Label(label));
	}

	public void addLabel(Label label) {
		if (myLabels == null) {
			myLabels = new ArrayList<Label>();
		}
		if (!myLabels.contains(label)) {
			myLabels.add(label);
			myIsSaved = false;
		}
	}

	public void removeLabel(String label) {
		if (myLabels != null && myLabels.remove(new Label(label))) {
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

		String fileName = getPath();
		// first archive delimiter
		int index = fileName.indexOf(":");
		// last path delimiter before first archive delimiter
		if (index == -1) {
			index = fileName.lastIndexOf("/");
		} else {
			index = fileName.lastIndexOf("/", index);
		}
		fileName = fileName.substring(index + 1);
		if (MiscUtil.matchesIgnoreCase(fileName, pattern)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[" + getPath() + ", " + myId + ", " + getTitle() + "]";
	}
}
