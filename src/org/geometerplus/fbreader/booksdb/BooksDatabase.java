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

package org.geometerplus.fbreader.booksdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

class BooksDatabase {
	private static BooksDatabase ourInstance;

	static BooksDatabase Instance() {
		if (ourInstance == null) {
			ourInstance = new BooksDatabase(ZLAndroidApplication.Instance());
		}
		return ourInstance;
	}

	final SQLiteDatabase Database;

	private BooksDatabase(Context context) {
		Database = context.openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		if (Database.getVersion() == 0) {
			Database.beginTransaction();
			/*
			Database.execSQL("DROP TABLE Books");
			Database.execSQL("DROP TABLE Authors");
			Database.execSQL("DROP TABLE Series");
			Database.execSQL("DROP TABLE Tags");
			Database.execSQL("DROP TABLE BookAuthor");
			Database.execSQL("DROP TABLE BookSeries");
			Database.execSQL("DROP TABLE BookTag");
			*/
			Database.execSQL(
				"CREATE TABLE Books(" +
					"book_id INTEGER PRIMARY KEY," +
					"encoding TEXT," +
					"lenguage TEXT," +
					"title TEXT NOT NULL," +
					"file_name TEXT UNIQUE NOT NULL)");
			Database.execSQL(
				"CREATE TABLE Authors(" +
					"author_id INTEGER PRIMARY KEY," +
					"name TEXT NOT NULL," +
					"sort_key TEXT NOT NULL," +
					"CONSTRAINT Authors_Unique UNIQUE (name, sort_key))");
			Database.execSQL(
				"CREATE TABLE BookAuthor(" +
					"author_id INTEGER NOT NULL REFERENCES Authors(author_id)," +
					"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
					"CONSTRAINT BookAuthor_Unique UNIQUE (author_id, book_id))");
			Database.execSQL(
				"CREATE TABLE Series(" +
					"series_id INTEGER PRIMARY KEY," +
					"name TEXT UNIQUE NOT NULL)");
			Database.execSQL(
				"CREATE TABLE BookSeries(" +
					"series_id INTEGER NOT NULL REFERENCES Series(series_id)," +
					"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
					"number_in_series INTEGER," +
					"CONSTRAINT BookSeries_Unique UNIQUE (series_id, book_id))");
			Database.execSQL(
				"CREATE TABLE Tags(" +
					"tag_id INTEGER PRIMARY KEY," +
					"name TEXT NOT NULL," +
					"parent INTEGER REFERENCES Tags(tag_id)," +
					"CONSTRAINT Tags_Unique UNIQUE (name, parent))");
			Database.execSQL(
				"CREATE TABLE BookTag(" +
					"tag_id INTEGER REFERENCES Tags(tag_id)," +
					"book_id INTEGER REFERENCES Books(book_id)," +
					"CONSTRAINT BookTag_Unique UNIQUE (tag_id, book_id))");
			Database.setTransactionSuccessful();
			Database.endTransaction();
									
			Database.setVersion(1);
		}
	}
}
