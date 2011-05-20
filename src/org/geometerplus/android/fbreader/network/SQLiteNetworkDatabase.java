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

package org.geometerplus.android.fbreader.network;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.NetworkDatabase;
import org.geometerplus.fbreader.network.urlInfo.*;

import org.geometerplus.android.util.SQLiteUtil;

class SQLiteNetworkDatabase extends NetworkDatabase {
	private final SQLiteDatabase myDatabase;

	SQLiteNetworkDatabase() {
		myDatabase = ZLAndroidApplication.Instance().openOrCreateDatabase("network.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentCodeVersion = 4;
		if (version >= currentCodeVersion) {
			return;
		}
		myDatabase.beginTransaction();
		switch (version) {
			case 0:
				createTables();
			case 1:
				updateTables1();
			case 2:
				updateTables2();
			case 3:
				updateTables3();
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

	@Override
	protected void loadCustomLinks(ICustomLinksHandler handler) {
		final Cursor cursor = myDatabase.rawQuery("SELECT link_id,title,site_name,summary FROM Links", null);
		final UrlInfoCollection<UrlInfoWithDate> linksMap = new UrlInfoCollection<UrlInfoWithDate>();
		while (cursor.moveToNext()) {
			final int id = cursor.getInt(0);
			final String title = cursor.getString(1);
			final String siteName = cursor.getString(2);
			final String summary = cursor.getString(3);

			linksMap.clear();
			final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url,update_time FROM LinkUrls WHERE link_id = " + id, null);
			while (linksCursor.moveToNext()) {
				try {
					linksMap.addInfo(
						new UrlInfoWithDate(
							UrlInfo.Type.valueOf(linksCursor.getString(0)),
							linksCursor.getString(1),
							SQLiteUtil.getDate(linksCursor, 2)
						)
					);
				} catch (IllegalArgumentException e) {
				}
			}
			linksCursor.close();

			handler.handleCustomLinkData(id, siteName, title, summary, linksMap);
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
							"INSERT INTO Links (title,site_name,summary) VALUES (?,?,?)"
						);
					}
					statement = myInsertCustomLinkStatement;
				} else {
					if (myUpdateCustomLinkStatement == null) {
						myUpdateCustomLinkStatement = myDatabase.compileStatement(
							"UPDATE Links SET title = ?, site_name = ?, summary =? "
								+ "WHERE link_id = ?"
						);
					}
					statement = myUpdateCustomLinkStatement;
				}

				statement.bindString(1, link.getTitle());
				statement.bindString(2, link.getSiteName());
				SQLiteUtil.bindString(statement, 3, link.getSummary());

				final long id;
				final UrlInfoCollection<UrlInfoWithDate> linksMap =
					new UrlInfoCollection<UrlInfoWithDate>();

				if (statement == myInsertCustomLinkStatement) {
					id = statement.executeInsert();
					link.setId((int) id);
				} else {
					id = link.getId();
					statement.bindLong(4, id);
					statement.execute();
					
					final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url,update_time FROM LinkUrls WHERE link_id = " + link.getId(), null);
					while (linksCursor.moveToNext()) {
						try {
							linksMap.addInfo(
								new UrlInfoWithDate(
									UrlInfo.Type.valueOf(linksCursor.getString(0)),
									linksCursor.getString(1),
									SQLiteUtil.getDate(linksCursor, 2)
								)
							);
						} catch (IllegalArgumentException e) {
						}
					}
					linksCursor.close();
				}

				for (UrlInfo.Type key : link.getUrlKeys()) {
					final UrlInfoWithDate info = link.getUrlInfo(key);
					final UrlInfoWithDate dbInfo = linksMap.getInfo(key);
					linksMap.removeAllInfos(key);
					final SQLiteStatement urlStatement;
					if (dbInfo == null) {
						if (myInsertCustomLinkUrlStatement == null) {
							myInsertCustomLinkUrlStatement = myDatabase.compileStatement(
									"INSERT OR REPLACE INTO LinkUrls(url,update_time,link_id,key) VALUES (?,?,?,?)");
						}
						urlStatement = myInsertCustomLinkUrlStatement;
					} else if (!info.equals(dbInfo)) {
						if (myUpdateCustomLinkUrlStatement == null) {
							myUpdateCustomLinkUrlStatement = myDatabase.compileStatement(
									"UPDATE LinkUrls SET url = ?, update_time = ? WHERE link_id = ? AND key = ?");
						}
						urlStatement = myUpdateCustomLinkUrlStatement;
					} else {
						continue;
					}
					SQLiteUtil.bindString(urlStatement, 1, info.Url);
					SQLiteUtil.bindDate(urlStatement, 2, info.Updated);
					urlStatement.bindLong(3, id);
					urlStatement.bindString(4, key.toString());
					urlStatement.execute();
				}
				for (UrlInfo info : linksMap.getAllInfos()) {
					if (myDeleteCustomLinkUrlStatement == null) {
						myDeleteCustomLinkUrlStatement = myDatabase.compileStatement(
								"DELETE FROM LinkUrls WHERE link_id = ? AND key = ?");
					}
					myDeleteCustomLinkUrlStatement.bindLong(1, id);
					myDeleteCustomLinkUrlStatement.bindString(2, info.InfoType.toString());
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
							"DELETE FROM LinkUrls WHERE link_id = ?");
				}
				myDeleteAllCustomLinksStatement.bindLong(1, id);
				myDeleteAllCustomLinksStatement.execute();

				if (myDeleteCustomLinkStatement == null) {
					myDeleteCustomLinkStatement = myDatabase.compileStatement(
						"DELETE FROM Links WHERE link_id = ?"
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

	private void updateTables1() {
		myDatabase.execSQL("ALTER TABLE CustomLinks RENAME TO CustomLinks_Obsolete");
		myDatabase.execSQL(
				"CREATE TABLE CustomLinks(" +
					"link_id INTEGER PRIMARY KEY," +
					"title TEXT NOT NULL," +
					"site_name TEXT NOT NULL," +
					"summary TEXT," +
					"icon TEXT)");
		myDatabase.execSQL("INSERT INTO CustomLinks (link_id,title,site_name,summary,icon) SELECT link_id,title,site_name,summary,icon FROM CustomLinks_Obsolete");
		myDatabase.execSQL("DROP TABLE CustomLinks_Obsolete");

		myDatabase.execSQL(
				"CREATE TABLE LinkUrls(" +
					"key TEXT NOT NULL," +
					"link_id INTEGER NOT NULL REFERENCES CustomLinks(link_id)," +
					"url TEXT," +
					"update_time INTEGER," +
					"CONSTRAINT LinkUrls_PK PRIMARY KEY (key, link_id))");
		myDatabase.execSQL("INSERT INTO LinkUrls (key,link_id,url) SELECT key,link_id,url FROM CustomLinkUrls");
		myDatabase.execSQL("DROP TABLE CustomLinkUrls");
	}

	private void updateTables2() {
		myDatabase.execSQL(
				"CREATE TABLE Links(" +
					"link_id INTEGER PRIMARY KEY," +
					"title TEXT NOT NULL," +
					"site_name TEXT NOT NULL," +
					"summary TEXT)");
		myDatabase.execSQL("INSERT INTO Links (link_id,title,site_name,summary) SELECT link_id,title,site_name,summary FROM CustomLinks");
		final Cursor cursor = myDatabase.rawQuery("SELECT link_id,icon FROM CustomLinks", null);
		while (cursor.moveToNext()) {
			final int id = cursor.getInt(0);
			final String url = cursor.getString(1);
			myDatabase.execSQL("INSERT INTO LinkUrls (key,link_id,url) VALUES " +
				"('icon'," + id + ",'" + url + "')");
		}
		cursor.close();
		myDatabase.execSQL("DROP TABLE CustomLinks");
	}

	private void updateTables3() {
		myDatabase.execSQL("UPDATE LinkUrls SET key='Catalog' WHERE key='main'");
		myDatabase.execSQL("UPDATE LinkUrls SET key='Search' WHERE key='search'");
		myDatabase.execSQL("UPDATE LinkUrls SET key='Image' WHERE key='icon'");
	}
}
