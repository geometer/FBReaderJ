/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.xml;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public abstract class ZLXMLProcessor {
	public abstract void setBufferSize(int bufferSize);

	public abstract boolean read(ZLXMLReader xmlReader, InputStream stream);

	public boolean read(ZLXMLReader xmlReader, ZLFile file) {
		InputStream stream = null;
		try {
			stream = file.getInputStream();
		} catch (IOException e) {
		}
		if (stream == null) {
			return false;
		}
		boolean code = read(xmlReader, stream);
		try {
			stream.close();
		} catch (IOException e) {
		}
		return code;
	}
}
