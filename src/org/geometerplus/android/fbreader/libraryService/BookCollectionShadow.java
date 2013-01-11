/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.Collections;
import java.util.List;

import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.fbreader.library.*;

public class BookCollectionShadow implements IBookCollection, ServiceConnection {
	private final Context myContext;
	private volatile LibraryInterface myInterface;
	private Runnable myOnBindAction;

	public BookCollectionShadow(Context context) {
		myContext = context;
	}

	public void bindToService(Runnable onBindAction) {
		myOnBindAction = onBindAction;
		myContext.bindService(
			new Intent(myContext, LibraryService.class),
			this,
			LibraryService.BIND_AUTO_CREATE
		);
	}

	public void unbind() {
		myContext.unbindService(this);
	}

	public synchronized Book getBookById(long id) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.bookById(id));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized List<Bookmark> allBookmarks() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookmarkList(myInterface.allBookmarks());
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public void saveBookmark(Bookmark bookmark) {
		if (myInterface != null) {
			try {
				bookmark.update(SerializerUtil.deserializeBookmark(
					myInterface.saveBookmark(SerializerUtil.serialize(bookmark))
				));
			} catch (RemoteException e) {
			}
		}
	}

	public void deleteBookmark(Bookmark bookmark) {
		if (myInterface != null) {
			try {
				myInterface.deleteBookmark(SerializerUtil.serialize(bookmark));
			} catch (RemoteException e) {
			}
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceConnected(ComponentName name, IBinder service) {
		myInterface = LibraryInterface.Stub.asInterface(service);
		if (myOnBindAction != null) {
			myOnBindAction.run();
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
		myInterface = null;
	}
}
