/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.util.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.database.Cursor;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.util.UIUtil;

public final class SQLiteBooksDatabase extends BooksDatabase {
	private final String myInstanceId;
	private final SQLiteDatabase myDatabase;

	public SQLiteBooksDatabase(Context context, String instanceId) {
		myInstanceId = instanceId;
		myDatabase = context.openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		migrate(context);
	}

	protected void executeAsATransaction(Runnable actions) {
		myDatabase.beginTransaction();
		try {
			actions.run();
			myDatabase.setTransactionSuccessful();
		} finally {
			myDatabase.endTransaction();
		}
	}

	private void migrate(Context context) {
		final int version = myDatabase.getVersion();
		final int currentVersion = 12;
		if (version >= currentVersion) {
			return;
		}
		UIUtil.wait((version == 0) ? "creatingBooksDatabase" : "updatingBooksDatabase", new Runnable() {
			public void run() {
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
				}
				myDatabase.setTransactionSuccessful();
				myDatabase.endTransaction();

				myDatabase.execSQL("VACUUM");
				myDatabase.setVersion(currentVersion);
			}
		}, context);
	}

	private static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	private static void bindDate(SQLiteStatement statement, int index, Date value) {
		if (value != null) {
			statement.bindLong(index, value.getTime());
		} else {
			statement.bindNull(index);
		}
	}

	private static Date getDate(Cursor cursor, int index) {
		if (cursor.isNull(index)) {
			return null;
		}
		return new Date(cursor.getLong(index));
	}

	protected Book loadBook(long bookId) {
		System.err.println("loading book info for " + bookId + " (" + myInstanceId + ")");
		Book book = null;
		final Cursor cursor = myDatabase.rawQuery("SELECT file_id,title,encoding,language FROM Books WHERE book_id = " + bookId, null);
		if (cursor.moveToNext()) {
			book = createBook(
				bookId, cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)
			);
		}
		cursor.close();
		return book;
	}

	protected Book loadBookByFile(long fileId, ZLFile file) {
		System.err.println("loading book info for " + file.getPath() + " (" + myInstanceId + ")");
		if (fileId == -1) {
			return null;
		}
		Book book = null;
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
	protected Map<Long,Book> loadBooks(FileInfoSet infos) {
		System.err.println("loading all books info for " + myInstanceId);
		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_id,title,encoding,language FROM Books", null
		);
		final int count = cursor.getCount();
		final HashMap<Long,Book> booksById = new HashMap<Long,Book>(count);
		final HashMap<Long,Book> booksByFileId = new HashMap<Long,Book>(count);
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final long fileId = cursor.getLong(1);
			final Book book = createBook(
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
		final HashMap<Long,Author> authorById = new HashMap<Long,Author>(cursor.getCount());
		while (cursor.moveToNext()) {
			authorById.put(cursor.getLong(0), new Author(cursor.getString(1), cursor.getString(2)));
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,author_id FROM BookAuthor ORDER BY author_index", null
		);
		while (cursor.moveToNext()) {
			Book book = booksById.get(cursor.getLong(0));
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
			Book book = booksById.get(cursor.getLong(0));
			if (book != null) {
				addTag(book, getTagById(cursor.getLong(1)));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT series_id,name FROM Series", null
		);
		final HashMap<Long,String> seriesById = new HashMap<Long,String>(cursor.getCount());
		while (cursor.moveToNext()) {
			seriesById.put(cursor.getLong(0), cursor.getString(1));
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,series_id,book_index FROM BookSeries", null
		);
		while (cursor.moveToNext()) {
			Book book = booksById.get(cursor.getLong(0));
			if (book != null) {
				String series = seriesById.get(cursor.getLong(1));
				if (series != null) {
					setSeriesInfo(book, series, cursor.getLong(2));
				}
			}
		}
		cursor.close();
		return booksByFileId;
	}

	private SQLiteStatement myUpdateBookInfoStatement;
	protected void updateBookInfo(long bookId, long fileId, String encoding, String language, String title) {
		if (myUpdateBookInfoStatement == null) {
			myUpdateBookInfoStatement = myDatabase.compileStatement(
				"UPDATE Books SET file_id = ?, encoding = ?, language = ?, title = ? WHERE book_id = ?"
			);
		}
		myUpdateBookInfoStatement.bindLong(1, fileId);
		bindString(myUpdateBookInfoStatement, 2, encoding);
		bindString(myUpdateBookInfoStatement, 3, language);
		myUpdateBookInfoStatement.bindString(4, title);
		myUpdateBookInfoStatement.bindLong(5, bookId);
		myUpdateBookInfoStatement.execute();
	}

	private SQLiteStatement myInsertBookInfoStatement;
	protected long insertBookInfo(ZLFile file, String encoding, String language, String title) {
		if (myInsertBookInfoStatement == null) {
			myInsertBookInfoStatement = myDatabase.compileStatement(
				"INSERT INTO Books (encoding,language,title,file_id) VALUES (?,?,?,?)"
			);
		}
		bindString(myInsertBookInfoStatement, 1, encoding);
		bindString(myInsertBookInfoStatement, 2, language);
		myInsertBookInfoStatement.bindString(3, title);
		final FileInfoSet infoSet = new FileInfoSet(file);
		myInsertBookInfoStatement.bindLong(4, infoSet.getId(file));
		return myInsertBookInfoStatement.executeInsert();
	}

	private SQLiteStatement myDeleteBookAuthorsStatement;
	protected void deleteAllBookAuthors(long bookId) {
		if (myDeleteBookAuthorsStatement == null) {
			myDeleteBookAuthorsStatement = myDatabase.compileStatement(
				"DELETE FROM BookAuthor WHERE book_id = ?"
			);
		}
		myDeleteBookAuthorsStatement.bindLong(1, bookId);
		myDeleteBookAuthorsStatement.execute();
	}

	private SQLiteStatement myGetAuthorIdStatement;
	private SQLiteStatement myInsertAuthorStatement;
	private SQLiteStatement myInsertBookAuthorStatement;
	protected void saveBookAuthorInfo(long bookId, long index, Author author) {
		if (myGetAuthorIdStatement == null) {
			myGetAuthorIdStatement = myDatabase.compileStatement(
				"SELECT author_id FROM Authors WHERE name = ? AND sort_key = ?"
			);
			myInsertAuthorStatement = myDatabase.compileStatement(
				"INSERT INTO Authors (name,sort_key) VALUES (?,?)"
			);
			myInsertBookAuthorStatement = myDatabase.compileStatement(
				"INSERT OR REPLACE INTO BookAuthor (book_id,author_id,author_index) VALUES (?,?,?)"
			);
		}

		long authorId;
		try {
			myGetAuthorIdStatement.bindString(1, author.DisplayName);
			myGetAuthorIdStatement.bindString(2, author.SortKey);
			authorId = myGetAuthorIdStatement.simpleQueryForLong();
		} catch (SQLException e) {
			myInsertAuthorStatement.bindString(1, author.DisplayName);
			myInsertAuthorStatement.bindString(2, author.SortKey);
			authorId = myInsertAuthorStatement.executeInsert();
		}
		myInsertBookAuthorStatement.bindLong(1, bookId);
		myInsertBookAuthorStatement.bindLong(2, authorId);
		myInsertBookAuthorStatement.bindLong(3, index);
		myInsertBookAuthorStatement.execute();
	}

	protected List<Author> loadAuthors(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Authors.name,Authors.sort_key FROM BookAuthor INNER JOIN Authors ON Authors.author_id = BookAuthor.author_id WHERE BookAuthor.book_id = ?", new String[] { "" + bookId });
		if (!cursor.moveToNext()) {
			return null;
		}
		final ArrayList<Author> list = new ArrayList<Author>(cursor.getCount());
		do {
			list.add(new Author(cursor.getString(0), cursor.getString(1)));
		} while (cursor.moveToNext());
		cursor.close();	
		return list;
	}

	private SQLiteStatement myGetTagIdStatement;
	private SQLiteStatement myCreateTagIdStatement;
	private long getTagId(Tag tag) {
		if (myGetTagIdStatement == null) {
			myGetTagIdStatement = myDatabase.compileStatement(
				"SELECT tag_id FROM Tags WHERE parent_id = ? AND name = ?"
			);
			myCreateTagIdStatement = myDatabase.compileStatement(
				"INSERT INTO Tags (parent_id,name) VALUES (?,?)"
			);
		}	
		{
			final Long id = myIdByTag.get(tag);
			if (id != null) {
				return id;
			}
		}
		if (tag.Parent != null) {
			myGetTagIdStatement.bindLong(1, getTagId(tag.Parent));
		} else {
			myGetTagIdStatement.bindNull(1);
		}
		myGetTagIdStatement.bindString(2, tag.Name);
		long id;
		try {
			id = myGetTagIdStatement.simpleQueryForLong();
		} catch (SQLException e) {
			if (tag.Parent != null) {
				myCreateTagIdStatement.bindLong(1, getTagId(tag.Parent));
			} else {
				myCreateTagIdStatement.bindNull(1);
			}
			myCreateTagIdStatement.bindString(2, tag.Name);
			id = myCreateTagIdStatement.executeInsert();
		}
		myIdByTag.put(tag, id);
		myTagById.put(id, tag);
		return id;
	}

	private SQLiteStatement myDeleteBookTagsStatement;
	protected void deleteAllBookTags(long bookId) {
		if (myDeleteBookTagsStatement == null) {
			myDeleteBookTagsStatement = myDatabase.compileStatement(
				"DELETE FROM BookTag WHERE book_id = ?"
			);
		}
		myDeleteBookTagsStatement.bindLong(1, bookId);
		myDeleteBookTagsStatement.execute();
	}

	private SQLiteStatement myInsertBookTagStatement;
	protected void saveBookTagInfo(long bookId, Tag tag) {
		if (myInsertBookTagStatement == null) {
			myInsertBookTagStatement = myDatabase.compileStatement(
				"INSERT INTO BookTag (book_id,tag_id) VALUES (?,?)"
			);
		}
		myInsertBookTagStatement.bindLong(1, bookId);
		myInsertBookTagStatement.bindLong(2, getTagId(tag));
		myInsertBookTagStatement.execute();
	}

	private Tag getTagById(long id) {
		Tag tag = myTagById.get(id);
		if (tag == null) {
			final Cursor cursor = myDatabase.rawQuery("SELECT parent_id,name FROM Tags WHERE tag_id = ?", new String[] { "" + id });
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

	protected List<Tag> loadTags(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Tags.tag_id FROM BookTag INNER JOIN Tags ON Tags.tag_id = BookTag.tag_id WHERE BookTag.book_id = ?", new String[] { "" + bookId });
		if (!cursor.moveToNext()) {
			return null;
		}
		ArrayList<Tag> list = new ArrayList<Tag>(cursor.getCount());
		do {
			list.add(getTagById(cursor.getLong(0)));
		} while (cursor.moveToNext());
		cursor.close();	
		return list;
	}

	private SQLiteStatement myGetSeriesIdStatement;
	private SQLiteStatement myInsertSeriesStatement;
	private SQLiteStatement myInsertBookSeriesStatement;
	private SQLiteStatement myDeleteBookSeriesStatement;
	protected void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo) {
		if (myGetSeriesIdStatement == null) {
			myGetSeriesIdStatement = myDatabase.compileStatement(
				"SELECT series_id FROM Series WHERE name = ?"
			);
			myInsertSeriesStatement = myDatabase.compileStatement(
				"INSERT INTO Series (name) VALUES (?)"
			);
			myInsertBookSeriesStatement = myDatabase.compileStatement(
				"INSERT OR REPLACE INTO BookSeries (book_id,series_id,book_index) VALUES (?,?,?)"
			);
			myDeleteBookSeriesStatement = myDatabase.compileStatement(
				"DELETE FROM BookSeries WHERE book_id = ?"
			);
		}

		if (seriesInfo == null) {
			myDeleteBookSeriesStatement.bindLong(1, bookId);
			myDeleteBookSeriesStatement.execute();
		} else {
			long seriesId;
			try {
				myGetSeriesIdStatement.bindString(1, seriesInfo.Name);
				seriesId = myGetSeriesIdStatement.simpleQueryForLong();
			} catch (SQLException e) {
				myInsertSeriesStatement.bindString(1, seriesInfo.Name);
				seriesId = myInsertSeriesStatement.executeInsert();
			}
			myInsertBookSeriesStatement.bindLong(1, bookId);
			myInsertBookSeriesStatement.bindLong(2, seriesId);
			myInsertBookSeriesStatement.bindLong(3, seriesInfo.Index);
			myInsertBookSeriesStatement.execute();
		}
	}

	protected SeriesInfo loadSeriesInfo(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Series.name,BookSeries.book_index FROM BookSeries INNER JOIN Series ON Series.series_id = BookSeries.series_id WHERE BookSeries.book_id = ?", new String[] { "" + bookId });
		SeriesInfo info = null;
		if (cursor.moveToNext()) {
			info = new SeriesInfo(cursor.getString(0), cursor.getLong(1));
		}
		cursor.close();	
		return info;
	}

	private SQLiteStatement myRemoveFileInfoStatement;
	protected void removeFileInfo(long fileId) {
		if (fileId == -1) {
			return;
		}
		if (myRemoveFileInfoStatement == null) {
			myRemoveFileInfoStatement = myDatabase.compileStatement(
				"DELETE FROM Files WHERE file_id = ?"
			);
		}
		myRemoveFileInfoStatement.bindLong(1, fileId);
		myRemoveFileInfoStatement.execute();
	}

	private SQLiteStatement myInsertFileInfoStatement;
	private SQLiteStatement myUpdateFileInfoStatement;
	protected void saveFileInfo(FileInfo fileInfo) {
		final long id = fileInfo.Id;
		SQLiteStatement statement;
		if (id == -1) {
			if (myInsertFileInfoStatement == null) {
				myInsertFileInfoStatement = myDatabase.compileStatement(
					"INSERT OR REPLACE INTO Files (name,parent_id,size) VALUES (?,?,?)"
				);
			}
			statement = myInsertFileInfoStatement;
		} else {
			if (myUpdateFileInfoStatement == null) {
				myUpdateFileInfoStatement = myDatabase.compileStatement(
					"UPDATE Files SET name = ?, parent_id = ?, size = ? WHERE file_id = ?"
				);
			}
			statement = myUpdateFileInfoStatement;
		}
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

	protected Collection<FileInfo> loadFileInfos() {
		System.err.println("loading all files info for " + myInstanceId);
		Cursor cursor = myDatabase.rawQuery(
			"SELECT file_id,name,parent_id,size FROM Files", null
		);
		HashMap<Long,FileInfo> infosById = new HashMap<Long,FileInfo>(cursor.getCount());
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

	private SQLiteStatement mySaveRecentBookStatement;
	protected void saveRecentBookIds(final List<Long> ids) {
		if (mySaveRecentBookStatement == null) {
			mySaveRecentBookStatement = myDatabase.compileStatement(
				"INSERT INTO RecentBooks (book_id) VALUES (?)"
			);
		}
		executeAsATransaction(new Runnable() {
			public void run() {
				myDatabase.delete("RecentBooks", null, null);
				for (long id : ids) {
					mySaveRecentBookStatement.bindLong(1, id);
					mySaveRecentBookStatement.execute();
				}
			}
		});
	}

	protected List<Long> loadRecentBookIds() {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id FROM RecentBooks ORDER BY book_index", null
		);
		final LinkedList<Long> ids = new LinkedList<Long>();
		while (cursor.moveToNext()) {
			ids.add(cursor.getLong(0));
		}
		cursor.close();
		return ids;
	}

	private SQLiteStatement myAddToFavoritesStatement;
	protected void addToFavorites(long bookId) {
		if (myAddToFavoritesStatement == null) {
			myAddToFavoritesStatement = myDatabase.compileStatement(
				"INSERT OR IGNORE INTO Favorites(book_id) VALUES (?)"
			);
		}
		myAddToFavoritesStatement.bindLong(1, bookId);
		myAddToFavoritesStatement.execute();
	}

	private SQLiteStatement myRemoveFromFavoritesStatement;
	protected void removeFromFavorites(long bookId) {
		if (myRemoveFromFavoritesStatement == null) {
			myRemoveFromFavoritesStatement = myDatabase.compileStatement(
				"DELETE FROM Favorites WHERE book_id = ?"
			);
		}
		myRemoveFromFavoritesStatement.bindLong(1, bookId);
		myRemoveFromFavoritesStatement.execute();
	}

	protected List<Long> loadFavoritesIds() {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id FROM Favorites", null
		);
		final LinkedList<Long> ids = new LinkedList<Long>();
		while (cursor.moveToNext()) {
			ids.add(cursor.getLong(0));
		}
		cursor.close();
		return ids;
	}

	protected List<Bookmark> loadBookmarks(long bookId) {
		LinkedList<Bookmark> list = new LinkedList<Bookmark>();
		Cursor cursor = myDatabase.rawQuery(
			"SELECT Bookmarks.bookmark_id,Bookmarks.book_id,Books.title,Bookmarks.bookmark_text,Bookmarks.creation_time,Bookmarks.modification_time,Bookmarks.access_time,Bookmarks.access_counter,Bookmarks.model_id,Bookmarks.paragraph,Bookmarks.word,Bookmarks.char FROM Bookmarks INNER JOIN Books ON Books.book_id = Bookmarks.book_id WHERE book_id = ?", new String[] { "" + bookId }
		);
		while (cursor.moveToNext()) {
			list.add(createBookmark(
				cursor.getLong(0),
				cursor.getLong(1),
				cursor.getString(2),
				cursor.getString(3),
				getDate(cursor, 4),
				getDate(cursor, 5),
				getDate(cursor, 6),
				(int)cursor.getLong(7),
				cursor.getString(8),
				(int)cursor.getLong(9),
				(int)cursor.getLong(10),
				(int)cursor.getLong(11)
			));
		}
		cursor.close();
		return list;
	}

	protected List<Bookmark> loadAllBookmarks() {
		LinkedList<Bookmark> list = new LinkedList<Bookmark>();
		myDatabase.execSQL("DELETE FROM Bookmarks WHERE book_id = -1");
		Cursor cursor = myDatabase.rawQuery(
			"SELECT Bookmarks.bookmark_id,Bookmarks.book_id,Books.title,Bookmarks.bookmark_text,Bookmarks.creation_time,Bookmarks.modification_time,Bookmarks.access_time,Bookmarks.access_counter,Bookmarks.model_id,Bookmarks.paragraph,Bookmarks.word,Bookmarks.char FROM Bookmarks INNER JOIN Books ON Books.book_id = Bookmarks.book_id", null
		);
		while (cursor.moveToNext()) {
			list.add(createBookmark(
				cursor.getLong(0),
				cursor.getLong(1),
				cursor.getString(2),
				cursor.getString(3),
				getDate(cursor, 4),
				getDate(cursor, 5),
				getDate(cursor, 6),
				(int)cursor.getLong(7),
				cursor.getString(8),
				(int)cursor.getLong(9),
				(int)cursor.getLong(10),
				(int)cursor.getLong(11)
			));
		}
		cursor.close();
		return list;
	}

	private SQLiteStatement myInsertBookmarkStatement;
	private SQLiteStatement myUpdateBookmarkStatement;
	protected long saveBookmark(Bookmark bookmark) {
		SQLiteStatement statement;
		if (bookmark.getId() == -1) {
			if (myInsertBookmarkStatement == null) {
				myInsertBookmarkStatement = myDatabase.compileStatement(
					"INSERT INTO Bookmarks (book_id,bookmark_text,creation_time,modification_time,access_time,access_counter,model_id,paragraph,word,char) VALUES (?,?,?,?,?,?,?,?,?,?)"
				);
			}
			statement = myInsertBookmarkStatement;
		} else {
			if (myUpdateBookmarkStatement == null) {
				myUpdateBookmarkStatement = myDatabase.compileStatement(
					"UPDATE Bookmarks SET book_id = ?, bookmark_text = ?, creation_time =?, modification_time = ?,access_time = ?, access_counter = ?, model_id = ?, paragraph = ?, word = ?, char = ? WHERE bookmark_id = ?"
				);
			}
			statement = myUpdateBookmarkStatement;
		}

		statement.bindLong(1, bookmark.getBookId());
		statement.bindString(2, bookmark.getText());
		bindDate(statement, 3, bookmark.getTime(Bookmark.CREATION));
		bindDate(statement, 4, bookmark.getTime(Bookmark.MODIFICATION));
		bindDate(statement, 5, bookmark.getTime(Bookmark.ACCESS));
		statement.bindLong(6, bookmark.getAccessCount());
		bindString(statement, 7, bookmark.getModelId());
		statement.bindLong(8, bookmark.ParagraphIndex);
		statement.bindLong(9, bookmark.ElementIndex);
		statement.bindLong(10, bookmark.CharIndex);

		if (statement == myInsertBookmarkStatement) {
			return statement.executeInsert();
		} else {
			final long id = bookmark.getId();
			statement.bindLong(11, id);
			statement.execute();
			return id;
		}
	}

	private SQLiteStatement myDeleteBookmarkStatement;
	protected void deleteBookmark(Bookmark bookmark) {
		if (myDeleteBookmarkStatement == null) {
			myDeleteBookmarkStatement = myDatabase.compileStatement(
				"DELETE FROM Bookmarks WHERE bookmark_id = ?"
			);
		}
		myDeleteBookmarkStatement.bindLong(1, bookmark.getId());
		myDeleteBookmarkStatement.execute();
	}

	protected ZLTextPosition getStoredPosition(long bookId) {
		ZLTextPosition position = null;
		Cursor cursor = myDatabase.rawQuery(
			"SELECT paragraph,word,char FROM BookState WHERE book_id = " + bookId, null
		);
		if (cursor.moveToNext()) {
			position = new ZLTextFixedPosition(
				(int)cursor.getLong(0),
				(int)cursor.getLong(1),
				(int)cursor.getLong(2)
			);
		}
		cursor.close();
		return position;
	}

	private SQLiteStatement myStorePositionStatement;
	protected void storePosition(long bookId, ZLTextPosition position) {
		if (myStorePositionStatement == null) {
			myStorePositionStatement = myDatabase.compileStatement(
				"INSERT OR REPLACE INTO BookState (book_id,paragraph,word,char) VALUES (?,?,?,?)"
			);
		}
		myStorePositionStatement.bindLong(1, bookId);
		myStorePositionStatement.bindLong(2, position.getParagraphIndex());
		myStorePositionStatement.bindLong(3, position.getElementIndex());
		myStorePositionStatement.bindLong(4, position.getCharIndex());
		myStorePositionStatement.execute();
	}

	private SQLiteStatement myInsertIntoBookListStatement;
	protected boolean insertIntoBookList(long bookId) {
		if (myInsertIntoBookListStatement == null) {
			myInsertIntoBookListStatement = myDatabase.compileStatement(
				"INSERT OR IGNORE INTO BookList(book_id) VALUES (?)"
			);
		}
		myInsertIntoBookListStatement.bindLong(1, bookId);
		myInsertIntoBookListStatement.execute();
		return true;
	}

	private SQLiteStatement myDeleteFromBookListStatement;
	protected boolean deleteFromBookList(long bookId) {
		if (myDeleteFromBookListStatement == null) {
			myDeleteFromBookListStatement = myDatabase.compileStatement(
				"DELETE FROM BookList WHERE book_id = ?"
			);
		}
		myDeleteFromBookListStatement.bindLong(1, bookId);
		myDeleteFromBookListStatement.execute();
		return true;
	}

	private SQLiteStatement myCheckBookListStatement;
	protected boolean checkBookList(long bookId) {
		if (myCheckBookListStatement == null) {
			myCheckBookListStatement = myDatabase.compileStatement(
				"SELECT COUNT(*) FROM BookList WHERE book_id = ?"
			);
		}
		myCheckBookListStatement.bindLong(1, bookId);
		return myCheckBookListStatement.simpleQueryForLong() > 0;
	}


	private void createTables() {
		myDatabase.execSQL(
			"CREATE TABLE Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_name TEXT UNIQUE NOT NULL)");
		myDatabase.execSQL(
			"CREATE TABLE Authors(" +
				"author_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"sort_key TEXT NOT NULL," +
				"CONSTRAINT Authors_Unique UNIQUE (name, sort_key))");
		myDatabase.execSQL(
			"CREATE TABLE BookAuthor(" +
				"author_id INTEGER NOT NULL REFERENCES Authors(author_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"author_index INTEGER NOT NULL," +
				"CONSTRAINT BookAuthor_Unique0 UNIQUE (author_id, book_id)," +
				"CONSTRAINT BookAuthor_Unique1 UNIQUE (book_id, author_index))");
		myDatabase.execSQL(
			"CREATE TABLE Series(" +
				"series_id INTEGER PRIMARY KEY," +
				"name TEXT UNIQUE NOT NULL)");
		myDatabase.execSQL(
			"CREATE TABLE BookSeries(" +
				"series_id INTEGER NOT NULL REFERENCES Series(series_id)," +
				"book_id INTEGER NOT NULL UNIQUE REFERENCES Books(book_id)," +
				"book_index INTEGER)");
		myDatabase.execSQL(
			"CREATE TABLE Tags(" +
				"tag_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent INTEGER REFERENCES Tags(tag_id)," +
				"CONSTRAINT Tags_Unique UNIQUE (name, parent))");
		myDatabase.execSQL(
			"CREATE TABLE BookTag(" +
				"tag_id INTEGER REFERENCES Tags(tag_id)," +
				"book_id INTEGER REFERENCES Books(book_id)," +
				"CONSTRAINT BookTag_Unique UNIQUE (tag_id, book_id))");
	}

	private void updateTables1() {
		myDatabase.execSQL("ALTER TABLE Tags RENAME TO Tags_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE Tags(" +
				"tag_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent_id INTEGER REFERENCES Tags(tag_id)," +
				"CONSTRAINT Tags_Unique UNIQUE (name, parent_id))");
		myDatabase.execSQL("INSERT INTO Tags (tag_id,name,parent_id) SELECT tag_id,name,parent FROM Tags_Obsolete");
		myDatabase.execSQL("DROP TABLE Tags_Obsolete");

		myDatabase.execSQL("ALTER TABLE BookTag RENAME TO BookTag_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE BookTag(" +
				"tag_id INTEGER NOT NULL REFERENCES Tags(tag_id)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"CONSTRAINT BookTag_Unique UNIQUE (tag_id, book_id))");
		myDatabase.execSQL("INSERT INTO BookTag (tag_id,book_id) SELECT tag_id,book_id FROM BookTag_Obsolete");
		myDatabase.execSQL("DROP TABLE BookTag_Obsolete");
	}

	private void updateTables2() {
		myDatabase.execSQL("CREATE INDEX BookAuthor_BookIndex ON BookAuthor (book_id)");
		myDatabase.execSQL("CREATE INDEX BookTag_BookIndex ON BookTag (book_id)");
		myDatabase.execSQL("CREATE INDEX BookSeries_BookIndex ON BookSeries (book_id)");
	}

	private void updateTables3() {
		myDatabase.execSQL(
			"CREATE TABLE Files(" +
				"file_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent_id INTEGER REFERENCES Files(file_id)," +
				"size INTEGER," +
				"CONSTRAINT Files_Unique UNIQUE (name, parent_id))");
	}

	private void updateTables4() {
		final FileInfoSet fileInfos = new FileInfoSet();
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT file_name FROM Books", null
		);
		while (cursor.moveToNext()) {
			fileInfos.check(ZLFile.createFileByPath(cursor.getString(0)).getPhysicalFile());
		}
		cursor.close();
		fileInfos.save();

		myDatabase.execSQL(
			"CREATE TABLE RecentBooks(" +
				"book_index INTEGER PRIMARY KEY," +
				"book_id INTEGER REFERENCES Books(book_id))");
		final ArrayList<Long> ids = new ArrayList<Long>();

		final SQLiteStatement statement = myDatabase.compileStatement(
			"SELECT book_id FROM Books WHERE file_name = ?"
		);

		for (int i = 0; i < 20; ++i) {
			final ZLStringOption option = new ZLStringOption("LastOpenedBooks", "Book" + i, "");
			final String fileName = option.getValue();
			option.setValue("");
			try {
				statement.bindString(1, fileName);
				final long bookId = statement.simpleQueryForLong();
				if (bookId != -1) {
					ids.add(bookId);
				}
			} catch (SQLException e) {
			}
		}
		saveRecentBookIds(ids);
	}

	private void updateTables5() {
		myDatabase.execSQL(
			"CREATE TABLE Bookmarks(" +
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
			"CREATE TABLE BookState(" +
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
			ZLConfig.Instance().removeGroup(fileName);
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
		final FileInfoSet infoSet = new FileInfoSet();
		Cursor cursor = myDatabase.rawQuery(
			"SELECT file_name FROM Books", null
		);
		while (cursor.moveToNext()) {
			infoSet.check(ZLFile.createFileByPath(cursor.getString(0)).getPhysicalFile());
		}
		cursor.close();
		infoSet.save();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name FROM Books", null
		);
		final SQLiteStatement deleteStatement = myDatabase.compileStatement("DELETE FROM Books WHERE book_id = ?");
		final SQLiteStatement updateStatement = myDatabase.compileStatement("UPDATE Books SET file_id = ? WHERE book_id = ?");
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
			"CREATE TABLE Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_id INTEGER UNIQUE NOT NULL REFERENCES Files(file_id))");
		myDatabase.execSQL("INSERT INTO Books (book_id,encoding,language,title,file_id) SELECT book_id,encoding,language,title,file_id FROM Books_Obsolete");
		myDatabase.execSQL("DROP TABLE Books_Obsolete");
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
}
