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

import java.util.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.database.Cursor;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.collection.*;

final class SQLiteBooksDatabase extends BooksDatabase {
	private final SQLiteDatabase myDatabase;

	SQLiteBooksDatabase() {
		System.err.println("+ SQLiteBooksDatabase");
		myDatabase = ZLAndroidApplication.Instance().openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		if (version >= 5) {
			return;
		}
		ZLDialogManager.getInstance().wait("migrating", new Runnable() {
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
				}
				myDatabase.setTransactionSuccessful();
				myDatabase.endTransaction();

				myDatabase.execSQL("VACUUM");
				myDatabase.setVersion(4);
			}
		});
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
			FILE_NAME_CONDITION, new String[] { description.File.getPath() },
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

	private boolean myCacheIsInitialized;
	private final HashMap<Tag,Long> myIdByTag = new HashMap<Tag,Long>(50);
	private final HashMap<Long,Tag> myTagById = new HashMap<Long,Tag>(50);
	private final HashMap<Long,Author> myAuthorById = new HashMap<Long,Author>(100);
	private final HashMap<Long,String> mySeriesById = new HashMap<Long,String>(20);

	private void initCaches() {
		if (myCacheIsInitialized) {
			return;
		}
		myCacheIsInitialized = true;

		Cursor cursor = myDatabase.rawQuery(
			"SELECT author_id,name,sort_key FROM Authors", null
		);
		while (cursor.moveToNext()) {
			myAuthorById.put(cursor.getLong(0), new Author(cursor.getString(1), cursor.getString(2)));
		}
		cursor.close();
        
		cursor = myDatabase.rawQuery("SELECT tag_id,parent_id,name FROM Tags ORDER BY tag_id", null);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(0);
			if (myTagById.get(id) == null) {
				final Tag tag = Tag.getTag(myTagById.get(cursor.getLong(1)), cursor.getString(2));
				myIdByTag.put(tag, id);
				myTagById.put(id, tag);
			}
		}
		cursor.close();
        
		cursor = myDatabase.rawQuery(
			"SELECT series_id,name FROM Series", null
		);
		while (cursor.moveToNext()) {
			mySeriesById.put(cursor.getLong(0), cursor.getString(1));
		}
		cursor.close();
	}

	protected Map<String,BookDescription> listBooks() {
		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name,title,encoding,language FROM Books", null
		);
		final int count = cursor.getCount();
		final HashMap<Long,BookDescription> booksById = new HashMap<Long,BookDescription>(count);
		final HashMap<String,BookDescription> booksByFilename = new HashMap<String,BookDescription>(count);
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final String fileName = cursor.getString(1);
			final BookDescription description = createDescription(
				id, fileName, cursor.getString(2), cursor.getString(3), cursor.getString(4)
			);
			booksById.put(id, description);
			booksByFilename.put(fileName, description);
		}
		cursor.close();

		initCaches();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,author_id FROM BookAuthor ORDER BY author_index", null
		);
		while (cursor.moveToNext()) {
			BookDescription book = booksById.get(cursor.getLong(0));
			if (book != null) {
				Author author = myAuthorById.get(cursor.getLong(1));
				if (author != null) {
					addAuthor(book, author);
				}
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery("SELECT book_id,tag_id FROM BookTag", null);
		while (cursor.moveToNext()) {
			BookDescription book = booksById.get(cursor.getLong(0));
			if (book != null) {
				addTag(book, getTagById(cursor.getLong(1)));
			}
		}
		cursor.close();

		cursor = myDatabase.rawQuery(
			"SELECT book_id,series_id,book_index FROM BookSeries", null
		);
		while (cursor.moveToNext()) {
			BookDescription book = booksById.get(cursor.getLong(0));
			if (book != null) {
				String series = mySeriesById.get(cursor.getLong(1));
				if (series != null) {
					setSeriesInfo(book, series, cursor.getLong(2));
				}
			}
		}
		cursor.close();
		return booksByFilename;
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
					"INSERT INTO Files (name,parent_id,size) VALUES (?,?,?)"
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

	SQLiteStatement mySelectFileInfoStatement;
	protected Collection<FileInfo> loadFileInfos(ZLFile file) {
		final LinkedList<ZLFile> fileStack = new LinkedList<ZLFile>();
		for (; file != null; file = file.getParent()) {
			fileStack.addFirst(file);
		}

		final ArrayList<FileInfo> infos = new ArrayList<FileInfo>(fileStack.size());
		final String[] parameters = { null };
		FileInfo current = null;
		for (ZLFile f : fileStack) {
			parameters[0] = f.getName(false);
			Cursor cursor = myDatabase.rawQuery(
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
		myDatabase.delete("Files", null, null);

		final FileInfoSet fileInfos = new FileInfoSet();
		fileInfos.loadAll();
		Cursor cursor = myDatabase.rawQuery(
			"SELECT file_name FROM Books", null
		);
		while (cursor.moveToNext()) {
			fileInfos.check(ZLFile.createFileByPath(cursor.getString(0)).getPhysicalFile());
		}
		cursor.close();
		fileInfos.save();
        
		cursor = myDatabase.rawQuery(
			"SELECT file_id FROM Files", null
		);
		System.err.println("saved " + cursor.getCount() + " books");
		cursor.close();
	}
}
