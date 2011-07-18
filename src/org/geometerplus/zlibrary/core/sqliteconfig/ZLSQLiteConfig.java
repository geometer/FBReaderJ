/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;
import java.util.LinkedList;

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
		switch (myDatabase.getVersion()) {
			case 0:
				myDatabase.execSQL("CREATE TABLE config (groupName VARCHAR, name VARCHAR, value VARCHAR, PRIMARY KEY(groupName, name) )");
				break;
			case 1:
				myDatabase.beginTransaction();
				SQLiteStatement removeStatement = myDatabase.compileStatement(
					"DELETE FROM config WHERE name = ? AND groupName LIKE ?"
				);
				removeStatement.bindString(2, "/%");
				removeStatement.bindString(1, "Size"); removeStatement.execute();
				removeStatement.bindString(1, "Title"); removeStatement.execute();
				removeStatement.bindString(1, "Language"); removeStatement.execute();
				removeStatement.bindString(1, "Encoding"); removeStatement.execute();
				removeStatement.bindString(1, "AuthorSortKey"); removeStatement.execute();
				removeStatement.bindString(1, "AuthorDisplayName"); removeStatement.execute();
				removeStatement.bindString(1, "EntriesNumber"); removeStatement.execute();
				removeStatement.bindString(1, "TagList"); removeStatement.execute();
				removeStatement.bindString(1, "Sequence"); removeStatement.execute();
				removeStatement.bindString(1, "Number in seq"); removeStatement.execute();
				myDatabase.execSQL(
					"DELETE FROM config WHERE name LIKE 'Entry%' AND groupName LIKE '/%'"
				);
				myDatabase.setTransactionSuccessful();
				myDatabase.endTransaction();
				myDatabase.execSQL("VACUUM");
				break;
		}
		myDatabase.setVersion(2);
		myGetValueStatement = myDatabase.compileStatement("SELECT value FROM config WHERE groupName = ? AND name = ?");
		mySetValueStatement = myDatabase.compileStatement("INSERT OR REPLACE INTO config (groupName, name, value) VALUES (?, ?, ?)");
		myUnsetValueStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ? AND name = ?");
		myDeleteGroupStatement = myDatabase.compileStatement("DELETE FROM config WHERE groupName = ?");

		/*
		final Cursor cursor = myDatabase.rawQuery("SELECT groupName,name FROM config WHERE groupName LIKE ? GROUP BY name", new String[] { "/%" });
		while (cursor.moveToNext()) {
			println(cursor.getString(0) + " = " + cursor.getString(1));
		}
		cursor.close();
		*/
	}

	@Override
	synchronized public List<String> listGroups() {
		final LinkedList<String> list = new LinkedList<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT DISTINCT groupName FROM config", null);
		while (cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		cursor.close();
		return list;
	}

	@Override
	synchronized public List<String> listNames(String group) {
		final LinkedList<String> list = new LinkedList<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT name FROM config WHERE groupName = ?", new String[] { group });
		while (cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		cursor.close();
		return list;
	}

	@Override
	synchronized public void removeGroup(String name) {
		myDeleteGroupStatement.bindString(1, name);
		try {
			myDeleteGroupStatement.execute();
		} catch (SQLException e) {
		}
	}

	@Override
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

	@Override
	synchronized public void setValue(String group, String name, String value) {
		mySetValueStatement.bindString(1, group);
		mySetValueStatement.bindString(2, name);
		mySetValueStatement.bindString(3, value);
		try {
			mySetValueStatement.execute();
		} catch (SQLException e) {
		}
	}

	@Override
	synchronized public void unsetValue(String group, String name) {
		myUnsetValueStatement.bindString(1, group);
		myUnsetValueStatement.bindString(2, name);
		try {
			myUnsetValueStatement.execute();
		} catch (SQLException e) {
		}
	}
}
