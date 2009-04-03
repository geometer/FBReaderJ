/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.sqliteconfig;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.geometerplus.zlibrary.core.config.ZLConfig;

public final class ZLSQLiteConfig extends ZLConfig {
	private final Context myContext;
	private SQLiteDatabase myDatabase;

	public ZLSQLiteConfig(Context context) {
		myContext = context;
	}

	private SQLiteDatabase database() {
		if (myDatabase == null) {
			synchronized (this) {
				if (myDatabase == null) {
					myDatabase = myContext.openOrCreateDatabase("config.db", Context.MODE_PRIVATE, null);
					if (myDatabase.getVersion() == 0) {
						myDatabase.execSQL("CREATE TABLE config (groupName VARCHAR, name VARCHAR, value VARCHAR, PRIMARY KEY(groupName, name) )");
						myDatabase.setVersion(1);
					}
				}
			}
		}
		return myDatabase;
	}

	public synchronized void executeAsATransaction(Runnable actions) {
		SQLiteDatabase db = database();
		db.beginTransaction();
		try {
			actions.run();
			database().setTransactionSuccessful();
		} finally {
			database().endTransaction();
		}
	}

	public void shutdown() {
		if (myDatabase != null) {
			new Thread(new Runnable() {
				public void run() {
					synchronized (this) {
						if (myDatabase != null) {
							myDatabase.close();
							myDatabase = null;
						}
					}
				}
			}).start();
		}
	}

	public void removeGroup(String name) {
		database().delete(ourTableName, "groupName = ?", new String[] { name });
	}

	private final String ourTableName = "config";
	private final String[] ourColumns = new String[] { "value" };

	public String getValue(String group, String name, String defaultValue) {
		Cursor cursor = database().query(
				true, ourTableName, ourColumns, "groupName = ? AND name = ?", 
				new String[] { group, name }, null, null, null, null);
		String answer = defaultValue;
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			answer = cursor.getString(0);
		}
		cursor.close();
		return answer;
	}

	public void setValue(String group, String name, String value) {
		database().execSQL(
				"INSERT OR REPLACE INTO config (groupName, name, value) VALUES (?, ?, ?)",
				new String[] { group, name, value });
	}

	public void unsetValue(String group, String name) {
		database().delete(ourTableName, "groupName = ? AND name = ?", new String[] { group, name });
	}
}
