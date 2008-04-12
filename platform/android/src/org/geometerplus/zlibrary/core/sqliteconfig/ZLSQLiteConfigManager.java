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

import java.io.FileNotFoundException;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.config.ZLConfigManager;

public class ZLSQLiteConfigManager extends ZLConfigManager {
	private SQLiteDatabase myDatabase;

	public ZLSQLiteConfigManager(Activity activity, String applicationName) {
		try {
			myDatabase = activity.openDatabase(applicationName, null);
		} catch (FileNotFoundException e) {
			try {
				myDatabase = activity.createDatabase(applicationName, 0, Activity.MODE_PRIVATE, null);
				myDatabase.execSQL("CREATE TABLE config (groupName VARCHAR, name VARCHAR, value VARCHAR, PRIMARY KEY(groupName, name) )");
			} catch (FileNotFoundException e2) {
			}
		}
		if (myDatabase != null) {
			setConfig(new ZLSQLiteConfig(myDatabase));
		}
	}

	public void shutdown() {
		if (myDatabase != null) {
			myDatabase.close();
		}
	}

	public void saveAll() {
	}

	public void saveDelta() {
	}
}
