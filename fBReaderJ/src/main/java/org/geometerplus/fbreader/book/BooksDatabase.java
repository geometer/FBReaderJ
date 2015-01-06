/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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
	public static final class NotAvailable extends Exception {
	}

	protected Book createBook(long id, long fileId, String title, String encoding, String language) {
		final FileInfoSet infos = new FileInfoSet(this, fileId);
		return createBook(id, infos.getFile(fileId), title, encoding, language);
	}
	protected Book createBook(long id, ZLFile file, String title, String encoding, String language) {
		return file != null ? new Book(id, file, title, encoding, language) : null;
	}
	protected void addAuthor(Book book, Author author) {
		book.addAuthorWithNoCheck(author);
	}
	protected void addTag(Book book, Tag tag) {
		book.addTagWithNoCheck(tag);
	}
	protected void setSeriesInfo(Book book, String series, String index) {
		book.setSeriesInfoWithNoCheck(series, index);
	}

	protected abstract void executeAsTransaction(Runnable actions);

	// returns map fileId -> book
	protected abstract Map<Long,Book> loadBooks(FileInfoSet infos, boolean existing);
	protected abstract void setExistingFlag(Collection<Book> books, boolean flag);
	protected abstract Book loadBook(long bookId);
	protected abstract Book loadBookByFile(long fileId, ZLFile file);
	protected abstract void deleteBook(long bookId);

	protected abstract List<Author> listAuthors(long bookId);
	protected abstract List<Tag> listTags(long bookId);
	protected abstract List<String> listLabels(long bookId);
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

	protected abstract List<Long> loadRecentBookIds();
	protected abstract void saveRecentBookIds(final List<Long> ids);

	protected abstract void setLabel(long bookId, String label);
	protected abstract void removeLabel(long bookId, String label);

	protected Bookmark createBookmark(
		long id, long bookId, String bookTitle, String text,
		Date creationDate, Date modificationDate, Date accessDate, int accessCounter,
		String modelId,
		int start_paragraphIndex, int start_wordIndex, int start_charIndex,
		int end_paragraphIndex, int end_wordIndex, int end_charIndex,
		boolean isVisible,
		int styleId
	) {
		return new Bookmark(
			id, bookId, bookTitle, text,
			creationDate, modificationDate, accessDate, accessCounter,
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

	protected HighlightingStyle createStyle(int id, String name, int bgColor, int fgColor) {
		return new HighlightingStyle(
			id, name,
			bgColor != -1 ? new ZLColor(bgColor) : null,
			fgColor != -1 ? new ZLColor(fgColor) : null
		);
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
}
