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

package org.geometerplus.zlibrary.core.image;

public abstract class ZLImageProxy implements ZLImage {
	public interface Synchronizer {
		void startImageLoading(ZLImageProxy image, Runnable postAction);
		void synchronize(ZLImageProxy image, Runnable postAction);
	}

	private volatile boolean myIsSynchronized;

	public final boolean isSynchronized() {
		if (myIsSynchronized && isOutdated()) {
			myIsSynchronized = false;
		}
		return myIsSynchronized;
	}

	protected final void setSynchronized() {
		myIsSynchronized = true;
	}

	protected boolean isOutdated() {
		return false;
	}

	public void startSynchronization(Synchronizer synchronizer, Runnable postAction) {
		synchronizer.startImageLoading(this, postAction);
	}

	public static enum SourceType {
		FILE,
		NETWORK,
		SERVICE;
	};
	public abstract SourceType sourceType();

	public abstract ZLImage getRealImage();
	public abstract String getId();

	@Override
	public String toString() {
		return getClass().getName() + "[" + getId() + "; synchronized=" + isSynchronized() + "]";
	}
}
