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

package org.geometerplus.zlibrary.core.image;

import org.geometerplus.zlibrary.core.util.MimeType;

public abstract class ZLImageProxy extends ZLLoadableImage {
	private volatile ZLImage myImage;

	public final ZLImage getRealImage() {
		return myImage;
	}

	public String getURI() {
		return myImage != null ? myImage.getURI() : "image proxy";
	}

	@Override
	public final synchronized void synchronize(Synchronizer synchronizer) {
		if (myImage == null) {
			myImage = retrieveRealImage(synchronizer);
			setSynchronized();
		}
	}

	@Override
	public final void synchronizeFast() {
		setSynchronized();
	}

	protected abstract ZLImage retrieveRealImage(Synchronizer synchronizer);
}
