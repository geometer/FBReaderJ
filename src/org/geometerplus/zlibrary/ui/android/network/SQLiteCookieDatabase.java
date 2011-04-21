/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.network;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.core.network.CookieDatabase;
import org.geometerplus.zlibrary.core.network.Cookie;

import org.geometerplus.android.util.SQLiteUtil;

public class SQLiteCookieDatabase extends CookieDatabase {
	public static void init(Context context) {
		if (getInstance() == null) {
			new SQLiteCookieDatabase(context);
		}
	}

	private final SQLiteDatabase myDatabase;
	private final SQLiteStatement myInsertStatement;
	private final SQLiteStatement myInsertPortsStatement;
	private final SQLiteStatement myDeletePortsStatement;

	SQLiteCookieDatabase(Context context) {
		myDatabase = context.getApplicationContext().openOrCreateDatabase("cookie.db", Context.MODE_PRIVATE, null);
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Cookie(" +
				"cookie_id INTEGER PRIMARY KEY," +
				"host TEXT NOT NULL," +
				"path TEXT," +
				"name TEXT NOT NULL," +
				"value TEXT NOT NULL," +
				"date_of_expiration INTEGER," +
				"secure INTEGER," +
				"CONSTRAINT Cookie_Unique UNIQUE(host,path,name))"
		);
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS CookiePort(" +
				"cookie_id INTEGER NOT NULL REFERENCES Cookie(cookie_id)," +
				"port INTEGER NOT NULL," +
				"CONSTRAINT CookiePort_Unique UNIQUE(cookie_id,port))"
		);

		myInsertStatement = myDatabase.compileStatement(
			"INSERT OR REPLACE INTO Cookie (host,path,name,value,date_of_expiration,secure) " +
			"VALUES (?,?,?,?,?,?)"
		);
		myInsertPortsStatement = myDatabase.compileStatement(
			"INSERT OR IGNORE INTO CookiePort (cookie_id,port) VALUES (?,?)"
		);
		myDeletePortsStatement = myDatabase.compileStatement(
			"DELETE FROM CookiePort WHERE cookie_id = ?"
		);

		final long time = new Date().getTime();
		myDatabase.execSQL(
			"DELETE FROM CookiePort WHERE cookie_id IN " +
			"(SELECT cookie_id FROM Cookie WHERE date_of_expiration <= " + time + ")"
		);
		myDatabase.execSQL(
			"DELETE FROM Cookie WHERE date_of_expiration <= " + time
		);
	}

	@Override
	protected void saveCookies(Collection<Cookie> cookies) {
		for (Cookie c : cookies) {
			if (c.Discard) {
				continue;
			}
			SQLiteUtil.bindString(myInsertStatement, 1, c.Host);
			SQLiteUtil.bindString(myInsertStatement, 2, c.Path);
			SQLiteUtil.bindString(myInsertStatement, 3, c.Name);
			SQLiteUtil.bindString(myInsertStatement, 4, c.Value);
			SQLiteUtil.bindDate(myInsertStatement, 5, c.DateOfExpiration);
			myInsertStatement.bindLong(6, c.Secure ? 1 : 0);
			final long id = myInsertStatement.executeInsert();
			myDeletePortsStatement.bindLong(1, id);
			myDeletePortsStatement.execute();		
			if (c.Ports != null) {
				myInsertPortsStatement.bindLong(1, id);
				for (int port : c.Ports) {
					myInsertPortsStatement.bindLong(2, port);
					myInsertPortsStatement.execute();
				}
			}
		}
	}

	@Override
	protected Collection<Cookie> getCookiesForHost(String hostName) {
		final List<Cookie> list = new LinkedList<Cookie>();
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT cookie_id,path,name,value,date_of_expiration,secure " +
				"FROM Cookie WHERE host='" + hostName + "'", null
		);
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final String path = cursor.getString(1);
			final String name = cursor.getString(2);
			final String value = cursor.getString(3);
			final Date date = SQLiteUtil.getDate(cursor, 4);
			final boolean secure = cursor.getLong(5) == 1;
			Set<Integer> portSet = null;
			final Cursor portsCursor = myDatabase.rawQuery(
				"SELECT port FROM CookiePort WHERE cookie_id = " + id, null
			);
			while (portsCursor.moveToNext()) {
				if (portSet == null) {
					portSet = new HashSet<Integer>();
				}
				portSet.add((int)portsCursor.getLong(1));
			}
			portsCursor.close();
			list.add(new Cookie(name, value, hostName, path, portSet, date, secure, false));
		}
		cursor.close();
		return list;
	}
}
