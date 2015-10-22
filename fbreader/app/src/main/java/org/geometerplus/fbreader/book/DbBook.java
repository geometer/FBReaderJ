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

import java.util.*;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.fbreader.formats.*;

public final class DbBook extends AbstractBook {
	public final ZLFile File;

	private Set<String> myVisitedHyperlinks;

	DbBook(long id, ZLFile file, String title, String encoding, String language) {
		super(id, title, encoding, language);
		if (file == null) {
			throw new IllegalArgumentException("Creating book with no file");
		}
		File = file;
	}

	DbBook(ZLFile file, FormatPlugin plugin) throws BookReadingException {
		this(-1, plugin.realBookFile(file), null, null, null);
		BookUtil.readMetainfo(this, plugin);
		mySaveState = SaveState.NotSaved;
	}

	@Override
	public String getPath() {
		return File.getPath();
	}

	void loadLists(BooksDatabase database, PluginCollection pluginCollection) {
		myAuthors = database.listAuthors(myId);
		myTags = database.listTags(myId);
		myLabels = database.listLabels(myId);
		mySeriesInfo = database.getSeriesInfo(myId);
		myUids = database.listUids(myId);
		myProgress = database.getProgress(myId);
		HasBookmark = database.hasVisibleBookmark(myId);
		mySaveState = SaveState.Saved;
		if (myUids == null || myUids.isEmpty()) {
			try {
				BookUtil.getPlugin(pluginCollection, this).readUids(this);
				save(database, false);
			} catch (BookReadingException e) {
			}
		}
	}

	enum WhatIsSaved {
		Nothing,
		Progress,
		Everything;
	}

	WhatIsSaved save(BooksDatabase database, boolean force) {
		if (force || myId == -1) {
			mySaveState = SaveState.NotSaved;
		}

		switch (mySaveState) {
			case Saved:
				return WhatIsSaved.Nothing;
			case ProgressNotSaved:
				return saveProgress(database) ? WhatIsSaved.Progress : WhatIsSaved.Nothing;
			default:
			case NotSaved:	
				return saveFull(database) ? WhatIsSaved.Everything : WhatIsSaved.Nothing;
		}
	}

	private boolean saveProgress(final BooksDatabase database) {
		final boolean[] result = new boolean[] { false };
		database.executeAsTransaction(new Runnable() {
			public void run() {
				if (myId != -1 && myProgress != null) {
					database.saveBookProgress(myId, myProgress);
					result[0] = true;
				}
			}
		});

		if (result[0]) {
			mySaveState = SaveState.Saved;
			return true;
		} else {
			return false;
		}
	}

	private boolean saveFull(final BooksDatabase database) {
		final boolean[] result = new boolean[] { true };
		database.executeAsTransaction(new Runnable() {
			public void run() {
				if (myId >= 0) {
					final FileInfoSet fileInfos = new FileInfoSet(database, File);
					database.updateBookInfo(myId, fileInfos.getId(File), myEncoding, myLanguage, getTitle());
				} else {
					myId = database.insertBookInfo(File, myEncoding, myLanguage, getTitle());
					if (myId == -1) {
						result[0] = false;
						return;
					}
					if (myVisitedHyperlinks != null) {
						for (String linkId : myVisitedHyperlinks) {
							database.addVisitedHyperlink(myId, linkId);
						}
					}
					database.addBookHistoryEvent(myId, BooksDatabase.HistoryEvent.Added);
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
				final List<Label> labelsInDb = database.listLabels(myId);
				for (Label label : labelsInDb) {
					if (myLabels == null || !myLabels.contains(label)) {
						database.removeLabel(myId, label);
					}
				}
				if (myLabels != null) {
					for (Label label : myLabels) {
						database.addLabel(myId, label);
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

		if (result[0]) {
			mySaveState = SaveState.Saved;
			return true;
		} else {
			return false;
		}
	}

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

	boolean hasSameMetainfoAs(DbBook other) {
		return
			ComparisonUtil.equal(getTitle(), other.getTitle()) &&
			ComparisonUtil.equal(myEncoding, other.myEncoding) &&
			ComparisonUtil.equal(myLanguage, other.myLanguage) &&
			ComparisonUtil.equal(myAuthors, other.myAuthors) &&
			MiscUtil.listsEquals(myTags, other.myTags) &&
			ComparisonUtil.equal(mySeriesInfo, other.mySeriesInfo) &&
			ComparisonUtil.equal(myUids, other.myUids);
	}

	void merge(DbBook other, DbBook base) {
		if (!ComparisonUtil.equal(getTitle(), other.getTitle()) &&
			ComparisonUtil.equal(getTitle(), base.getTitle())) {
			setTitle(other.getTitle());
		}
		if (!ComparisonUtil.equal(myEncoding, other.myEncoding) &&
			ComparisonUtil.equal(myEncoding, base.myEncoding)) {
			setEncoding(other.myEncoding);
		}
		if (!ComparisonUtil.equal(myLanguage, other.myLanguage) &&
			ComparisonUtil.equal(myLanguage, base.myLanguage)) {
			setLanguage(other.myLanguage);
		}
		if (!MiscUtil.listsEquals(myTags, other.myTags) &&
			MiscUtil.listsEquals(myTags, base.myTags)) {
			myTags = other.myTags != null ? new ArrayList<Tag>(other.myTags) : null;
			mySaveState = SaveState.NotSaved;
		}
		if (!ComparisonUtil.equal(mySeriesInfo, other.mySeriesInfo) &&
			ComparisonUtil.equal(mySeriesInfo, base.mySeriesInfo)) {
			mySeriesInfo = other.mySeriesInfo;
			mySaveState = SaveState.NotSaved;
		}
		if (!MiscUtil.listsEquals(myUids, other.myUids) &&
			MiscUtil.listsEquals(myUids, base.myUids)) {
			myUids = other.myUids != null ? new ArrayList<UID>(other.myUids) : null;
			mySaveState = SaveState.NotSaved;
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
		if (!(o instanceof DbBook)) {
			return false;
		}
		final DbBook obook = ((DbBook)o);
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
}
