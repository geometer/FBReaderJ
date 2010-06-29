/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.network;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.NetworkDatabase;

class SQLiteNetworkDatabase extends NetworkDatabase {
	private final SQLiteDatabase myDatabase;

	SQLiteNetworkDatabase() {
		myDatabase = ZLAndroidApplication.Instance().openOrCreateDatabase("network.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentCodeVersion = 1;
		if (version >= currentCodeVersion) {
			return;
		}
		myDatabase.beginTransaction();
		switch (version) {
			case 0:
				createTables();
		}
		myDatabase.setTransactionSuccessful();
		myDatabase.endTransaction();

		myDatabase.execSQL("VACUUM");
		myDatabase.setVersion(currentCodeVersion);
	}

	protected void executeAsATransaction(Runnable actions) {
		myDatabase.beginTransaction();
		try {
			actions.run();
			myDatabase.setTransactionSuccessful();
		} finally {
			myDatabase.endTransaction();
		}
	}

	private static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	@Override
	protected void loadCustomLinks(List<ICustomNetworkLink> links, ICustomLinksFactory factory) {
		final Cursor cursor = myDatabase.rawQuery("SELECT link_id,title,site_name,summary,icon FROM CustomLinks", null);
		final HashMap<String,String> linksMap = new HashMap<String,String>();
		while (cursor.moveToNext()) {
			final int id = cursor.getInt(0);
			final String title = cursor.getString(1);
			final String siteName = cursor.getString(2);
			final String summary = cursor.getString(3);
			final String icon = cursor.getString(4);

			linksMap.clear();
			final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url FROM CustomLinkUrls WHERE link_id = " + id, null);
			while (linksCursor.moveToNext()) {
				linksMap.put(linksCursor.getString(0), linksCursor.getString(1));
			}
			linksCursor.close();

			final ICustomNetworkLink newLink = factory.createCustomLink(id, siteName, title, summary, icon, linksMap);
			if (newLink != null) {
				links.add(newLink);
			}
		}
		cursor.close();
	}

	private SQLiteStatement myInsertCustomLinkStatement;
	private SQLiteStatement myUpdateCustomLinkStatement;
	private SQLiteStatement myInsertCustomLinkUrlStatement;
	private SQLiteStatement myUpdateCustomLinkUrlStatement;
	private SQLiteStatement myDeleteCustomLinkUrlStatement;
	@Override
	protected void saveCustomLink(final ICustomNetworkLink link) {
		executeAsATransaction(new Runnable() {
			public void run() {
				final SQLiteStatement statement;
				if (link.getId() == ICustomNetworkLink.INVALID_ID) {
					if (myInsertCustomLinkStatement == null) {
						myInsertCustomLinkStatement = myDatabase.compileStatement(
							"INSERT INTO CustomLinks (title,site_name,summary,icon) VALUES (?,?,?,?)"
						);
					}
					statement = myInsertCustomLinkStatement;
				} else {
					if (myUpdateCustomLinkStatement == null) {
						myUpdateCustomLinkStatement = myDatabase.compileStatement(
							"UPDATE CustomLinks SET title = ?, site_name = ?, summary =?, icon = ? "
								+ "WHERE link_id = ?"
						);
					}
					statement = myUpdateCustomLinkStatement;
				}

				statement.bindString(1, link.getTitle());
				statement.bindString(2, link.getSiteName());
				bindString(statement, 3, link.getSummary());
				bindString(statement, 4, link.getIcon());

				final long id;
				final HashMap<String,String> linksMap = new HashMap<String,String>();

				if (statement == myInsertCustomLinkStatement) {
					id = statement.executeInsert();
					link.setId((int) id);
				} else {
					id = link.getId();
					statement.bindLong(5, id);
					statement.execute();
					
					final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url FROM CustomLinkUrls WHERE link_id = " + link.getId(), null);
					while (linksCursor.moveToNext()) {
						linksMap.put(linksCursor.getString(0), linksCursor.getString(1));
					}
					linksCursor.close();
				}

				for (String key: link.getLinkKeys()) {
					final String value = link.getLink(key);
					final String dbValue = linksMap.remove(key);
					final SQLiteStatement urlStatement;
					if (dbValue == null) {
						if (myInsertCustomLinkUrlStatement == null) {
							myInsertCustomLinkUrlStatement = myDatabase.compileStatement(
									"INSERT INTO CustomLinkUrls(url,link_id,key) VALUES (?,?,?)");
						}
						urlStatement = myInsertCustomLinkUrlStatement;
					} else if (!value.equals(dbValue)) {
						if (myUpdateCustomLinkUrlStatement == null) {
							myUpdateCustomLinkUrlStatement = myDatabase.compileStatement(
									"UPDATE CustomLinkUrls SET url = ? WHERE link_id = ? AND key = ?");
						}
						urlStatement = myUpdateCustomLinkUrlStatement;
					} else {
						continue;
					}
					urlStatement.bindString(1, value);
					urlStatement.bindLong(2, id);
					urlStatement.bindString(3, key);
					urlStatement.execute();
				}
				for (String key: linksMap.keySet()) {
					if (myDeleteCustomLinkUrlStatement == null) {
						myDeleteCustomLinkUrlStatement = myDatabase.compileStatement(
								"DELETE FROM CustomLinkUrls WHERE link_id = ? AND key = ?");
					}
					myDeleteCustomLinkUrlStatement.bindLong(1, id);
					myDeleteCustomLinkUrlStatement.bindString(2, key);
					myDeleteCustomLinkUrlStatement.execute();
				}
			}
		});
	}

	private SQLiteStatement myDeleteAllCustomLinksStatement;
	private SQLiteStatement myDeleteCustomLinkStatement;
	@Override
	protected void deleteCustomLink(final ICustomNetworkLink link) {
		if (link.getId() == ICustomNetworkLink.INVALID_ID) {
			return;
		}
		executeAsATransaction(new Runnable() {
			public void run() {
				final long id = link.getId();
				if (myDeleteAllCustomLinksStatement == null) {
					myDeleteAllCustomLinksStatement = myDatabase.compileStatement(
							"DELETE FROM CustomLinkUrls WHERE link_id = ?");
				}
				myDeleteAllCustomLinksStatement.bindLong(1, id);
				myDeleteAllCustomLinksStatement.execute();

				if (myDeleteCustomLinkStatement == null) {
					myDeleteCustomLinkStatement = myDatabase.compileStatement(
						"DELETE FROM CustomLinks WHERE link_id = ?"
					);
				}
				myDeleteCustomLinkStatement.bindLong(1, id);
				myDeleteCustomLinkStatement.execute();

				link.setId(ICustomNetworkLink.INVALID_ID);
			}
		});
	}
	
	private void createTables() {
		myDatabase.execSQL(
				"CREATE TABLE CustomLinks(" +
					"link_id INTEGER PRIMARY KEY," +
					"title TEXT UNIQUE NOT NULL," +
					"site_name TEXT NOT NULL," +
					"summary TEXT," +
					"icon TEXT)");
		myDatabase.execSQL(
				"CREATE TABLE CustomLinkUrls(" +
					"key TEXT NOT NULL," +
					"link_id INTEGER NOT NULL REFERENCES CustomLinks(link_id)," +
					"url TEXT NOT NULL," +
					"CONSTRAINT CustomLinkUrls_PK PRIMARY KEY (key, link_id))");
	}
}
