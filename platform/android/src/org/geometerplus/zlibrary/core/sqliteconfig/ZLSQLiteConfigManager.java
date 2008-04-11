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
