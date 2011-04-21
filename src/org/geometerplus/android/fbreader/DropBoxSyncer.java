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

package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.content.Context;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.Cursor;
import android.util.Log;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.dropbox.client.DropboxAPI.FileDownload;
import com.dropbox.client.DropboxClient;
import com.dropbox.client.Authenticator;

import java.util.Map;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class DropBoxSyncer  {
	
	public static boolean IN_PROGRESS;
	
	//Singleton variable
	private static DropBoxSyncer dbsyncer;
	
    final static private String CONSUMER_KEY = "XXXX";
    final static private String CONSUMER_SECRET = "YYYY";

    private DropboxAPI api = new DropboxAPI();

    final static public String ACCOUNT_PREFS_NAME = "dropboxprefs";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private String dbemailaddress;
	private String dbpassword;

    private static final String TAG = "DropBoxSyncer";

    private Config mConfig;
    private boolean mLoggedIn;

	boolean autosync = false;

	private FBReader myActivity;

	//autosync variables
	//autosync can only use context - not activity
	private Context myContext;
	private long mybook;

	//manualsync variables
	private ZLFile myBookfile;
	private ZLTextView  myView;

	public void init(Context context) {
		//myActivity = (FBReader)context;
		myContext = context;
	}
	
	public void init(ZLFile bookfile, ZLTextView view, FBReader activity) {
		myBookfile = bookfile;
		myView = view;
		myActivity = activity;
		//for the autosync process it is required only to have context, hence most 
		//of the methods in this class use Context instead of FBReader activity
		myContext = activity;
	}
	
	//Singleton method
	public static DropBoxSyncer getDropBoxSyncer() {
		if(dbsyncer == null) {
			dbsyncer = new DropBoxSyncer();
		}
		return dbsyncer;
	}

	public void setDropBoxCredentials(String email, String passwd) {
		dbemailaddress = email;
		dbpassword = passwd;
	}
	
	public void clearDropBoxCredentials() {
		dbemailaddress = null;
		dbpassword = null;
	}

	//manualsync runner
	public void run() {
		if(!DropBoxSyncer.IN_PROGRESS) {
			DropBoxSyncer.IN_PROGRESS = true;
			autosync = false;
			dropboxSync();
		}
	}

	//autosync runner
	public void run(long bookId) {
		mybook = bookId;
		autosync = true;
		if(getKeys() != null) {
			//we have keys - and will sync
			if(!DropBoxSyncer.IN_PROGRESS) {
				DropBoxSyncer.IN_PROGRESS = true;
				dropboxSync();
			}
		}
	}

	/**
     * This lets us use the Dropbox API from the LoginAsyncTask
     */
    public DropboxAPI getAPI() {
    	return api;
    }

    public void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    }


    public void showToast(String msg) {
        Toast error = Toast.makeText(myContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

    protected Config getConfig() {
    	if (mConfig == null) {
	    	mConfig = api.getConfig(null, false);
	    	// TODO On a production app which you distribute, your consumer
	    	// key and secret should be obfuscated somehow.
	    	mConfig.consumerKey=CONSUMER_KEY;
	    	mConfig.consumerSecret=CONSUMER_SECRET;
	    	mConfig.server="api.dropbox.com";
	    	mConfig.contentServer="api-content.dropbox.com";
	    	mConfig.port=80;
    	}
    	return mConfig;
    }
        
    public void setConfig(Config conf) {
    	mConfig = conf;
    }

    public String[] getKeys() {
        SharedPreferences prefs = myContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    // Save the access key/secret for later
    public void storeKeys(String key, String secret) {
        SharedPreferences prefs = myContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    public void clearKeys() {
        SharedPreferences prefs = myContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }    	

	private void dropboxSync() {
		if(getKeys() != null) {
			//have token+secret
			String key = getKeys()[0];
			String secret = getKeys()[1];
			LoginAsyncTask login = new LoginAsyncTask(this, key, secret, getConfig(), true);
	        login.execute();
		}
		else {
			if(dbemailaddress == null || dbpassword == null) {
				myActivity.getDropBoxCredentials();
				DropBoxSyncer.IN_PROGRESS = false;
			}
			else {
				//handle email + password once
				LoginAsyncTask login = new LoginAsyncTask(this, dbemailaddress, dbpassword, getConfig(), false);
	        	login.execute();
			}
		}
	}

    private int uploadDropboxFile(String dbPath, File localFile) throws IOException {
    	return api.putFile("dropbox", dbPath, localFile);
    }
    
    private boolean downloadDropboxFile(String dbPath, File localFile) throws IOException {
    	BufferedInputStream br = null;
		BufferedOutputStream bw = null;
		BufferedInputStream ttt = null;
		try {
			if (!localFile.exists()) {
				localFile.createNewFile(); //otherwise dropbox client will fail silently
			}
			
			FileDownload fd = api.getFileStream("dropbox", dbPath, null);
			//In case the file doesnt exist in dropbox
			if(fd.etag == null) {
				//create dropbox sqlite file and upload
				createDropBoxSyncDB();
				fd = api.getFileStream("dropbox", dbPath, null);
			}
			br = new BufferedInputStream(fd.is);
			bw = new BufferedOutputStream(new FileOutputStream(localFile));

			byte[] buffer = new byte[4096];
			int read;
			while (true) {
			read = br.read(buffer);
			if (read <= 0) {
			break;
			}
			bw.write(buffer, 0, read);
			}
		} 
		finally {
			//in finally block:
			if (bw != null) {
				bw.close();
			}
			if (br != null) {
				br.close();
			}
		}
		
    	return true;
    }

	//create "/.fbreader/sync.db" in your dropbox 
	private void createDropBoxSyncDB() {
		Map conf = getConfig().toMap();
		Authenticator auth = null;
		try {
			auth = new Authenticator(getConfig().toMap()); // throws IOException, OAuthException, OAuthCommunicationException
			DropboxClient dbclient = new DropboxClient(conf, auth); 
			dbclient.fileCreateFolder("dropbox", "/.fbreader", null); //throws DropboxException
			File syncerfile = myContext.getFileStreamPath("sync.db");
			syncerfile.delete();
			SQLiteDatabase syncdb = SQLiteDatabase.openOrCreateDatabase(syncerfile.getPath(), null);
			syncdb.execSQL(
				"CREATE TABLE DBBooks (" +
				"DBBook_id TEXT PRIMARY KEY," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL,"+ 
				"char INTEGER NOT NULL)");
			syncdb.close();
			uploadDropboxFile("/.fbreader/", syncerfile);
		}
		catch(Exception e) {
			Log.e(TAG, "Error Creating dropbox file.", e);
		}
	}

	//callback method from LoginAsyncTask
	public void bookSync() {
		if(autosync) {
			autoSync();
		}
		else {
			manualSync();
		}
	}

	//save book position in local copy of sync.db
	private void savePosition(ZLTextPosition currentPos, SQLiteDatabase db, String bookID) {
		SQLiteStatement mystatement = db.compileStatement("INSERT OR REPLACE INTO DBBooks values (?, ?, ?, ?)");
		mystatement.bindString(1,bookID);
		mystatement.bindLong(2, currentPos.getParagraphIndex());
		mystatement.bindLong(3, currentPos.getElementIndex());
		mystatement.bindLong(4, currentPos.getCharIndex());
		mystatement.execute();		
	}
	
	//get book positionfrom a local copy of sync.db
	private ZLTextPosition getSyncDBPosition(SQLiteDatabase db, String bookID) {
		ZLTextPosition dbposition = null;
		Cursor cursor = db.rawQuery("SELECT paragraph,word,char FROM DBBooks WHERE DBBook_id='" + bookID + "'", null);
		if(cursor.getCount() == 1) {
			cursor.moveToNext();
			dbposition = new ZLTextFixedPosition(
				(int)cursor.getLong(0),
				(int)cursor.getLong(1),
				(int)cursor.getLong(2)
				);
		}
		cursor.close();
		return dbposition;
	}
			
	private void manualSync() {
		try {
			final ZLTextPosition position = myView.getStartCursor();
			File syncfile = myActivity.getFileStreamPath("sync.db");						
			downloadDropboxFile("/.fbreader/sync.db", syncfile);

			String dbbookid = myBookfile.getShortName() + "-" + myBookfile.size();			
			SQLiteDatabase syncdb = SQLiteDatabase.openDatabase(syncfile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
			boolean moved = false;
			
			ZLTextPosition dbposition = getSyncDBPosition(syncdb, dbbookid);
			if(dbposition != null) {
				int compare;
				if(dbposition.compareTo(position) >= 0) {
					//this book is behind - update view
					myView.gotoPosition(dbposition);
					//ZLApplication.Instance().repaintView();
					final ZLViewWidget widget = ZLApplication.Instance().getViewWidget();
					widget.reset();
					widget.repaint();
					moved = true;
				}
			}
			if(!moved) {
				//this book is furthest ahead - update dropboxdb
				savePosition(position, syncdb, dbbookid);
				//upload file
				syncdb.close();
				uploadDropboxFile("/.fbreader/", syncfile);
			}
			else {
				//dont upload - just close db
				syncdb.close();
			}

			api.deauthenticate();
			setLoggedIn(false);
			DropBoxSyncer.IN_PROGRESS = false;
			showToast("Book synced.");
		}
		catch(Exception e) {
			Log.e(TAG, "Error syncing.", e);
			api.deauthenticate();
			setLoggedIn(false);
			DropBoxSyncer.IN_PROGRESS = false;
			showToast("Sync Failed!");
		}	
	}

	private void autoSync() {
		try {
			File syncfile = myContext.getFileStreamPath("sync.db");			
			downloadDropboxFile("/.fbreader/sync.db", syncfile);

			//load book and position from local db, where it just has been stored
			SQLiteBooksDatabase bdb = (SQLiteBooksDatabase)BooksDatabase.Instance();
			Book tmpbook = bdb.loadBook(mybook);
			ZLTextPosition myposition = tmpbook.getStoredPosition();
			
			String dbbookid = tmpbook.File.getShortName() + "-" + tmpbook.File.size();			
			SQLiteDatabase syncdb = SQLiteDatabase.openDatabase(syncfile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
			boolean savebook = true;

			ZLTextPosition dbposition = getSyncDBPosition(syncdb, dbbookid);
			if(dbposition != null) {
				if(dbposition.compareTo(myposition) >= 0) {
					savebook = false;
				}
				
			}
			
			if(savebook) {
				//this book is furthest ahead - update dropboxdb
				savePosition(myposition, syncdb, dbbookid);
				//upload file
				syncdb.close();
				uploadDropboxFile("/.fbreader/", syncfile);
			}
			else {
				//dont upload - just close db
				syncdb.close();
			}					
			api.deauthenticate();
			setLoggedIn(false);
			DropBoxSyncer.IN_PROGRESS = false;
		}
		catch(Exception e) {
			api.deauthenticate();
			setLoggedIn(false);
			DropBoxSyncer.IN_PROGRESS = false;
		}	

	}
	
}
