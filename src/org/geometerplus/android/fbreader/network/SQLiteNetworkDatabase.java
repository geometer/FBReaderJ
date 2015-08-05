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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.*;

import org.geometerplus.android.util.SQLiteUtil;

class SQLiteNetworkDatabase extends NetworkDatabase {
	private final SQLiteDatabase myDatabase;

	SQLiteNetworkDatabase(Application application, NetworkLibrary library) {
		super(library);
		myDatabase = application.openOrCreateDatabase("network.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentCodeVersion = 9;
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
			case 4:
				updateTables4();
			case 5:
				updateTables5();
			case 6:
				updateTables6();
			case 7:
				updateTables7();
			case 8:
				updateTables8();
		}
		myDatabase.setTransactionSuccessful();
		myDatabase.endTransaction();

		myDatabase.execSQL("VACUUM");
		myDatabase.setVersion(currentCodeVersion);
	}

	protected void executeAsTransaction(Runnable actions) {
		myDatabase.beginTransaction();
		try {
			actions.run();
			myDatabase.setTransactionSuccessful();
		} finally {
			myDatabase.endTransaction();
		}
	}

	@Override
	protected synchronized List<INetworkLink> listLinks() {
		final List<INetworkLink> links = new LinkedList<INetworkLink>();

		final Cursor cursor = myDatabase.rawQuery("SELECT link_id,type,predefined_id,title,summary,language FROM Links", null);
		final UrlInfoCollection<UrlInfoWithDate> linksMap = new UrlInfoCollection<UrlInfoWithDate>();
		while (cursor.moveToNext()) {
			final int id = cursor.getInt(0);
			final INetworkLink.Type type = INetworkLink.Type.byIndex(cursor.getInt(1));
			final String predefinedId = cursor.getString(2);
			final String title = cursor.getString(3);
			final String summary = cursor.getString(4);
			final String language = cursor.getString(5);

			linksMap.clear();
			final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url,mime,update_time FROM LinkUrls WHERE link_id = " + id, null);
			while (linksCursor.moveToNext()) {
				try {
					linksMap.addInfo(
						new UrlInfoWithDate(
							UrlInfo.Type.valueOf(linksCursor.getString(0)),
							linksCursor.getString(1),
							MimeType.get(linksCursor.getString(2)),
							SQLiteUtil.getDate(linksCursor, 3)
						)
					);
				} catch (IllegalArgumentException e) {
				}
			}
			linksCursor.close();

			final INetworkLink l = createLink(id, type, predefinedId, title, summary, language, linksMap);
			if (l != null) {
				links.add(l);
			}
		}
		cursor.close();

		return links;
	}

	private SQLiteStatement myInsertCustomLinkStatement;
	private SQLiteStatement myUpdateCustomLinkStatement;
	private SQLiteStatement myInsertCustomLinkUrlStatement;
	private SQLiteStatement myUpdateCustomLinkUrlStatement;
	@Override
	protected synchronized void saveLink(final INetworkLink link) {
		executeAsTransaction(new Runnable() {
			public void run() {
				final SQLiteStatement statement;
				if (link.getId() == INetworkLink.INVALID_ID) {
					if (myInsertCustomLinkStatement == null) {
						myInsertCustomLinkStatement = myDatabase.compileStatement(
							"INSERT INTO Links (title,summary,language,predefined_id,type) VALUES (?,?,?,?,?)"
						);
					}
					statement = myInsertCustomLinkStatement;
				} else {
					if (myUpdateCustomLinkStatement == null) {
						myUpdateCustomLinkStatement = myDatabase.compileStatement(
							"UPDATE Links SET title=?,summary=?,language=? WHERE link_id=?"
						);
					}
					statement = myUpdateCustomLinkStatement;
				}

				statement.bindString(1, link.getTitle());
				SQLiteUtil.bindString(statement, 2, link.getSummary());
				SQLiteUtil.bindString(statement, 3, link.getLanguage());

				final long id;
				final UrlInfoCollection<UrlInfoWithDate> linksMap =
					new UrlInfoCollection<UrlInfoWithDate>();

				if (statement == myInsertCustomLinkStatement) {
					if (link instanceof IPredefinedNetworkLink) {
						statement.bindString(4, ((IPredefinedNetworkLink)link).getPredefinedId());
					} else {
						SQLiteUtil.bindString(statement, 4, null);
					}
					statement.bindLong(5, link.getType().Index);
					id = statement.executeInsert();
					link.setId((int)id);
				} else {
					id = link.getId();
					statement.bindLong(4, id);
					statement.execute();

					final Cursor linksCursor = myDatabase.rawQuery("SELECT key,url,mime,update_time FROM LinkUrls WHERE link_id=" + id, null);
					while (linksCursor.moveToNext()) {
						try {
							linksMap.addInfo(
								new UrlInfoWithDate(
									UrlInfo.Type.valueOf(linksCursor.getString(0)),
									linksCursor.getString(1),
									MimeType.get(linksCursor.getString(2)),
									SQLiteUtil.getDate(linksCursor, 3)
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
									"INSERT OR REPLACE INTO LinkUrls(url,mime,update_time,link_id,key) VALUES (?,?,?,?,?)");
						}
						urlStatement = myInsertCustomLinkUrlStatement;
					} else if (!info.equals(dbInfo)) {
						if (myUpdateCustomLinkUrlStatement == null) {
							myUpdateCustomLinkUrlStatement = myDatabase.compileStatement(
									"UPDATE LinkUrls SET url = ?, mime = ?, update_time = ? WHERE link_id = ? AND key = ?");
						}
						urlStatement = myUpdateCustomLinkUrlStatement;
					} else {
						continue;
					}
					SQLiteUtil.bindString(urlStatement, 1, info.Url);
					SQLiteUtil.bindString(urlStatement, 2, info.Mime != null ? info.Mime.toString() : "");
					SQLiteUtil.bindDate(urlStatement, 3, info.Updated);
					urlStatement.bindLong(4, id);
					urlStatement.bindString(5, key.toString());
					urlStatement.execute();
				}
				for (UrlInfo info : linksMap.getAllInfos()) {
					myDatabase.delete("LinkUrls", "link_id = ? AND key = ?",
						new String[] { String.valueOf(id), info.InfoType.toString() }
					);
				}
			}
		});
	}

	@Override
	protected synchronized void deleteLink(final INetworkLink link) {
		if (link.getId() == INetworkLink.INVALID_ID) {
			return;
		}
		executeAsTransaction(new Runnable() {
			public void run() {
				final String stringLinkId = String.valueOf(link.getId());
				myDatabase.delete("Links", "link_id = ?", new String[] { stringLinkId });
				myDatabase.delete("LinkUrls", "link_id = ?", new String[] { stringLinkId });
				link.setId(INetworkLink.INVALID_ID);
			}
		});
	}

	@Override
	protected synchronized Map<String,String> getLinkExtras(INetworkLink link) {
		final HashMap<String,String> extras = new HashMap<String,String>();
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT key,value FROM Extras WHERE link_id = ?",
			new String[] { String.valueOf(link.getId()) }
		);
		while (cursor.moveToNext()) {
			extras.put(cursor.getString(0), cursor.getString(1));
		}
		cursor.close();
		return extras;
	}

	@Override
	protected synchronized void setLinkExtras(final INetworkLink link, final Map<String,String> extras) {
		executeAsTransaction(new Runnable() {
			public void run() {
				if (link.getId() == INetworkLink.INVALID_ID) {
					return;
				}
				myDatabase.delete("Extras", "link_id = ?", new String[] { String.valueOf(link.getId()) });
				for (Map.Entry<String,String> entry : extras.entrySet()) {
					myDatabase.execSQL(
						"INSERT INTO Extras (link_id,key,value) VALUES (?,?,?)",
						new Object[] { link.getId(), entry.getKey(), entry.getValue() }
					);
				}
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

	private void updateTables4() {
		myDatabase.execSQL("ALTER TABLE Links ADD COLUMN is_predefined INTEGER");
		myDatabase.execSQL("UPDATE Links SET is_predefined=0");

		myDatabase.execSQL("ALTER TABLE Links ADD COLUMN is_enabled INTEGER DEFAULT 1");

		myDatabase.execSQL("ALTER TABLE LinkUrls RENAME TO LinkUrls_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE LinkUrls(" +
				"key TEXT NOT NULL," +
				"link_id INTEGER NOT NULL REFERENCES Links(link_id)," +
				"url TEXT," +
				"update_time INTEGER," +
				"CONSTRAINT LinkUrls_PK PRIMARY KEY (key, link_id))"
		);
		myDatabase.execSQL("INSERT INTO LinkUrls (key,link_id,url) SELECT key,link_id,url FROM LinkUrls_Obsolete");
		myDatabase.execSQL("DROP TABLE LinkUrls_Obsolete");

		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Extras(" +
				"link_id INTEGER NOT NULL REFERENCES Links(link_id)," +
				"key TEXT NOT NULL," +
				"value TEXT NOT NULL," +
				"CONSTRAINT Extras_PK PRIMARY KEY (key, link_id))"
		);
	}

	private void updateTables5() {
		myDatabase.execSQL("ALTER TABLE Links RENAME TO Links_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE Links(" +
				"link_id INTEGER PRIMARY KEY," +
				"title TEXT NOT NULL," +
				"site_name TEXT NOT NULL," +
				"summary TEXT," +
				"language TEXT," +
				"predefined_id TEXT," +
				"is_enabled INTEGER)");
		myDatabase.execSQL("INSERT INTO Links (link_id,title,site_name,summary,language,predefined_id,is_enabled) SELECT link_id,title,site_name,summary,NULL,NULL,is_enabled FROM Links_Obsolete");
		myDatabase.execSQL("DROP TABLE Links_Obsolete");
	}

	private void updateTables6() {
		myDatabase.execSQL("ALTER TABLE Links ADD COLUMN type INTEGER");
		myDatabase.execSQL("UPDATE Links SET type=" + INetworkLink.Type.Custom.Index);
	}

	private void updateTables7() {
		myDatabase.execSQL("ALTER TABLE LinkUrls ADD COLUMN mime TEXT");
	}

	private void updateTables8() {
		myDatabase.execSQL("ALTER TABLE Links RENAME TO Links_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE Links(" +
				"link_id INTEGER PRIMARY KEY," +
				"title TEXT NOT NULL," +
				"summary TEXT," +
				"language TEXT," +
				"predefined_id TEXT," +
				"is_enabled INTEGER," +
				"type INTEGER)");
		myDatabase.execSQL("INSERT INTO Links (link_id,title,summary,language,predefined_id,is_enabled,type) SELECT link_id,title,summary,language,predefined_id,is_enabled,type FROM Links_Obsolete");
		myDatabase.execSQL("DROP TABLE Links_Obsolete");
	}
}
