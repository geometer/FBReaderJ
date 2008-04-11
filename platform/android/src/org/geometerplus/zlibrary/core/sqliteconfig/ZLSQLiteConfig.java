/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.geometerplus.zlibrary.core.config.ZLConfig;

class ZLSQLiteConfig implements ZLConfig {
	private final SQLiteDatabase myDatabase;

	ZLSQLiteConfig(SQLiteDatabase database) {
		myDatabase = database;
	}

	public void removeGroup(String name) {
		myDatabase.execSQL("DELETE FROM config WHERE groupName='" + name + "'");
	}

	private final String ourTableName = "config";
	private final String[] ourColumns = new String[] { "value" };
	private final String[] ourSelectionArgs = new String[0];

	public String getValue(String group, String name, String defaultValue) {
		Cursor cursor = myDatabase.query(true, ourTableName, ourColumns, "groupName='" + group + "' AND name='" + name + "'", ourSelectionArgs, null, null, null);
		String answer = defaultValue;
		if (cursor.count() != 0) {
			cursor.first();
			answer = cursor.getString(0);
		}
		cursor.close();
		return answer;
	}

	public void setValue(String group, String name, String value, String category) {
		myDatabase.execSQL("INSERT OR REPLACE INTO config (groupName, name, value) VALUES ('" + group + "', '" + name + "', '" + value + "')");
	}

	public void unsetValue(String group, String name) {
		myDatabase.execSQL("DELETE FROM config WHERE groupName='" + group + "' AND name='" + name + "'");
	}
}
