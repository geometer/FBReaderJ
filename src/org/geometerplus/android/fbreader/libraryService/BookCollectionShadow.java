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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.library.*;

public class BookCollectionShadow implements IBookCollection, ServiceConnection {
	private final Context myContext;
	private volatile LibraryInterface myInterface;
	private Runnable myOnBindAction;

	public BookCollectionShadow(Context context) {
		myContext = context;
	}

	public synchronized void bindToService(Runnable onBindAction) {
		if (myInterface != null) {
			if (onBindAction != null) {
				onBindAction.run();
			}
		} else {
			if (onBindAction != null) {
				myOnBindAction = onBindAction;
			}
			myContext.bindService(
				new Intent(myContext, LibraryService.class),
				this,
				LibraryService.BIND_AUTO_CREATE
			);
		}
	}

	public void unbind() {
		if (myInterface != null) {
			myContext.unbindService(this);
			myInterface = null;
		}
	}

	public void addListener(Listener listener) {
		// TODO: implement
	}

	public void removeListener(Listener listener) {
		// TODO: implement
	}

	public synchronized int size() {
		if (myInterface == null) {
			return 0;
		}
		try {
			return myInterface.size();
		} catch (RemoteException e) {
			return 0;
		}
	}

	public synchronized List<Book> books(String pattern) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookList(myInterface.books(pattern));
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized List<Book> recentBooks() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookList(myInterface.recentBooks());
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized List<Book> favorites() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookList(myInterface.favorites());
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized Book getRecentBook(int index) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getRecentBook(index));
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized Book getBookByFile(ZLFile file) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByFile(file.getPath()));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookById(long id) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookById(id));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized void removeBook(Book book, boolean deleteFromDisk) {
		if (myInterface != null) {
			try {
				myInterface.removeBook(SerializerUtil.serialize(book), deleteFromDisk);
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void addBookToRecentList(Book book) {
		if (myInterface != null) {
			try {
				myInterface.addBookToRecentList(SerializerUtil.serialize(book));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void setBookFavorite(Book book, boolean favorite) {
		if (myInterface != null) {
			try {
				myInterface.setBookFavorite(SerializerUtil.serialize(book), favorite);
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized List<Bookmark> invisibleBookmarks(Book book) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookmarkList(
				myInterface.invisibleBookmarks(SerializerUtil.serialize(book))
			);
		} catch (RemoteException e) {
			return Collections.emptyList();
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

	public synchronized void saveBookmark(Bookmark bookmark) {
		if (myInterface != null) {
			try {
				bookmark.update(SerializerUtil.deserializeBookmark(
					myInterface.saveBookmark(SerializerUtil.serialize(bookmark))
				));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void deleteBookmark(Bookmark bookmark) {
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
			myOnBindAction = null;
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
	}
}
