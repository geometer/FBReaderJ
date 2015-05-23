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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;

public class DbBook extends AbstractBook {
	private Set<String> myVisitedHyperlinks;

	DbBook(long id, ZLFile file, String title, String encoding, String language) {
		super(id, file, title, encoding, language);
	}

	DbBook(ZLFile file, FormatPlugin plugin) throws BookReadingException {
		super(-1, plugin.realBookFile(file), null, null, null);
		BookUtil.readMetainfo(this, plugin);
		myIsSaved = false;
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
				BookUtil.getPlugin(this).readUids(this);
				save(database, false);
			} catch (BookReadingException e) {
			}
		}
	}

	boolean save(final BooksDatabase database, boolean force) {
		if (!force && myId != -1 && myIsSaved) {
			return false;
		}

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

		if (result[0]) {
			myIsSaved = true;
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
}
