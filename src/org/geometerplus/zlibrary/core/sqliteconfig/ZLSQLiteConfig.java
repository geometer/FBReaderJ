/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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
import android.util.Log;
import android.widget.Toast;
import android.preference.Preference;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.android.fbreader.preferences.PreferenceActivity;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

public final class ZLSQLiteConfig extends ZLConfig {
	private final SQLiteDatabase myDatabase;
	private final SQLiteStatement myGetValueStatement;
	private final SQLiteStatement mySetValueStatement;
	private final SQLiteStatement myUnsetValueStatement;
	private final SQLiteStatement myDeleteGroupStatement;
	private final SQLiteStatement myDeleteAllStatement;

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
		myDeleteAllStatement = myDatabase.compileStatement("DELETE FROM config");
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

	/**
	 * Save all config parameters to an XML file
	 * @param filename absolute path to file
	 */
	@Override
	synchronized public void saveConfigToFile(String filename) {
		Properties prop = new Properties();
		try {
			FileOutputStream out = new FileOutputStream(filename);
			Cursor c = myDatabase.rawQuery("SELECT groupName,name,value from config order by groupName,name", null);
			for (int j=0; j < c.getCount(); j++) {
				c.moveToPosition(j);
				prop.setProperty(c.getString(0) + "::" + c.getString(1), c.getString(2));
			}
			prop.storeToXML(out, "Config file for FBReaderJ");
			out.close();
		}
		catch(Exception e) {
		}
	}

	/**
	 * Load config from XML file
	 * @param filename absolute path to file
	 * @throws Exception
	 */
	@Override
	synchronized public void loadConfigFromFile(String filename) throws Exception {
		Properties prop = new Properties();
		FileInputStream in = new FileInputStream(filename);
		prop.loadFromXML(in);
		in.close();
		Enumeration pNames = prop.propertyNames();
		// Empty config DB first
		myDeleteAllStatement.execute();
		while (pNames.hasMoreElements()) {
			Object raw = pNames.nextElement();
			String[] names = ((String)raw).split("::"); // groupName, name
			String groupName = names[0];
			String name = names[1];
			String val = prop.getProperty((String)raw);
			setValue(groupName, name, val);
			PreferenceActivity.updatePreference(groupName, name, val);
		}
	}
}
