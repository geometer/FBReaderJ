/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.database.Cursor;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.collection.*;

final class SQLiteBooksDatabase extends BooksDatabase {
	private final SQLiteDatabase myDatabase;

	SQLiteBooksDatabase() {
		myDatabase = ZLAndroidApplication.Instance().openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		switch (myDatabase.getVersion()) {
			case 0:
				createTables();
			case 1:
				updateTables1();
		}
		myDatabase.setVersion(2);
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

	private static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	private static final String BOOKS_TABLE = "Books";
	private static final String[] BOOKS_COLUMNS = { "book_id", "encoding", "language", "title" };
	private static final String FILE_NAME_CONDITION = "file_name = ?";
	protected long loadBook(BookDescription description) {
		final Cursor cursor = myDatabase.query(
			BOOKS_TABLE,
			BOOKS_COLUMNS,
			FILE_NAME_CONDITION, new String[] { description.FileName },
			null, null, null, null
		);
		long id = -1;
		if (cursor.moveToNext()) {
			id = cursor.getLong(0);
			description.setEncoding(cursor.getString(1));
			description.setLanguage(cursor.getString(2));
			description.setTitle(cursor.getString(3));
		}
		cursor.close();
		return id;
	}

	protected void listBooks() {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT Authors.name,Authors.sort_key,Books.title,Books.language,Books.encoding,Books.file_name FROM Books LEFT JOIN BookAuthor ON Books.book_id = BookAuthor.book_id LEFT JOIN Authors ON Authors.author_id = BookAuthor.author_id ORDER by Books.file_name", null);
		while (cursor.moveToNext()) {
			System.err.println(cursor.getString(0) + ":" + cursor.getString(2));
		}
		cursor.close();	
	}

	private SQLiteStatement myUpdateBookInfoStatement;
	protected void updateBookInfo(long bookId, String encoding, String language, String title) {
		if (myUpdateBookInfoStatement == null) {
			myUpdateBookInfoStatement = myDatabase.compileStatement(
				"UPDATE Books SET encoding = ?, language = ?, title = ? WHERE book_id = ?"
			);
		}
		bindString(myUpdateBookInfoStatement, 1, encoding);
		bindString(myUpdateBookInfoStatement, 2, language);
		myUpdateBookInfoStatement.bindString(3, title);
		myUpdateBookInfoStatement.bindLong(4, bookId);
		myUpdateBookInfoStatement.execute();
	}

	private SQLiteStatement myInsertBookInfoStatement;
	protected long insertBookInfo(String fileName, String encoding, String language, String title) {
		if (myInsertBookInfoStatement == null) {
			myInsertBookInfoStatement = myDatabase.compileStatement(
				"INSERT INTO Books (encoding,language,title,file_name) VALUES (?,?,?,?)"
			);
		}
		bindString(myInsertBookInfoStatement, 1, encoding);
		bindString(myInsertBookInfoStatement, 2, language);
		myInsertBookInfoStatement.bindString(3, title);
		myInsertBookInfoStatement.bindString(4, fileName);
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
				"INSERT OR IGNORE INTO BookAuthor (book_id,author_id,author_index) VALUES (?,?,?)"
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

	protected ArrayList<Author> loadAuthors(long bookId) {
		final Cursor cursor = myDatabase.rawQuery("SELECT Authors.name,Authors.sort_key FROM BookAuthor INNER JOIN Authors ON Authors.author_id = BookAuthor.author_id WHERE BookAuthor.book_id = ?", new String[] { "" + bookId });
		if (!cursor.moveToNext()) {
			return null;
		}
		ArrayList<Author> list = new ArrayList<Author>(cursor.getCount());
		do {
			list.add(new Author(cursor.getString(0), cursor.getString(1)));
		} while (cursor.moveToNext());
		cursor.close();	
		return list;
	}

	private HashMap<Tag,Long> myIdByTag = new HashMap<Tag,Long>();
	private HashMap<Long,Tag> myTagById = new HashMap<Long,Tag>();

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

	protected ArrayList<Tag> loadTags(long bookId) {
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

	private SQLiteStatement myResetBookInfoStatement;
	private final static String myBookIdWhereClause = "book_id = ?";
	protected void resetBookInfo(String fileName) {
		if (myResetBookInfoStatement == null) {
			myResetBookInfoStatement = myDatabase.compileStatement(
				"SELECT book_id FROM Books WHERE file_name = ?"
			);
		}
		myResetBookInfoStatement.bindString(1, fileName);
		try {
			final long bookId = myResetBookInfoStatement.simpleQueryForLong();
			final String[] parameters = { "" + bookId };
			executeAsATransaction(new Runnable() {
				public void run() {
					myDatabase.delete("Books", myBookIdWhereClause, parameters);
					myDatabase.delete("BookAuthor", myBookIdWhereClause, parameters);
					myDatabase.delete("BookSeries", myBookIdWhereClause, parameters);
					myDatabase.delete("BookTag", myBookIdWhereClause, parameters);
				}
			});
		} catch (SQLException e) {
		}
	}

	private void createTables() {
		myDatabase.beginTransaction();
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
		myDatabase.setTransactionSuccessful();
		myDatabase.endTransaction();
	}

	private void updateTables1() {
		myDatabase.beginTransaction();

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

		myDatabase.setTransactionSuccessful();
		myDatabase.endTransaction();
	}
}
