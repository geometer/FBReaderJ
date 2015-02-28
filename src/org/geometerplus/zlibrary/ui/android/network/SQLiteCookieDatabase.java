/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.CookieDatabase;

import org.geometerplus.android.util.SQLiteUtil;

public class SQLiteCookieDatabase extends CookieDatabase {
	public static synchronized void init(Context context) {
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

		removeObsolete(new Date());
	}

	@Override
	protected void removeObsolete(Date date) {
		final long time = date.getTime();
		myDatabase.execSQL(
			"DELETE FROM CookiePort WHERE cookie_id IN " +
			"(SELECT cookie_id FROM Cookie WHERE date_of_expiration <= " + time + ")"
		);
		myDatabase.execSQL(
			"DELETE FROM Cookie WHERE date_of_expiration <= " + time
		);
	}

	@Override
	protected void removeForDomain(String domain) {
		if (domain == null) {
			return;
		}

		SQLiteStatement statement = myDatabase.compileStatement(
			"DELETE FROM CookiePort WHERE cookie_id IN " +
			"(SELECT cookie_id FROM Cookie WHERE host=?)"
		);
		statement.bindString(1, domain);
		statement.execute();

		statement = myDatabase.compileStatement(
			"DELETE FROM Cookie WHERE host=?"
		);
		statement.bindString(1, domain);
		statement.execute();
	}

	@Override
	protected void removeAll() {
		myDatabase.execSQL("DELETE FROM CookiePort");
		myDatabase.execSQL("DELETE FROM Cookie");
	}

	@Override
	protected void saveCookies(List<Cookie> cookies) {
		for (Cookie c : cookies) {
			if (!c.isPersistent()) {
				continue;
			}
			SQLiteUtil.bindString(myInsertStatement, 1, c.getDomain());
			SQLiteUtil.bindString(myInsertStatement, 2, c.getPath());
			SQLiteUtil.bindString(myInsertStatement, 3, c.getName());
			SQLiteUtil.bindString(myInsertStatement, 4, c.getValue());
			SQLiteUtil.bindDate(myInsertStatement, 5, c.getExpiryDate());
			myInsertStatement.bindLong(6, c.isSecure() ? 1 : 0);
			final long id = myInsertStatement.executeInsert();
			myDeletePortsStatement.bindLong(1, id);
			myDeletePortsStatement.execute();
			if (c.getPorts() != null) {
				myInsertPortsStatement.bindLong(1, id);
				for (int port : c.getPorts()) {
					myInsertPortsStatement.bindLong(2, port);
					myInsertPortsStatement.execute();
				}
			}
		}
	}

	@Override
	protected List<Cookie> loadCookies() {
		final List<Cookie> list = new LinkedList<Cookie>();
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT cookie_id,host,path,name,value,date_of_expiration,secure FROM Cookie", null
		);
		while (cursor.moveToNext()) {
			final long id = cursor.getLong(0);
			final String host = cursor.getString(1);
			final String path = cursor.getString(2);
			final String name = cursor.getString(3);
			final String value = cursor.getString(4);
			final Date date = SQLiteUtil.getDate(cursor, 5);
			final boolean secure = cursor.getLong(6) == 1;
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
			final BasicClientCookie2 c = new BasicClientCookie2(name, value);
			c.setDomain(host);
			c.setPath(path);
			if (portSet != null) {
				final int ports[] = new int[portSet.size()];
				int index = 0;
				for (int p : portSet) {
					ports[index] = p;
					++index;
				}
				c.setPorts(ports);
			}
			c.setExpiryDate(date);
			c.setSecure(secure);
			c.setDiscard(false);
			list.add(c);
		}
		cursor.close();
		return list;
	}
}
