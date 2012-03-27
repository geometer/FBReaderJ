/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.*;

public class ZLFileImage extends ZLSingleImage {
	public static final String SCHEME = "imagefile";

	public static final String ENCODING_NONE = "";
	public static final String ENCODING_HEX = "hex";
	public static final String ENCODING_BASE64 = "base64";

	public static ZLFileImage byUrlPath(String urlPath) {
		try {
			final String[] data = urlPath.split("\000");
			return new ZLFileImage(
				MimeType.IMAGE_AUTO,
				ZLFile.createFileByPath(data[0]),
				data[1],
				Integer.parseInt(data[2]),
				Integer.parseInt(data[3])
			);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private final ZLFile myFile;
	private final String myEncoding;
	private final int myOffset;
	private final int myLength;
	
	public ZLFileImage(String mimeType, ZLFile file, String encoding, int offset, int length) {
		this(MimeType.get(mimeType), file, encoding, offset, length);
	}

	public ZLFileImage(MimeType mimeType, ZLFile file, String encoding, int offset, int length) {
		super(mimeType);
		myFile = file;
		myEncoding = encoding;
		myOffset = offset;
		myLength = length;
	}

	public ZLFileImage(MimeType mimeType, ZLFile file) {
		this(mimeType, file, ENCODING_NONE, 0, (int)file.size());
	}

	public String getURI() {
		return SCHEME + "://" + myFile.getPath() + "\000" + myEncoding + "\000" + myOffset + "\000" + myLength;
	}

	@Override
	public InputStream inputStream() {
		try {
			final InputStream stream =
				new SliceInputStream(myFile.getInputStream(), myOffset, myLength);
			if (ENCODING_NONE.equals(myEncoding)) {
				return stream;
			} else if (ENCODING_HEX.equals(myEncoding)) {
				return new HexInputStream(stream);
			} else if (ENCODING_BASE64.equals(myEncoding)) {
				return new Base64InputStream(stream);
			} else {
				System.err.println("unsupported encoding: " + myEncoding);
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
