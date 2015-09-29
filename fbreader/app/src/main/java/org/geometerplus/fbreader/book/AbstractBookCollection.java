/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.book;

import java.util.*;

public abstract class AbstractBookCollection<B extends AbstractBook> implements IBookCollection<B> {
	private final List<Listener> myListeners = Collections.synchronizedList(new LinkedList<Listener>());

	public void addListener(Listener listener) {
		if (!myListeners.contains(listener)) {
			myListeners.add(listener);
		}
	}

	public void removeListener(Listener listener) {
		myListeners.remove(listener);
	}

	protected boolean hasListeners() {
		return !myListeners.isEmpty();
	}

	protected void fireBookEvent(BookEvent event, B book) {
		synchronized (myListeners) {
			for (Listener l : myListeners) {
				l.onBookEvent(event, book);
			}
		}
	}

	protected void fireBuildEvent(Status status) {
		synchronized (myListeners) {
			for (Listener l : myListeners) {
				l.onBuildEvent(status);
			}
		}
	}

	public boolean sameBook(B b0, B b1) {
		if (b0 == b1) {
			return true;
		}
		if (b0 == null || b1 == null) {
			return false;
		}

		if (b0.getPath().equals(b1.getPath())) {
			return true;
		}

		final String hash0 = getHash(b0, false);
		return hash0 != null && hash0.equals(getHash(b1, false));
	}
}
