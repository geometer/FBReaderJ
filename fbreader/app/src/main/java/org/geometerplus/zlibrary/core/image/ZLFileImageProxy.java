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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class ZLFileImageProxy extends ZLImageSimpleProxy {
	protected final ZLFile File;
	private volatile ZLFileImage myImage;

	protected ZLFileImageProxy(ZLFile file) {
		File = file;
	}

	@Override
	public final ZLFileImage getRealImage() {
		return myImage;
	}

	@Override
	public String getURI() {
		return "cover:" + File.getPath();
	}

	@Override
	public final synchronized void synchronize() {
		if (myImage == null) {
			myImage = retrieveRealImage();
			setSynchronized();
		}
	}

	@Override
	public SourceType sourceType() {
		return SourceType.FILE;
	}

	@Override
	public String getId() {
		return File.getPath();
	}

	protected abstract ZLFileImage retrieveRealImage();
}
