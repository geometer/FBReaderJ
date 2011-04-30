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

package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.SliceInputStream;

public class PluckerFileImage extends ZLSingleImage {
	private final ZLFile myFile;
	private final int myOffset;
	private final int mySize;

	public PluckerFileImage(MimeType mimeType, final ZLFile file, final int offset, final int size) {
		super(mimeType);
		myFile = file;
		myOffset = offset;
		mySize = size;
	}

	public String getURI() {
		// TODO: implement
		return null;
	}

	@Override
	public InputStream inputStream() {
		try {
			return new SliceInputStream(myFile.getInputStream(), myOffset, mySize);
		} catch (IOException e) {
			return null;
		}
	}
}
