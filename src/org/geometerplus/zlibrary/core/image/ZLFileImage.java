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

package org.geometerplus.zlibrary.core.image;

import java.io.*;

import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.*;

public class ZLFileImage implements ZLStreamImage {
	public static final String SCHEME = "imagefile";

	public static final String ENCODING_NONE = "";
	public static final String ENCODING_HEX = "hex";
	public static final String ENCODING_BASE64 = "base64";

	public static ZLFileImage byUrlPath(String urlPath) {
		try {
			final String[] data = urlPath.split("\000");
			int count = Integer.parseInt(data[2]);
			int[] offsets = new int[count];
			int[] lengths = new int[count];
			for (int i = 0; i < count; ++i) {
				offsets[i] = Integer.parseInt(data[3 + i]);
				lengths[i] = Integer.parseInt(data[3 + count + i]);
			}
			return new ZLFileImage(
				ZLFile.createFileByPath(data[0]),
				data[1],
				offsets,
				lengths,
				null
			);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private final ZLFile myFile;
	private final String myEncoding;
	private final int[] myOffsets;
	private final int[] myLengths;
	private final FileEncryptionInfo myEncryptionInfo;

	public ZLFileImage(ZLFile file, String encoding, int[] offsets, int[] lengths, FileEncryptionInfo encryptionInfo) {
		myFile = file;
		myEncoding = encoding != null ? encoding : ENCODING_NONE;
		myOffsets = offsets;
		myLengths = lengths;
		myEncryptionInfo = encryptionInfo;
	}

	public ZLFileImage(ZLFile file, String encoding, int offset, int length) {
		this(file, encoding, new int[] { offset }, new int[] { length }, null);
	}

	public ZLFileImage(ZLFile file) {
		this(file, ENCODING_NONE, 0, (int)file.size());
	}

	public String getURI() {
		String result = SCHEME + "://" + myFile.getPath() + "\000" + myEncoding + "\000" + myOffsets.length;
		for (int offset : myOffsets) {
			result += "\000" + offset;
		}
		for (int length : myLengths) {
			result += "\000" + length;
		}
		return result;
	}

	private InputStream baseInputStream() throws IOException {
		if (myOffsets.length == 1) {
			final int offset = myOffsets[0];
			final int length = myLengths[0];
			return new SliceInputStream(myFile.getInputStream(), offset, length != 0 ? length : Integer.MAX_VALUE);
		} else {
			final InputStream[] streams = new InputStream[myOffsets.length];
			for (int i = 0; i < myOffsets.length; ++i) {
				final int offset = myOffsets[i];
				final int length = myLengths[i];
				streams[i] = new SliceInputStream(myFile.getInputStream(), offset, length != 0 ? length : Integer.MAX_VALUE);
			}
			return new MergedInputStream(streams);
		}
	}

	@Override
	public InputStream inputStream() {
		try {
			if (myEncryptionInfo != null) {
				return null;
			}

			InputStream stream = baseInputStream();
			if (ENCODING_NONE.equals(myEncoding)) {
				return stream;
			} else if (ENCODING_HEX.equals(myEncoding)) {
				return new HexInputStream(stream);
			} else if (ENCODING_BASE64.equals(myEncoding)) {
				stream = new Base64InputStream(stream);
				final int len = (int)stream.skip(stream.available());
				stream.close();
				return new SliceInputStream(new Base64InputStream(baseInputStream()), 0, len);
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
