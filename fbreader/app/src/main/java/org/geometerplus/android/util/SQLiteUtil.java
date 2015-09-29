/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.util;

import java.util.Date;

import android.database.sqlite.SQLiteStatement;
import android.database.Cursor;

public abstract class SQLiteUtil {
	public static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	public static void bindLong(SQLiteStatement statement, int index, Long value) {
		if (value != null) {
			statement.bindLong(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	public static void bindDate(SQLiteStatement statement, int index, Date value) {
		if (value != null) {
			statement.bindLong(index, value.getTime());
		} else {
			statement.bindNull(index);
		}
	}

	public static Date getDate(Cursor cursor, int index) {
		if (cursor.isNull(index)) {
			return null;
		}
		return new Date(cursor.getLong(index));
	}
}
