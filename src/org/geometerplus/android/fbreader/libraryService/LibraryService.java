/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.libraryService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;

public class LibraryService extends Service implements Library.ChangeListener {
	public final class LibraryImplementation extends LibraryInterface.Stub {
		private final AbstractLibrary myBaseLibrary;

		LibraryImplementation() {
			BooksDatabase database = SQLiteBooksDatabase.Instance();
			if (database == null) {
				database = new SQLiteBooksDatabase(LibraryService.this, "LIBRARY SERVICE");
			}
			myBaseLibrary = new Library(database);
			((Library)myBaseLibrary).startBuild();
		}

		public boolean isUpToDate() {
			return myBaseLibrary.isUpToDate();
		}
	}

	private LibraryImplementation myLibrary;

	@Override
	public void onStart(Intent intent, int startId) {
		onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.err.println("LibraryService started for intent " + intent);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		System.err.println("LibraryService binded for intent " + intent);
		return myLibrary;
	}

	@Override
	public void onCreate() {
		System.err.println("LibraryService.onCreate()");
		super.onCreate();
		myLibrary = new LibraryImplementation();
		myLibrary.myBaseLibrary.addChangeListener(this);
	}

	@Override
	public void onDestroy() {
		System.err.println("LibraryService.onDestroy()");
		myLibrary.myBaseLibrary.removeChangeListener(this);
		myLibrary = null;
		super.onDestroy();
	}

	public void onLibraryChanged(final Code code) {
		// TODO: implement signal sending
		System.err.println("LibraryService.onLibraryChanged(" + code + "): " + myLibrary.isUpToDate());
	}
}
