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

package org.geometerplus.zlibrary.core.image;

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class ZLFileImage extends ZLSingleImage {
	private final ZLFile myFile;
	
	public ZLFileImage(String mimeType, String path) {
		this(mimeType, ZLFile.createFile(path));
	}

	public ZLFileImage(String mimeType, ZLFile file) {
		super(mimeType);
		myFile = file;
	}

	public byte [] byteData() {
		try {
			final InputStream stream = myFile.getInputStream();
			if (stream == null) {
				return new byte[0];
			}

			final ArrayList data = new ArrayList();
			byte[] buffer;
			int sizeOfBufferData;

			do {
				buffer = new byte[4096];
				sizeOfBufferData = stream.read(buffer);
				data.add(buffer);
			} while (sizeOfBufferData == 4096);
			final int dataSizeMinus1 = data.size() - 1;
			buffer = new byte[dataSizeMinus1 * 4096 + sizeOfBufferData];
			for (int i = 0; i < dataSizeMinus1; ++i) {
				System.arraycopy(data.get(i), 0, buffer, i * 4096, 4096);
			}
			System.arraycopy(data.get(dataSizeMinus1), 0, buffer, dataSizeMinus1 * 4096, sizeOfBufferData);
			stream.close();
			return buffer;
		} catch (IOException e) {
		}
		
		return new byte[0];
	}
}
