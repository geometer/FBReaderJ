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

package org.geometerplus.android.fbreader.libraryService;

import java.util.*;
import java.math.BigDecimal;

import android.content.Context;
import android.database.sqlite.*;
import android.database.SQLException;
import android.database.Cursor;

import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.util.SQLiteUtil;

final class SQLiteBooksDatabase extends BooksDatabase {
	private final SQLiteDatabase myDatabase;
	private final HashMap<String,SQLiteStatement> myStatements =
		new HashMap<String,SQLiteStatement>();

	SQLiteBooksDatabase(Context context) {
		myDatabase = context.openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	@Override
	public void finalize() {
		myDatabase.close();
	}

	protected void executeAsTransaction(Runnable actions) {
		boolean transactionStarted = false;
		try {
			myDatabase.beginTransaction();
			transactionStarted = true;
		} catch (Throwable t) {
		}
		try {
			actions.run();
			if (transactionStarted) {
				myDatabase.setTransactionSuccessful();
			}
		} finally {
			if (transactionStarted) {
				myDatabase.endTransaction();
			}
		}
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentVersion = 40;
		if (version >= currentVersion) {
			return;
		}

		myDatabase.beginTransaction();

		switch (version) {
			case 0:
				createTables();
			case 1:
				updateTables1();
			case 2:
				updateTables2();
			case 3:
				updateTables3();
			case 4:
				updateTables4();
			case 5:
				updateTables5();
			case 6:
				updateTables6();
			case 7:
				updateTables7();
			case 8:
				updateTables8();
			case 9:
				updateTables9();
			case 10:
				updateTables10();
			case 11:
				updateTables11();
			case 12:
				updateTables12();
			case 13:
				updateTables13();
			case 14:
				updateTables14();
			case 15:
				updateTables15();
			case 16:
				updateTables16();
			case 17:
				updateTables17();
			case 18:
				updateTables18();
			case 19:
				updateTables19();
			case 20:
				updateTables20();
			case 21:
				updateTables21();
			case 22:
				updateTables22();
			case 23:
				updateTables23();
			case 24:
				updateTables24();
			case 25:
				updateTables25();
			case 26:
				updateTables26();
			case 27:
				updateTables27();
			case 28:
				updateTables28();
			case 29:
				updateTables29();
			case 30:
				updateTables30();
			case 31:
				updateTables31();
			case 32:
				updateTables32();
			case 33:
				updateTables33();
			case 34:
				updateTables34();
			case 35:
				updateTables35();
			case 36:
				updateTables36();
			case 37:
				updateTables37();
			case 38:
				updateTables38();
			case 39:
				updateTables39();
		}
		myDatabase.setTransactionSuccessful();
		myDatabase.setVersion(currentVersion);
		myDatabase.endTransaction();

		myDatabase.execSQL("VACUUM");
	}

	@Override
	protected String getOptionValue(String name) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT value FROM Options WHERE name=?", new String[] { name }
		);
		try {
			return cursor.moveToNext() ? cursor.getString(0) : null;
		} finally {
			cursor.close();
		}
	}

	@Override
	protected void setOptionValue(String name, String value) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO Options (name,value) VALUES (?,?)"
		);
		synchronized (statement) {
			SQLiteUtil.bindString(statement, 1, name);
			SQLiteUtil.bindString(statement, 2, value);
			statement.execute();
		}
	}

	@Override
	protected DbBook loadBook(long bookId) {
		DbBook book = null;
		final Cursor cursor = myDatabase.rawQuery("SELECT file_id,title,encoding,language FROM Books WHERE book_id = " + bookId, null);
		if (cursor.moveToNext()) {
			book = createBook(
				bookId, cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)
			);
		}
		cursor.close();
		return book;
	}

	@Override
	protected DbBook loadBookByFile(long fileId, ZLFile file) {
		if (fileId == -1) {
			return null;
		}
		DbBook book = null;
		final Cursor cursor = myDatabase.rawQuery("SELECT book_id,title,encoding,language FROM Books WHERE file_id = " + fileId, null);
		if (cursor.moveToNext()) {
			book = createBook(
				cursor.getLong(0), file, cursor.getString(1), cursor.getString(2), cursor.getString(3)
			);
		}
		cursor.close();
		return book;
	}

	private boolean myTagCacheIsInitialized;
	private final HashMap<Tag,Long> myIdByTag = new HashMap<Tag,Long>();
	private final HashMap<Long,Tag> myTagById = new HashMap<Long,Tag>();

	private void initTagCache() {
		if (myTagCacheIsInitialized) {
			return;
		}
		myTagCacheIsInitialized = true;

		Cursor cursor = myDatabase.rawQuery("SELECT tag_id,parent_id,name FROM Tags ORDER BY tag_id", null);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(0);
			if (myTagById.get(id) == null) {
				final Tag tag = Tag.getTag(myTagById.get(cursor.getLong(1)), cursor.getString(2));
				myIdByTag.put(tag, id);
				myTagById.put(id, tag);
			}
		}
		cursor.close();
	}

	@Override
	protected Map<Long,DbBook> loadBooks(FileInfoSet infos, boolean existing) {
		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_id,title,encoding,language FROM Books WHERE `exists` = " + (existing ? 1 : 0), null
		);
		final HashMap<Long,DbBook> booksById = new HashMap<Long,DbBook>();
		final HashMap<Long,DbBook> booksByFileId = new HashMap<Long,DbBook>();
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final long fileId = cursor.getLong(1);
			final DbBook book = createBook(
				id, infos.getFile(fileId), cursor.getString(2), cursor.getString(3), cursor.getString(4)
			);
			if (book != null) {
				booksById.put(id, book);
				booksByFileId.put(fileId, book);
			}
		}
		cursor.close();

		initTagCache();

		cursor = myDatabase.rawQuery(
			"SELECT author_id,name,sort_key FROM Authors", null
		);
		final HashMap<Long,Author> authorById = new HashMap<Long,Author>();
		while (cursor.moveToNext()) {
			authorById.put(cursor.getLong(0), new Author(cursor.getString(1), cursor.getString(2)));
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,author_id FROM BookAuthor ORDER BY author_index", null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				Author author = authorById.get(cursor.getLong(1));
				if (author != null) {
					addAuthor(book, author);
				}
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery("SELECT book_id,tag_id FROM BookTag", null);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				addTag(book, getTagById(cursor.getLong(1)));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT series_id,name FROM Series", null
		);
		final HashMap<Long,String> seriesById = new HashMap<Long,String>();
		while (cursor.moveToNext()) {
			seriesById.put(cursor.getLong(0), cursor.getString(1));
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,series_id,book_index FROM BookSeries", null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				final String series = seriesById.get(cursor.getLong(1));
				if (series != null) {
					setSeriesInfo(book, series, cursor.getString(2));
				}
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,type,uid FROM BookUid", null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				book.addUid(cursor.getString(1), cursor.getString(2));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT BookLabel.book_id,Labels.name,BookLabel.uid FROM Labels" +
			" INNER JOIN BookLabel ON BookLabel.label_id=Labels.label_id",
			null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				book.addLabel(new Label(cursor.getString(2), cursor.getString(1)));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,numerator,denominator FROM BookReadingProgress",
			null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				book.setProgress(RationalNumber.create(cursor.getLong(1), cursor.getLong(2)));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id FROM Bookmarks WHERE visible = 1 GROUP by book_id",
			null
		);
		while (cursor.moveToNext()) {
			final DbBook book = booksById.get(cursor.getLong(0));
			if (book != null) {
				book.HasBookmark = true;
			}
		}
		cursor.close();

		return booksByFileId;
	}

	@Override
	protected void setExistingFlag(Collection<DbBook> books, boolean flag) {
		if (books.isEmpty()) {
			return;
		}
		final StringBuilder bookSet = new StringBuilder("(");
		boolean first = true;
		for (DbBook b : books) {
			if (first) {
				first = false;
			} else {
				bookSet.append(",");
			}
			bookSet.append(b.getId());
		}
		bookSet.append(")");
		myDatabase.execSQL(
			"UPDATE Books SET `exists` = " + (flag ? 1 : 0) + " WHERE book_id IN " + bookSet
		);
	}

	@Override
	protected void updateBookInfo(long bookId, long fileId, String encoding, String language, String title) {
		final SQLiteStatement statement = get(
			"UPDATE OR IGNORE Books SET file_id=?, encoding=?, language=?, title=? WHERE book_id=?"
		);
		synchronized (statement) {
			statement.bindLong(1, fileId);
			SQLiteUtil.bindString(statement, 2, encoding);
			SQLiteUtil.bindString(statement, 3, language);
			statement.bindString(4, title);
			statement.bindLong(5, bookId);
			statement.execute();
		}
	}

	@Override
	protected long insertBookInfo(ZLFile file, String encoding, String language, String title) {
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO Books (encoding,language,title,file_id) VALUES (?,?,?,?)"
		);
		synchronized (statement) {
			SQLiteUtil.bindString(statement, 1, encoding);
			SQLiteUtil.bindString(statement, 2, language);
			statement.bindString(3, title);
			final FileInfoSet infoSet = new FileInfoSet(this, file);
			statement.bindLong(4, infoSet.getId(file));
			return statement.executeInsert();
		}
	}

	protected void deleteAllBookAuthors(long bookId) {
		final SQLiteStatement statement = get("DELETE FROM BookAuthor WHERE book_id=?");
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.execute();
		}
	}

	protected void saveBookAuthorInfo(long bookId, long index, Author author) {
		final SQLiteStatement getAuthorIdStatement = get(
			"SELECT author_id FROM Authors WHERE name=? AND sort_key=?"
		);
		final SQLiteStatement insertAuthorStatement = get(
			"INSERT OR IGNORE INTO Authors (name,sort_key) VALUES (?,?)"
		);
		final SQLiteStatement insertBookAuthorStatement = get(
			"INSERT OR REPLACE INTO BookAuthor (book_id,author_id,author_index) VALUES (?,?,?)"
		);

		long authorId;
		try {
			getAuthorIdStatement.bindString(1, author.DisplayName);
			getAuthorIdStatement.bindString(2, author.SortKey);
			authorId = getAuthorIdStatement.simpleQueryForLong();
		} catch (SQLException e) {
			insertAuthorStatement.bindString(1, author.DisplayName);
			insertAuthorStatement.bindString(2, author.SortKey);
			authorId = insertAuthorStatement.executeInsert();
		}
		insertBookAuthorStatement.bindLong(1, bookId);
		insertBookAuthorStatement.bindLong(2, authorId);
		insertBookAuthorStatement.bindLong(3, index);
		insertBookAuthorStatement.execute();
	}

	protected List<Author> listAuthors(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Authors.name,Authors.sort_key FROM BookAuthor INNER JOIN Authors ON Authors.author_id = BookAuthor.author_id WHERE BookAuthor.book_id = ?", new String[] { String.valueOf(bookId) });
		if (!cursor.moveToNext()) {
			cursor.close();
			return null;
		}
		final ArrayList<Author> list = new ArrayList<Author>();
		do {
			list.add(new Author(cursor.getString(0), cursor.getString(1)));
		} while (cursor.moveToNext());
		cursor.close();
		return list;
	}

	private long getTagId(Tag tag) {
		final SQLiteStatement getTagIdStatement = get(
			"SELECT tag_id FROM Tags WHERE parent_id=? AND name=?"
		);
		{
			final Long id = myIdByTag.get(tag);
			if (id != null) {
				return id;
			}
		}
		if (tag.Parent != null) {
			getTagIdStatement.bindLong(1, getTagId(tag.Parent));
		} else {
			getTagIdStatement.bindNull(1);
		}
		getTagIdStatement.bindString(2, tag.Name);
		long id;
		try {
			id = getTagIdStatement.simpleQueryForLong();
		} catch (SQLException e) {
			final SQLiteStatement createTagIdStatement = get(
				"INSERT OR IGNORE INTO Tags (parent_id,name) VALUES (?,?)"
			);
			if (tag.Parent != null) {
				createTagIdStatement.bindLong(1, getTagId(tag.Parent));
			} else {
				createTagIdStatement.bindNull(1);
			}
			createTagIdStatement.bindString(2, tag.Name);
			id = createTagIdStatement.executeInsert();
		}
		myIdByTag.put(tag, id);
		myTagById.put(id, tag);
		return id;
	}

	protected void deleteAllBookTags(long bookId) {
		final SQLiteStatement statement = get("DELETE FROM BookTag WHERE book_id=?");
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.execute();
		}
	}

	protected void saveBookTagInfo(long bookId, Tag tag) {
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO BookTag (book_id,tag_id) VALUES (?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, getTagId(tag));
			statement.execute();
		}
	}

	private Tag getTagById(long id) {
		Tag tag = myTagById.get(id);
		if (tag == null) {
			final Cursor cursor = myDatabase.rawQuery("SELECT parent_id,name FROM Tags WHERE tag_id = ?", new String[] { String.valueOf(id) });
			if (cursor.moveToNext()) {
				final Tag parent = cursor.isNull(0) ? null : getTagById(cursor.getLong(0));
				tag = Tag.getTag(parent, cursor.getString(1));
				myIdByTag.put(tag, id);
				myTagById.put(id, tag);
			}
			cursor.close();
		}
		return tag;
	}

	protected List<Tag> listTags(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Tags.tag_id FROM BookTag INNER JOIN Tags ON Tags.tag_id = BookTag.tag_id WHERE BookTag.book_id = ?", new String[] { String.valueOf(bookId) });
		if (!cursor.moveToNext()) {
			cursor.close();
			return null;
		}
		final ArrayList<Tag> list = new ArrayList<Tag>();
		do {
			list.add(getTagById(cursor.getLong(0)));
		} while (cursor.moveToNext());
		cursor.close();
		return list;
	}

	@Override
	protected List<Label> listLabels(long bookId) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT Labels.name,BookLabel.uid FROM Labels" +
			" INNER JOIN BookLabel ON BookLabel.label_id=Labels.label_id" +
			" WHERE BookLabel.book_id=?",
			new String[] { String.valueOf(bookId) }
		);
		final LinkedList<Label> labels = new LinkedList<Label>();
		while (cursor.moveToNext()) {
			labels.add(new Label(cursor.getString(1), cursor.getString(0)));
		}
		cursor.close();
		return labels;
	}

	@Override
	protected List<String> listLabels() {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT DISTINCT(Labels.name) FROM Labels" +
			" INNER JOIN BookLabel ON BookLabel.label_id=Labels.label_id" +
			" INNER JOIN Books ON BookLabel.book_id=Books.book_id" +
			" WHERE Books.`exists`=1",
			null
		);
		final LinkedList<String> names = new LinkedList<String>();
		while (cursor.moveToNext()) {
			names.add(cursor.getString(0));
		}
		cursor.close();
		return names;
	}

	protected void deleteAllBookUids(long bookId) {
		final SQLiteStatement statement = get("DELETE FROM BookUid WHERE book_id=?");
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.execute();
		}
	}

	@Override
	protected void saveBookUid(long bookId, UID uid) {
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO BookUid (book_id,type,uid) VALUES (?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindString(2, uid.Type);
			statement.bindString(3, uid.Id);
			statement.execute();
		}
	}

	@Override
	protected List<UID> listUids(long bookId) {
		final ArrayList<UID> list = new ArrayList<UID>();
		final Cursor cursor = myDatabase.rawQuery("SELECT type,uid FROM BookUid WHERE book_id = ?", new String[] { String.valueOf(bookId) });
		while (cursor.moveToNext()) {
			list.add(new UID(cursor.getString(0), cursor.getString(1)));
		}
		cursor.close();
		return list;
	}

	@Override
	protected Long bookIdByUid(UID uid) {
		Long bookId = null;
		final Cursor cursor = myDatabase.rawQuery("SELECT book_id FROM BookUid WHERE type = ? AND uid = ? LIMIT 1", new String[] { uid.Type, uid.Id });
		if (cursor.moveToNext()) {
			bookId = cursor.getLong(0);
		}
		cursor.close();
		return bookId;
	}

	protected void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo) {
		if (seriesInfo == null) {
			final SQLiteStatement statement = get("DELETE FROM BookSeries WHERE book_id=?");
			synchronized (statement) {
				statement.bindLong(1, bookId);
				statement.execute();
			}
		} else {
			long seriesId;
			try {
				final SQLiteStatement getSeriesIdStatement = get(
					"SELECT series_id FROM Series WHERE name = ?"
				);
				synchronized (getSeriesIdStatement) {
					getSeriesIdStatement.bindString(1, seriesInfo.Series.getTitle());
					seriesId = getSeriesIdStatement.simpleQueryForLong();
				}
			} catch (SQLException e) {
				final SQLiteStatement insertSeriesStatement = get(
					"INSERT OR IGNORE INTO Series (name) VALUES (?)"
				);
				synchronized (insertSeriesStatement) {
					insertSeriesStatement.bindString(1, seriesInfo.Series.getTitle());
					seriesId = insertSeriesStatement.executeInsert();
				}
			}
			final SQLiteStatement insertBookSeriesStatement = get(
				"INSERT OR REPLACE INTO BookSeries (book_id,series_id,book_index) VALUES (?,?,?)"
			);
			synchronized (insertBookSeriesStatement) {
				insertBookSeriesStatement.bindLong(1, bookId);
				insertBookSeriesStatement.bindLong(2, seriesId);
				SQLiteUtil.bindString(
					insertBookSeriesStatement, 3,
					seriesInfo.Index != null ? seriesInfo.Index.toPlainString() : null
				);
				insertBookSeriesStatement.execute();
			}
		}
	}

	protected SeriesInfo getSeriesInfo(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Series.name,BookSeries.book_index FROM BookSeries INNER JOIN Series ON Series.series_id = BookSeries.series_id WHERE BookSeries.book_id = ?", new String[] { String.valueOf(bookId) });
		SeriesInfo info = null;
		if (cursor.moveToNext()) {
			info = SeriesInfo.createSeriesInfo(cursor.getString(0), cursor.getString(1));
		}
		cursor.close();
		return info;
	}

	protected void removeFileInfo(long fileId) {
		if (fileId == -1) {
			return;
		}
		final SQLiteStatement statement = get("DELETE FROM Files WHERE file_id=?");
		synchronized (statement) {
			statement.bindLong(1, fileId);
			statement.execute();
		}
	}

	protected void saveFileInfo(FileInfo fileInfo) {
		final long id = fileInfo.Id;
		SQLiteStatement statement;
		if (id == -1) {
			statement = get(
				"INSERT OR IGNORE INTO Files (name,parent_id,size) VALUES (?,?,?)"
			);
		} else {
			statement = get(
				"UPDATE Files SET name=?, parent_id=?, size=? WHERE file_id=?"
			);
		}
		synchronized (statement) {
			statement.bindString(1, fileInfo.Name);
			final FileInfo parent = fileInfo.Parent;
			if (parent != null) {
				statement.bindLong(2, parent.Id);
			} else {
				statement.bindNull(2);
			}
			final long size = fileInfo.FileSize;
			if (size != -1) {
				statement.bindLong(3, size);
			} else {
				statement.bindNull(3);
			}
			if (id == -1) {
				fileInfo.Id = statement.executeInsert();
			} else {
				statement.bindLong(4, id);
				statement.execute();
			}
		}
	}

	protected Collection<FileInfo> loadFileInfos() {
		Cursor cursor = myDatabase.rawQuery(
			"SELECT file_id,name,parent_id,size FROM Files", null
		);
		HashMap<Long,FileInfo> infosById = new HashMap<Long,FileInfo>();
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final FileInfo info = createFileInfo(id,
				cursor.getString(1),
				cursor.isNull(2) ? null : infosById.get(cursor.getLong(2))
			);
			if (!cursor.isNull(3)) {
				info.FileSize = cursor.getLong(3);
			}
			infosById.put(id, info);
		}
		cursor.close();
		return infosById.values();
	}

	protected Collection<FileInfo> loadFileInfos(ZLFile file) {
		final LinkedList<ZLFile> fileStack = new LinkedList<ZLFile>();
		for (; file != null; file = file.getParent()) {
			fileStack.addFirst(file);
		}

		final ArrayList<FileInfo> infos = new ArrayList<FileInfo>(fileStack.size());
		final String[] parameters = { null };
		FileInfo current = null;
		for (ZLFile f : fileStack) {
			parameters[0] = f.getLongName();
			final Cursor cursor = myDatabase.rawQuery(
				(current == null) ?
					"SELECT file_id,size FROM Files WHERE name = ?" :
					"SELECT file_id,size FROM Files WHERE parent_id = " + current.Id + " AND name = ?",
				parameters
			);
			if (cursor.moveToNext()) {
				current = createFileInfo(cursor.getLong(0), parameters[0], current);
				if (!cursor.isNull(1)) {
					current.FileSize = cursor.getLong(1);
				}
				infos.add(current);
				cursor.close();
			} else {
				cursor.close();
				break;
			}
		}

		return infos;
	}

	protected Collection<FileInfo> loadFileInfos(long fileId) {
		final ArrayList<FileInfo> infos = new ArrayList<FileInfo>();
		while (fileId != -1) {
			final Cursor cursor = myDatabase.rawQuery(
				"SELECT name,size,parent_id FROM Files WHERE file_id = " + fileId, null
			);
			if (cursor.moveToNext()) {
				FileInfo info = createFileInfo(fileId, cursor.getString(0), null);
				if (!cursor.isNull(1)) {
					info.FileSize = cursor.getLong(1);
				}
				infos.add(0, info);
				fileId = cursor.isNull(2) ? -1 : cursor.getLong(2);
			} else {
				fileId = -1;
			}
			cursor.close();
		}
		for (int i = 1; i < infos.size(); ++i) {
			final FileInfo oldInfo = infos.get(i);
			final FileInfo newInfo = createFileInfo(oldInfo.Id, oldInfo.Name, infos.get(i - 1));
			newInfo.FileSize = oldInfo.FileSize;
			infos.set(i, newInfo);
		}
		return infos;
	}

	@Override
	protected void addBookHistoryEvent(long bookId, int event) {
		final SQLiteStatement statement = get(
			"INSERT INTO BookHistory (book_id,timestamp,event) VALUES (?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, System.currentTimeMillis());
			statement.bindLong(3, event);
			statement.executeInsert();
		}
	}

	@Override
	protected void removeBookHistoryEvents(long bookId, int event) {
		final SQLiteStatement statement = get(
			"DELETE FROM BookHistory WHERE book_id=? and event=?"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, event);
			statement.executeInsert();
		}
	}

	@Override
	protected List<Long> loadRecentBookIds(int event, int limit) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id FROM BookHistory WHERE event=? GROUP BY book_id ORDER BY timestamp DESC LIMIT ?",
			new String[] { String.valueOf(event), String.valueOf(limit) }
		);
		final LinkedList<Long> ids = new LinkedList<Long>();
		while (cursor.moveToNext()) {
			ids.add(cursor.getLong(0));
		}
		cursor.close();
		return ids;
	}

	@Override
	protected void addLabel(long bookId, Label label) {
		myDatabase.execSQL("INSERT OR IGNORE INTO Labels (name) VALUES (?)", new Object[] { label.Name });
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO BookLabel(label_id,book_id,uid,timestamp)" +
			" SELECT label_id,?,?,? FROM Labels WHERE name=?"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindString(2, label.Uid);
			statement.bindLong(3, System.currentTimeMillis());
			statement.bindString(4, label.Name);
			statement.execute();
		}
	}

	@Override
	protected void removeLabel(long bookId, Label label) {
		final int count = myDatabase.delete(
			"BookLabel",
			"book_id=? AND uid=?",
			new String[] { String.valueOf(bookId), label.Uid }
		);

		if (count > 0) {
			final SQLiteStatement statement = get(
				"INSERT OR IGNORE INTO DeletedBookLabelIds (uid) VALUES (?)"
			);
			synchronized (statement) {
				statement.bindString(1, label.Uid);
				statement.execute();
			}
		}
	}

	@Override
	protected boolean hasVisibleBookmark(long bookId) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT bookmark_id FROM Bookmarks WHERE book_id = " + bookId +
			" AND visible = 1 LIMIT 1", null
		);
		final boolean result = cursor.moveToNext();
		cursor.close();
		return result;
	}

	@Override
	protected List<Bookmark> loadBookmarks(BookmarkQuery query) {
		final LinkedList<Bookmark> list = new LinkedList<Bookmark>();
		final StringBuilder sql = new StringBuilder("SELECT")
			.append(" bm.bookmark_id,bm.uid,bm.version_uid,")
			.append("bm.book_id,b.title,bm.bookmark_text,bm.original_text,")
			.append("bm.creation_time,bm.modification_time,bm.access_time,")
			.append("bm.model_id,bm.paragraph,bm.word,bm.char,")
			.append("bm.end_paragraph,bm.end_word,bm.end_character,")
			.append("bm.style_id")
			.append(" FROM Bookmarks AS bm")
			.append(" INNER JOIN Books AS b ON b.book_id = bm.book_id")
			.append(" WHERE");
		if (query.Book != null) {
			sql.append(" b.book_id = " + query.Book.getId() +" AND");
		}
		sql
			.append(" bm.visible = " + (query.Visible ? 1 : 0))
			.append(" ORDER BY bm.bookmark_id")
			.append(" LIMIT " + query.Limit * query.Page + "," + query.Limit);
		Cursor cursor = myDatabase.rawQuery(sql.toString(), null);
		while (cursor.moveToNext()) {
			list.add(createBookmark(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getLong(3),
				cursor.getString(4),
				cursor.getString(5),
				cursor.isNull(6) ? null : cursor.getString(6),
				cursor.getLong(7),
				cursor.isNull(8) ? null : cursor.getLong(8),
				cursor.isNull(9) ? null : cursor.getLong(9),
				cursor.getString(10),
				(int)cursor.getLong(11),
				(int)cursor.getLong(12),
				(int)cursor.getLong(13),
				(int)cursor.getLong(14),
				cursor.isNull(15) ? -1 : (int)cursor.getLong(15),
				cursor.isNull(16) ? -1 : (int)cursor.getLong(16),
				query.Visible,
				(int)cursor.getLong(17)
			));
		}
		cursor.close();
		return list;
	}

	@Override
	protected List<HighlightingStyle> loadStyles() {
		final LinkedList<HighlightingStyle> list = new LinkedList<HighlightingStyle>();
		final String sql = "SELECT style_id,timestamp,name,bg_color,fg_color FROM HighlightingStyle";
		final Cursor cursor = myDatabase.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			final String name = cursor.getString(2);
			final int bgColor = (int)cursor.getLong(3);
			final int fgColor = (int)cursor.getLong(4);
			list.add(createStyle(
				(int)cursor.getLong(0),
				cursor.getLong(1),
				name.length() > 0 ? name : null,
				bgColor != -1 ? new ZLColor(bgColor) : null,
				fgColor != -1 ? new ZLColor(fgColor) : null
			));
		}
		cursor.close();
		return list;
	}

	protected void saveStyle(HighlightingStyle style) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO HighlightingStyle (style_id,name,bg_color,fg_color,timestamp) VALUES (?,?,?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, style.Id);
			final String name = style.getNameOrNull();
			statement.bindString(2, name != null ? name : "");
			final ZLColor bgColor = style.getBackgroundColor();
			statement.bindLong(3, bgColor != null ? bgColor.intValue() : -1);
			final ZLColor fgColor = style.getForegroundColor();
			statement.bindLong(4, fgColor != null ? fgColor.intValue() : -1);
			statement.bindLong(5, System.currentTimeMillis());
			statement.execute();
		}
	}

	// this is workaround for working with old format plugins;
	// it should never go via the third way with new versions
	private String uid(Bookmark bookmark) {
		if (bookmark.Uid != null) {
			return bookmark.Uid;
		}
		if (bookmark.getId() == -1) {
			return UUID.randomUUID().toString();
		}

		final Cursor cursor = myDatabase.rawQuery(
			"SELECT uid FROM Bookmarks WHERE bookmark_id = " + bookmark.getId(), null
		);
		try {
			if (cursor.moveToNext()) {
				return cursor.getString(0);
			}
		} finally {
			cursor.close();
		}

		return UUID.randomUUID().toString();
	}

	@Override
	protected long saveBookmark(Bookmark bookmark) {
		final SQLiteStatement statement;
		final long bookmarkId = bookmark.getId();

		if (bookmarkId == -1) {
			statement = get(
				"INSERT INTO Bookmarks (uid,version_uid,book_id,bookmark_text,original_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character,visible,style_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
		} else {
			statement = get(
				"UPDATE Bookmarks SET uid=?,version_uid=?,book_id=?,bookmark_text=?,original_text=?,creation_time=?,modification_time=?,access_time=?,model_id=?,paragraph=?,word=?,char=?,end_paragraph=?,end_word=?,end_character=?,visible=?,style_id=? WHERE bookmark_id=?"
			);
		}

		synchronized (statement) {
			int fieldCount = 0;
			SQLiteUtil.bindString(statement, ++fieldCount, uid(bookmark));
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.getVersionUid());
			statement.bindLong(++fieldCount, bookmark.BookId);
			statement.bindString(++fieldCount, bookmark.getText());
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.getOriginalText());
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Creation));
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Modification));
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Access));
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.ModelId);
			statement.bindLong(++fieldCount, bookmark.ParagraphIndex);
			statement.bindLong(++fieldCount, bookmark.ElementIndex);
			statement.bindLong(++fieldCount, bookmark.CharIndex);
			final ZLTextPosition end = bookmark.getEnd();
			if (end != null) {
				statement.bindLong(++fieldCount, end.getParagraphIndex());
				statement.bindLong(++fieldCount, end.getElementIndex());
				statement.bindLong(++fieldCount, end.getCharIndex());
			} else {
				statement.bindLong(++fieldCount, bookmark.getLength());
				statement.bindNull(++fieldCount);
				statement.bindNull(++fieldCount);
			}
			statement.bindLong(++fieldCount, bookmark.IsVisible ? 1 : 0);
			statement.bindLong(++fieldCount, bookmark.getStyleId());

			if (bookmarkId == -1) {
				return statement.executeInsert();
			} else {
				statement.bindLong(++fieldCount, bookmarkId);
				statement.execute();
				return bookmarkId;
			}
		}
	}

	@Override
	protected void deleteBookmark(Bookmark bookmark) {
		final String uuid = uid(bookmark);
		SQLiteStatement statement = get("DELETE FROM Bookmarks WHERE uid=?");
		synchronized (statement) {
			statement.bindString(1, uuid);
			statement.execute();
		}
		statement = get("INSERT OR IGNORE INTO DeletedBookmarkIds (uid) VALUES (?)");
		synchronized (statement) {
			statement.bindString(1, uuid);
			statement.execute();
		}
	}

	@Override
	protected List<String> deletedBookmarkUids() {
		final Cursor cursor = myDatabase.rawQuery("SELECT uid FROM DeletedBookmarkIds", null);
		final LinkedList<String> uids = new LinkedList<String>();
		while (cursor.moveToNext()) {
			uids.add(cursor.getString(0));
		}
		cursor.close();
		return uids;
	}

	@Override
	protected void purgeBookmarks(List<String> uids) {
		final SQLiteStatement statement = get("DELETE FROM DeletedBookmarkIds WHERE uid=?");
		synchronized (statement) {
			for (String u : uids) {
				statement.bindString(1, u);
				statement.execute();
			}
		}
	}

	protected ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		ZLTextFixedPosition.WithTimestamp position = null;
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT paragraph,word,char,timestamp FROM BookState WHERE book_id = " + bookId, null
		);
		if (cursor.moveToNext()) {
			position = new ZLTextFixedPosition.WithTimestamp(
				(int)cursor.getLong(0),
				(int)cursor.getLong(1),
				(int)cursor.getLong(2),
				cursor.getLong(3)
			);
		}
		cursor.close();
		return position;
	}

	protected void storePosition(long bookId, ZLTextPosition position) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO BookState (book_id,paragraph,word,char,timestamp) VALUES (?,?,?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, position.getParagraphIndex());
			statement.bindLong(3, position.getElementIndex());
			statement.bindLong(4, position.getCharIndex());

			long timestamp = -1;
			if (position instanceof ZLTextFixedPosition.WithTimestamp) {
				timestamp = ((ZLTextFixedPosition.WithTimestamp)position).Timestamp;
			}
			if (timestamp == -1) {
				timestamp = System.currentTimeMillis();
			}
			statement.bindLong(5, timestamp);

			statement.execute();
		}
	}

	private void deleteVisitedHyperlinks(long bookId) {
		final SQLiteStatement statement = get("DELETE FROM VisitedHyperlinks WHERE book_id=?");
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.execute();
		}
	}

	protected void addVisitedHyperlink(long bookId, String hyperlinkId) {
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO VisitedHyperlinks(book_id,hyperlink_id) VALUES (?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindString(2, hyperlinkId);
			statement.execute();
		}
	}

	protected Collection<String> loadVisitedHyperlinks(long bookId) {
		final TreeSet<String> links = new TreeSet<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT hyperlink_id FROM VisitedHyperlinks WHERE book_id = ?", new String[] { String.valueOf(bookId) });
		while (cursor.moveToNext()) {
			links.add(cursor.getString(0));
		}
		cursor.close();
		return links;
	}

	@Override
	protected void saveBookProgress(long bookId, RationalNumber progress) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO BookReadingProgress (book_id,numerator,denominator) VALUES (?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, progress.Numerator);
			statement.bindLong(3, progress.Denominator);
			statement.execute();
		}
	}

	@Override
	protected RationalNumber getProgress(long bookId) {
		final RationalNumber progress;
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT numerator,denominator FROM BookReadingProgress WHERE book_id=" + bookId, null
		);
		if (cursor.moveToNext()) {
			progress = RationalNumber.create(cursor.getLong(0), cursor.getLong(1));
		} else {
			progress = null;
		}
		cursor.close();
		return progress;
	}

	@Override
	protected String getHash(long bookId, long lastModified) throws NotAvailable {
		try {
			final SQLiteStatement statement = get(
				"SELECT hash FROM BookHash WHERE book_id=? AND timestamp>?"
			);
			synchronized (statement) {
				statement.bindLong(1, bookId);
				statement.bindLong(2, lastModified);
				try {
					return statement.simpleQueryForString();
				} catch (SQLiteDoneException e) {
					return null;
				}
			}
		} catch (Throwable t) {
			throw new NotAvailable();
		}
	}

	@Override
	protected void setHash(long bookId, String hash) throws NotAvailable {
		try {
			final SQLiteStatement statement = get(
				"INSERT OR REPLACE INTO BookHash (book_id,timestamp,hash) VALUES (?,?,?)"
			);
			synchronized (statement) {
				statement.bindLong(1, bookId);
				statement.bindLong(2, System.currentTimeMillis());
				statement.bindString(3, hash);
				statement.execute();
			}
		} catch (Throwable t) {
			throw new NotAvailable();
		}
	}

	@Override
	protected List<Long> bookIdsByHash(String hash) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id FROM BookHash WHERE hash=?", new String[] { hash }
		);
		final List<Long> bookIds = new LinkedList<Long>();
		while (cursor.moveToNext()) {
			bookIds.add(cursor.getLong(0));
		}
		cursor.close();
		return bookIds;
	}

	@Override
	protected void deleteBook(long bookId) {
		myDatabase.beginTransaction();
		myDatabase.execSQL("DELETE FROM BookHistory WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookHash WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookAuthor WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookLabel WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookReadingProgress WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookSeries WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookState WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookTag WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM BookUid WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM Bookmarks WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM VisitedHyperlinks WHERE book_id=" + bookId);
		myDatabase.execSQL("DELETE FROM Books WHERE book_id=" + bookId);
		myDatabase.setTransactionSuccessful();
		myDatabase.endTransaction();
	}

	private void createTables() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_name TEXT UNIQUE NOT NULL)");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Authors(" +
				"author_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"sort_key TEXT NOT NULL," +
				"CONSTRAINT Authors_Unique UNIQUE (name, sort_key))");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookAuthor(" +
				"author_id INTEGER NOT NULL REFERENCES Authors(author_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"author_index INTEGER NOT NULL," +
				"CONSTRAINT BookAuthor_Unique0 UNIQUE (author_id, book_id)," +
				"CONSTRAINT BookAuthor_Unique1 UNIQUE (book_id, author_index))");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Series(" +
				"series_id INTEGER PRIMARY KEY," +
				"name TEXT UNIQUE NOT NULL)");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookSeries(" +
				"series_id INTEGER NOT NULL REFERENCES Series(series_id)," +
				"book_id INTEGER NOT NULL UNIQUE REFERENCES Books(book_id)," +
				"book_index INTEGER)");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Tags(" +
				"tag_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent INTEGER REFERENCES Tags(tag_id)," +
				"CONSTRAINT Tags_Unique UNIQUE (name, parent))");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookTag(" +
				"tag_id INTEGER REFERENCES Tags(tag_id)," +
				"book_id INTEGER REFERENCES Books(book_id)," +
				"CONSTRAINT BookTag_Unique UNIQUE (tag_id, book_id))");
	}

	private void updateTables1() {
		myDatabase.execSQL("ALTER TABLE Tags RENAME TO Tags_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Tags(" +
				"tag_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent_id INTEGER REFERENCES Tags(tag_id)," +
				"CONSTRAINT Tags_Unique UNIQUE (name, parent_id))");
		myDatabase.execSQL("INSERT INTO Tags (tag_id,name,parent_id) SELECT tag_id,name,parent FROM Tags_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Tags_Obsolete");

		myDatabase.execSQL("ALTER TABLE BookTag RENAME TO BookTag_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookTag(" +
				"tag_id INTEGER NOT NULL REFERENCES Tags(tag_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"CONSTRAINT BookTag_Unique UNIQUE (tag_id, book_id))");
		myDatabase.execSQL("INSERT INTO BookTag (tag_id,book_id) SELECT tag_id,book_id FROM BookTag_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS BookTag_Obsolete");
	}

	private void updateTables2() {
		myDatabase.execSQL("CREATE INDEX BookAuthor_BookIndex ON BookAuthor (book_id)");
		myDatabase.execSQL("CREATE INDEX BookTag_BookIndex ON BookTag (book_id)");
		myDatabase.execSQL("CREATE INDEX BookSeries_BookIndex ON BookSeries (book_id)");
	}

	private void updateTables3() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Files(" +
				"file_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent_id INTEGER REFERENCES Files(file_id)," +
				"size INTEGER," +
				"CONSTRAINT Files_Unique UNIQUE (name, parent_id))");
	}

	private void updateTables4() {
		final FileInfoSet fileInfos = new FileInfoSet(this);
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT file_name FROM Books", null
		);
		while (cursor.moveToNext()) {
			fileInfos.check(ZLFile.createFileByPath(cursor.getString(0)).getPhysicalFile(), false);
		}
		cursor.close();
		fileInfos.save();

		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS RecentBooks(" +
				"book_index INTEGER PRIMARY KEY," +
				"book_id INTEGER REFERENCES Books(book_id))");
	}

	private void updateTables5() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"bookmark_text TEXT NOT NULL," +
				"creation_time INTEGER NOT NULL," +
				"modification_time INTEGER," +
				"access_time INTEGER," +
				"access_counter INTEGER NOT NULL," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL)");

		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookState(" +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books(book_id)," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL)");
		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name FROM Books", null
		);
		final SQLiteStatement statement = myDatabase.compileStatement("INSERT INTO BookState (book_id,paragraph,word,char) VALUES (?,?,?,?)");
		while (cursor.moveToNext()) {
			final long bookId = cursor.getLong(0);
			final String fileName = cursor.getString(1);
			final int position = new ZLIntegerOption(fileName, "PositionInBuffer", 0).getValue();
			final int paragraph = new ZLIntegerOption(fileName, "Paragraph_" + position, 0).getValue();
			final int word = new ZLIntegerOption(fileName, "Word_" + position, 0).getValue();
			final int chr = new ZLIntegerOption(fileName, "Char_" + position, 0).getValue();
			if ((paragraph != 0) || (word != 0) || (chr != 0)) {
				statement.bindLong(1, bookId);
				statement.bindLong(2, paragraph);
				statement.bindLong(3, word);
				statement.bindLong(4, chr);
				statement.execute();
			}
			Config.Instance().removeGroup(fileName);
		}
		cursor.close();
	}

	private void updateTables6() {
		myDatabase.execSQL(
			"ALTER TABLE Bookmarks ADD COLUMN model_id TEXT"
		);

		myDatabase.execSQL(
			"ALTER TABLE Books ADD COLUMN file_id INTEGER"
		);

		myDatabase.execSQL("DELETE FROM Files");
		final FileInfoSet infoSet = new FileInfoSet(this);
		Cursor cursor = myDatabase.rawQuery(
			"SELECT file_name FROM Books", null
		);
		while (cursor.moveToNext()) {
			infoSet.check(ZLFile.createFileByPath(cursor.getString(0)).getPhysicalFile(), false);
		}
		cursor.close();
		infoSet.save();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name FROM Books", null
		);
		final SQLiteStatement deleteStatement = myDatabase.compileStatement("DELETE FROM Books WHERE book_id=?");
		final SQLiteStatement updateStatement = myDatabase.compileStatement("UPDATE OR IGNORE Books SET file_id=? WHERE book_id=?");
		while (cursor.moveToNext()) {
			final long bookId = cursor.getLong(0);
			final long fileId = infoSet.getId(ZLFile.createFileByPath(cursor.getString(1)));

			if (fileId == -1) {
				deleteStatement.bindLong(1, bookId);
				deleteStatement.execute();
			} else {
				updateStatement.bindLong(1, fileId);
				updateStatement.bindLong(2, bookId);
				updateStatement.execute();
			}
		}
		cursor.close();

		myDatabase.execSQL("ALTER TABLE Books RENAME TO Books_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_id INTEGER UNIQUE NOT NULL REFERENCES Files(file_id))");
		myDatabase.execSQL("INSERT INTO Books (book_id,encoding,language,title,file_id) SELECT book_id,encoding,language,title,file_id FROM Books_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Books_Obsolete");
	}

	private void updateTables7() {
		final ArrayList<Long> seriesIDs = new ArrayList<Long>();
		Cursor cursor = myDatabase.rawQuery(
			"SELECT series_id,name FROM Series", null
		);
		while (cursor.moveToNext()) {
			if (cursor.getString(1).length() > 200) {
				seriesIDs.add(cursor.getLong(0));
			}
		}
		cursor.close();
		if (seriesIDs.isEmpty()) {
			return;
		}

		final ArrayList<Long> bookIDs = new ArrayList<Long>();
		for (Long id : seriesIDs) {
			cursor = myDatabase.rawQuery(
				"SELECT book_id FROM BookSeries WHERE series_id=" + id, null
			);
			while (cursor.moveToNext()) {
				bookIDs.add(cursor.getLong(0));
			}
			cursor.close();
			myDatabase.execSQL("DELETE FROM BookSeries WHERE series_id=" + id);
			myDatabase.execSQL("DELETE FROM Series WHERE series_id=" + id);
		}

		for (Long id : bookIDs) {
			myDatabase.execSQL("DELETE FROM Books WHERE book_id=" + id);
			myDatabase.execSQL("DELETE FROM BookAuthor WHERE book_id=" + id);
			myDatabase.execSQL("DELETE FROM BookTag WHERE book_id=" + id);
		}
	}

	private void updateTables8() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookList ( " +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books (book_id))");
	}

	private void updateTables9() {
		myDatabase.execSQL("CREATE INDEX BookList_BookIndex ON BookList (book_id)");
	}

	private void updateTables10() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Favorites(" +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books(book_id))");
	}

	private void updateTables11() {
		myDatabase.execSQL("UPDATE Files SET size = size + 1");
	}

	private void updateTables12() {
		myDatabase.execSQL("DELETE FROM Files WHERE parent_id IN (SELECT file_id FROM Files WHERE name LIKE '%.epub')");
	}

	private void updateTables13() {
		myDatabase.execSQL(
			"ALTER TABLE Bookmarks ADD COLUMN visible INTEGER DEFAULT 1"
		);
	}

	private void updateTables14() {
		myDatabase.execSQL("ALTER TABLE BookSeries RENAME TO BookSeries_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookSeries(" +
				"series_id INTEGER NOT NULL REFERENCES Series(series_id)," +
				"book_id INTEGER NOT NULL UNIQUE REFERENCES Books(book_id)," +
				"book_index REAL)");
		myDatabase.execSQL("INSERT INTO BookSeries (series_id,book_id,book_index) SELECT series_id,book_id,book_index FROM BookSeries_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS BookSeries_Obsolete");
	}

	private void updateTables15() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS VisitedHyperlinks(" +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"hyperlink_id TEXT NOT NULL," +
				"CONSTRAINT VisitedHyperlinks_Unique UNIQUE (book_id, hyperlink_id))");
	}

	private void updateTables16() {
		myDatabase.execSQL(
			"ALTER TABLE Books ADD COLUMN `exists` INTEGER DEFAULT 1"
		);
	}

	private void updateTables17() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookStatus(" +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id) PRIMARY KEY," +
				"access_time INTEGER NOT NULL," +
				"pages_full INTEGER NOT NULL," +
				"page_current INTEGER NOT NULL)");
	}

	private void updateTables18() {
		myDatabase.execSQL("ALTER TABLE BookSeries RENAME TO BookSeries_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookSeries(" +
				"series_id INTEGER NOT NULL REFERENCES Series(series_id)," +
				"book_id INTEGER NOT NULL UNIQUE REFERENCES Books(book_id)," +
				"book_index TEXT)");
		final SQLiteStatement insert = myDatabase.compileStatement(
			"INSERT INTO BookSeries (series_id,book_id,book_index) VALUES (?,?,?)"
		);
		final Cursor cursor = myDatabase.rawQuery("SELECT series_id,book_id,book_index FROM BookSeries_Obsolete", null);
		while (cursor.moveToNext()) {
			insert.bindLong(1, cursor.getLong(0));
			insert.bindLong(2, cursor.getLong(1));
			final float index = cursor.getFloat(2);
			final String stringIndex;
			if (index == 0.0f) {
				stringIndex = null;
			} else {
				if (Math.abs(index - Math.round(index)) < 0.01) {
					stringIndex = String.valueOf(Math.round(index));
				} else {
					stringIndex = String.format("%.1f", index);
				}
			}
			final BigDecimal bdIndex = SeriesInfo.createIndex(stringIndex);
			SQLiteUtil.bindString(insert, 3, bdIndex != null ? bdIndex.toString() : null);
			insert.executeInsert();
		}
		cursor.close();
		myDatabase.execSQL("DROP TABLE IF EXISTS BookSeries_Obsolete");
	}

	private void updateTables19() {
		myDatabase.execSQL("DROP TABLE IF EXISTS BookList");
	}

	private void updateTables20() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Labels(" +
				"label_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL UNIQUE)");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookLabel(" +
				"label_id INTEGER NOT NULL REFERENCES Labels(label_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"CONSTRAINT BookLabel_Unique UNIQUE (label_id,book_id))");
		final SQLiteStatement insert = myDatabase.compileStatement(
			"INSERT INTO Labels (name) VALUES ('favorite')"
		);
		final long id = insert.executeInsert();
		myDatabase.execSQL("INSERT INTO BookLabel (label_id,book_id) SELECT " + id + ",book_id FROM Favorites");
		myDatabase.execSQL("DROP TABLE IF EXISTS Favorites");
	}

	private void updateTables21() {
		myDatabase.execSQL("DROP TABLE IF EXISTS BookUid");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookUid(" +
				"book_id INTEGER NOT NULL UNIQUE REFERENCES Books(book_id)," +
				"type TEXT NOT NULL," +
				"uid TEXT NOT NULL," +
				"CONSTRAINT BookUid_Unique UNIQUE (book_id,type,uid))");
	}

	private void updateTables22() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_paragraph INTEGER");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_word INTEGER");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_character INTEGER");
	}

	private void updateTables23() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS HighlightingStyle(" +
				"style_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"bg_color INTEGER NOT NULL)");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN style_id INTEGER NOT NULL REFERENCES HighlightingStyle(style_id) DEFAULT 1");
		myDatabase.execSQL("UPDATE Bookmarks SET end_paragraph = LENGTH(bookmark_text)");
	}

	private void updateTables24() {
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (1, '', 136*256*256 + 138*256 + 133)"); // #888a85
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (2, '', 245*256*256 + 121*256 + 0)"); // #f57900
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (3, '', 114*256*256 + 159*256 + 207)"); // #729fcf
	}

	private void updateTables25() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookReadingProgress(" +
				"book_id INTEGER PRIMARY KEY REFERENCES Books(book_id)," +
				"numerator INTEGER NOT NULL," +
				"denominator INTEGER NOT NULL)");
	}

	private void updateTables26() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookHash(" +
				"book_id INTEGER PRIMARY KEY REFERENCES Books(book_id)," +
				"timestamp INTEGER NOT NULL," +
				"hash TEXT(40) NOT NULL)"
		);
	}

	private void updateTables27() {
		myDatabase.execSQL("ALTER TABLE BookState ADD COLUMN timestamp INTEGER");
	}

	private void updateTables28() {
		myDatabase.execSQL("ALTER TABLE HighlightingStyle ADD COLUMN fg_color INTEGER NOT NULL DEFAULT -1");
	}

	private void updateTables29() {
		myDatabase.execSQL("DROP TABLE IF EXISTS BookHistory");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookHistory(" +
				"book_id INTEGER REFERENCES Books(book_id)," +
				"timestamp INTEGER NOT NULL," +
				"event INTEGER NOT NULL)"
		);

		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id FROM RecentBooks ORDER BY book_index", null
		);
		SQLiteStatement insert = myDatabase.compileStatement(
			"INSERT OR IGNORE INTO BookHistory(book_id,timestamp,event) VALUES (?,?,?)"
		);
		insert.bindLong(3, HistoryEvent.Opened);
		int count = -1;
		while (cursor.moveToNext()) {
			insert.bindLong(1, cursor.getLong(0));
			insert.bindLong(2, count);
			try {
				insert.executeInsert();
			} catch (Throwable t) {
				// ignore
			}
			--count;
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id FROM Books ORDER BY book_id DESC", null
		);
		insert = myDatabase.compileStatement(
			"INSERT OR IGNORE INTO BookHistory(book_id,timestamp,event) VALUES (?,?,?)"
		);
		insert.bindLong(3, HistoryEvent.Added);
		while (cursor.moveToNext()) {
			insert.bindLong(1, cursor.getLong(0));
			insert.bindLong(2, count);
			try {
				insert.executeInsert();
			} catch (Throwable t) {
				// ignore
			}
			--count;
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,timestamp,event FROM BookHistory", null
		);
		while (cursor.moveToNext()) {
			System.err.println("HISTORY RECORD: " + cursor.getLong(0) + " : " + cursor.getLong(1) + " : " + cursor.getLong(2));
		}
		cursor.close();
	}

	private void updateTables30() {
		myDatabase.execSQL("DROP TABLE IF EXISTS RecentBooks");
	}

	private void updateTables31() {
		myDatabase.execSQL("ALTER TABLE BookLabel ADD COLUMN timestamp INTEGER NOT NULL DEFAULT -1");
	}

	private void updateTables32() {
		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Options(name TEXT PRIMARY KEY, value TEXT)");
	}

	private void updateTables33() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN uid TEXT(36)");
		final Cursor cursor = myDatabase.rawQuery("SELECT bookmark_id FROM Bookmarks", null);
		final SQLiteStatement statement = get("UPDATE Bookmarks SET uid=? WHERE bookmark_id=?");
		while (cursor.moveToNext()) {
			statement.bindString(1, UUID.randomUUID().toString());
			statement.bindLong(2, cursor.getLong(0));
			statement.execute();
		}
		cursor.close();

		myDatabase.execSQL("ALTER TABLE Bookmarks RENAME TO Bookmarks_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"uid TEXT(36) NOT NULL UNIQUE," +
				"version_uid TEXT(36)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"visible INTEGER DEFAULT 1," +
				"style_id INTEGER NOT NULL REFERENCES HighlightingStyle(style_id) DEFAULT 1," +
				"bookmark_text TEXT NOT NULL," +
				"creation_time INTEGER NOT NULL," +
				"modification_time INTEGER," +
				"access_time INTEGER," +
				"model_id TEXT," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL," +
				"end_paragraph INTEGER," +
				"end_word INTEGER," +
				"end_character INTEGER)"
		);
		final String fields = "bookmark_id,uid,book_id,visible,style_id,bookmark_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character";
		myDatabase.execSQL("INSERT INTO Bookmarks (" + fields + ") SELECT " + fields + " FROM Bookmarks_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Bookmarks_Obsolete");
	}

	private void updateTables34() {
		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS DeletedBookmarkIds(uid TEXT(36) PRIMARY KEY)");
	}

	private void updateTables35() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN original_text TEXT DEFAULT NULL");
	}

	private int styleBg(int styleId) {
		switch (styleId) {
			case 1:
				return 0x888a85;
			case 2:
				return 0xf57900;
			case 3:
				return 0x729fcf;
			default:
				return 0;
		}
	}

	private void updateTables36() {
		myDatabase.execSQL("ALTER TABLE HighlightingStyle ADD COLUMN timestamp INTEGER DEFAULT 0");

		final String sql = "SELECT style_id,name,bg_color FROM HighlightingStyle";
		final Cursor cursor = myDatabase.rawQuery(sql, null);
		final SQLiteStatement statement =
			get("UPDATE HighlightingStyle SET timestamp=? WHERE style_id=?");
		while (cursor.moveToNext()) {
			final int styleId = (int)cursor.getLong(0);
			if ((!cursor.isNull(1) && !"".equals(cursor.getString(1))) ||
					styleBg(styleId) != (int)cursor.getLong(2)) {
				statement.bindLong(1, System.currentTimeMillis());
				statement.bindLong(2, styleId);
				statement.execute();
			}
		}
		cursor.close();
	}

	private void updateTables37() {
		myDatabase.execSQL("ALTER TABLE Bookmarks RENAME TO Bookmarks_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"uid TEXT(36) NOT NULL UNIQUE," +
				"version_uid TEXT(36)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"visible INTEGER DEFAULT 1," +
				"style_id INTEGER NOT NULL REFERENCES HighlightingStyle(style_id) DEFAULT 1," +
				"bookmark_text TEXT NOT NULL," +
				"creation_time INTEGER NOT NULL," +
				"modification_time INTEGER," +
				"access_time INTEGER," +
				"model_id TEXT," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL," +
				"end_paragraph INTEGER," +
				"end_word INTEGER," +
				"end_character INTEGER)"
		);
		final String fields = "bookmark_id,uid,version_uid,book_id,visible,style_id,bookmark_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character";
		myDatabase.execSQL("INSERT INTO Bookmarks (" + fields + ") SELECT " + fields + " FROM Bookmarks_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Bookmarks_Obsolete");
	}

	private void updateTables38() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN original_text TEXT DEFAULT NULL");
	}

	private void updateTables39() {
		myDatabase.execSQL("ALTER TABLE BookLabel RENAME TO BookLabel_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookLabel(" +
				"label_id INTEGER NOT NULL REFERENCES Labels(label_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"timestamp INTEGER NOT NULL DEFAULT -1," +
				"uid TEXT(36) NOT NULL UNIQUE," +
				"CONSTRAINT BookLabel_Unique UNIQUE (label_id,book_id))");
		final Cursor cursor = myDatabase.rawQuery("SELECT label_id,book_id,timestamp FROM BookLabel_Obsolete", null);
		final SQLiteStatement statement = get("INSERT INTO BookLabel (label_id,book_id,timestamp,uid) VALUES (?,?,?,?)");
		while (cursor.moveToNext()) {
			statement.bindLong(1, cursor.getLong(0));
			statement.bindLong(2, cursor.getLong(1));
			statement.bindLong(3, cursor.getLong(2));
			statement.bindString(4, UUID.randomUUID().toString());
			statement.execute();
		}
		cursor.close();
		myDatabase.execSQL("DROP TABLE IF EXISTS BookLabel_Obsolete");

		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS DeletedBookLabelIds(uid TEXT(36) PRIMARY KEY)");
	}

	private SQLiteStatement get(String sql) {
		SQLiteStatement statement = myStatements.get(sql);
		if (statement == null) {
			statement = myDatabase.compileStatement(sql);
			myStatements.put(sql, statement);
		}
		return statement;
	}
}
