/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.synchroniser;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

public class SynchroniserService extends Service implements Runnable {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();

	@Override
	public IBinder onBind(Intent intent) {
		myCollection.bindToService(this, this);
		return null;
	}

	@Override
	public void run() {
		System.err.println("SYNCHRONIZER BINDED TO LIBRARY");
	}

	@Override
	public void onDestroy() {
		myCollection.unbind();
		System.err.println("SYNCHRONIZER UNBINDED FROM LIBRARY");
		super.onDestroy();
	}
}
