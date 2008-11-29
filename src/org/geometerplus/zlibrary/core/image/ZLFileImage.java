/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public class ZLFileImage extends ZLSingleImage {
	private final String myPath;
	
	public ZLFileImage(String mimeType, String path) {
		super(mimeType);
		myPath = path;
	}

	public byte [] byteData() {
		final InputStream stream = ZLibrary.Instance().getInputStream(myPath);
		if (stream == null) {
			return new byte[0];
		}
		final ArrayList data = new ArrayList();
		byte[] buffer;
		int sizeOfBufferData;
		try {
			do {
				buffer = new byte[4096];
				sizeOfBufferData = stream.read(buffer);
				data.add(buffer);
			} while (sizeOfBufferData == 4096);
			final int dataSizeMinusOne = data.size() - 1;
			buffer = new byte[dataSizeMinusOne * 4096 + sizeOfBufferData];
			for (int i = 0; i < dataSizeMinusOne; ++i) {
				System.arraycopy(data.get(i), 0, buffer, i * 4096, 4096);
			}
			System.arraycopy(data.get(dataSizeMinusOne), 0, buffer, dataSizeMinusOne * 4096, sizeOfBufferData);
			return buffer;
		} catch (IOException e) {
		}
		
		return new byte[0];
	}
}
