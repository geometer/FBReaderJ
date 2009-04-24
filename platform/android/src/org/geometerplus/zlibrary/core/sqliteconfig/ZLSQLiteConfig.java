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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.core.config.ZLConfig;

public final class ZLSQLiteConfig extends ZLConfig {
	private final SQLiteDatabase myDatabase;
	private final SQLiteStatement myGetValueStatement;
	private final SQLiteStatement mySetValueStatement;
	private final SQLiteStatement myUnsetValueStatement;
	private final SQLiteStatement myDeleteGroupStatement;

	public ZLSQLiteConfig(Context context) {
		myDatabase = context.openOrCreateDatabase("config.db", Context.MODE_PRIVATE, null);
		if (myDatabase.getVersion() == 0) {
			myDatabase.execSQL("CREATE TABLE config (groupName VARCHAR, name VARCHAR, value VARCHAR, PRIMARY KEY(groupName, name) )");
			myDatabase.setVersion(1);
		}
		myGetValueStatement = myDatabase.compileStatement("SELECT value FROM config WHERE groupName = ? AND name = ?");
		mySetValueStatement = myDatabase.compileStatement("INSERT OR REPLACE INTO config (groupName, name, value) VALUES (?, ?, ?)");
		myUnsetValueStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ? AND name = ?");
		myDeleteGroupStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ?");

		System.err.println("+ ZLSQLiteConfig");
		/*
		final Cursor cursor = myDatabase.rawQuery("SELECT groupName,value FROM config WHERE name = ? AND groupName LIKE ?", new String[] { "Size", "/%" });
		while (cursor.moveToNext()) {
			System.err.println(cursor.getString(0) + ": " + cursor.getString(1));
		}
		cursor.close();
		*/
	}

	synchronized public void executeAsATransaction(Runnable actions) {
		myDatabase.beginTransaction();
		try {
			actions.run();
			myDatabase.setTransactionSuccessful();
		} finally {
			myDatabase.endTransaction();
		}
	}

	synchronized public void removeGroup(String name) {
		myDeleteGroupStatement.bindString(1, name);
		try {
			myDeleteGroupStatement.execute();
		} catch (SQLException e) {
		}
	}

	synchronized public String getValue(String group, String name, String defaultValue) {
		String answer = defaultValue;
		myGetValueStatement.bindString(1, group);
		myGetValueStatement.bindString(2, name);
		try {
			answer = myGetValueStatement.simpleQueryForString();
		} catch (SQLException e) {
		}
		return answer;
	}

	synchronized public void setValue(String group, String name, String value) {
		mySetValueStatement.bindString(1, group);
		mySetValueStatement.bindString(2, name);
		mySetValueStatement.bindString(3, value);
		try {
			mySetValueStatement.execute();
		} catch (SQLException e) {
		}
	}

	synchronized public void unsetValue(String group, String name) {
		myUnsetValueStatement.bindString(1, group);
		myUnsetValueStatement.bindString(2, name);
		try {
			myUnsetValueStatement.execute();
		} catch (SQLException e) {
		}
	}
}
