/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public abstract class BooksDatabase {
	protected interface HistoryEvent {
		int Added = 0;
		int Opened = 1;
	}

	public static final class NotAvailable extends Exception {
	}

	protected DbBook createBook(long id, long fileId, String title, String encoding, String language) {
		final FileInfoSet infos = new FileInfoSet(this, fileId);
		return createBook(id, infos.getFile(fileId), title, encoding, language);
	}
	protected DbBook createBook(long id, ZLFile file, String title, String encoding, String language) {
		return file != null ? new DbBook(id, file, title, encoding, language) : null;
	}
	protected void addAuthor(DbBook book, Author author) {
		book.addAuthorWithNoCheck(author);
	}
	protected void addTag(DbBook book, Tag tag) {
		book.addTagWithNoCheck(tag);
	}
	protected void setSeriesInfo(DbBook book, String series, String index) {
		book.setSeriesInfoWithNoCheck(series, index);
	}

	protected abstract void executeAsTransaction(Runnable actions);

	// returns map fileId -> book
	protected abstract Map<Long,DbBook> loadBooks(FileInfoSet infos, boolean existing);
	protected abstract void setExistingFlag(Collection<DbBook> books, boolean flag);
	protected abstract DbBook loadBook(long bookId);
	protected abstract DbBook loadBookByFile(long fileId, ZLFile file);
	protected abstract void deleteBook(long bookId);

	protected abstract List<String> listLabels();

	protected abstract List<Author> listAuthors(long bookId);
	protected abstract List<Tag> listTags(long bookId);
	protected abstract List<Label> listLabels(long bookId);
	protected abstract SeriesInfo getSeriesInfo(long bookId);
	protected abstract List<UID> listUids(long bookId);
	protected abstract boolean hasVisibleBookmark(long bookId);
	protected abstract RationalNumber getProgress(long bookId);

	protected abstract Long bookIdByUid(UID uid);

	protected abstract void updateBookInfo(long bookId, long fileId, String encoding, String language, String title);
	protected abstract long insertBookInfo(ZLFile file, String encoding, String language, String title);
	protected abstract void deleteAllBookAuthors(long bookId);
	protected abstract void saveBookAuthorInfo(long bookId, long index, Author author);
	protected abstract void deleteAllBookTags(long bookId);
	protected abstract void saveBookTagInfo(long bookId, Tag tag);
	protected abstract void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo);
	protected abstract void deleteAllBookUids(long bookId);
	protected abstract void saveBookUid(long bookId, UID uid);
	protected abstract void saveBookProgress(long bookId, RationalNumber progress);

	protected FileInfo createFileInfo(long id, String name, FileInfo parent) {
		return new FileInfo(name, parent, id);
	}

	protected abstract Collection<FileInfo> loadFileInfos();
	protected abstract Collection<FileInfo> loadFileInfos(ZLFile file);
	protected abstract Collection<FileInfo> loadFileInfos(long fileId);
	protected abstract void removeFileInfo(long fileId);
	protected abstract void saveFileInfo(FileInfo fileInfo);

	protected abstract void addBookHistoryEvent(long bookId, int event);
	protected abstract void removeBookHistoryEvents(long bookId, int event);
	protected abstract List<Long> loadRecentBookIds(int event, int limit);

	protected abstract void addLabel(long bookId, Label label);
	protected abstract void removeLabel(long bookId, Label label);

	protected Bookmark createBookmark(
		long id, String uid, String versionUid,
		long bookId, String bookTitle, String text, String originalText,
		long creationTimestamp, Long modificationTimestamp, Long accessTimestamp,
		String modelId,
		int start_paragraphIndex, int start_wordIndex, int start_charIndex,
		int end_paragraphIndex, int end_wordIndex, int end_charIndex,
		boolean isVisible,
		int styleId
	) {
		return new Bookmark(
			id, uid, versionUid,
			bookId, bookTitle, text, originalText,
			creationTimestamp, modificationTimestamp, accessTimestamp,
			modelId,
			start_paragraphIndex, start_wordIndex, start_charIndex,
			end_paragraphIndex, end_wordIndex, end_charIndex,
			isVisible,
			styleId
		);
	}

	protected abstract List<Bookmark> loadBookmarks(BookmarkQuery query);
	protected abstract long saveBookmark(Bookmark bookmark);
	protected abstract void deleteBookmark(Bookmark bookmark);
	protected abstract List<String> deletedBookmarkUids();
	protected abstract void purgeBookmarks(List<String> uids);

	protected HighlightingStyle createStyle(int id, long timestamp, String name, ZLColor bgColor, ZLColor fgColor) {
		return new HighlightingStyle(id, timestamp, name, bgColor, fgColor);
	}
	protected abstract List<HighlightingStyle> loadStyles();
	protected abstract void saveStyle(HighlightingStyle style);

	protected abstract ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId);
	protected abstract void storePosition(long bookId, ZLTextPosition position);

	protected abstract Collection<String> loadVisitedHyperlinks(long bookId);
	protected abstract void addVisitedHyperlink(long bookId, String hyperlinkId);

	protected abstract String getHash(long bookId, long lastModified) throws NotAvailable;
	protected abstract void setHash(long bookId, String hash) throws NotAvailable;
	protected abstract List<Long> bookIdsByHash(String hash);

	protected abstract String getOptionValue(String name);
	protected abstract void setOptionValue(String name, String value);
}
