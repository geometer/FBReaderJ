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

package org.geometerplus.fbreader.network;

import java.io.*;

import org.geometerplus.zlibrary.core.image.ZLBase64EncodedImage;
import org.geometerplus.zlibrary.core.util.MimeType;

final class Base64EncodedImage extends ZLBase64EncodedImage {
	private static final String ENCODED_SUFFIX = ".base64";

	private final MimeType myMimeType;
	private String myDecodedFileName;

	public Base64EncodedImage(NetworkLibrary library, String data, MimeType mimeType) {
		myMimeType = mimeType;

		final String dir = library.SystemInfo.networkCacheDirectory() + "/base64";
		new File(dir).mkdirs();

		myDecodedFileName = dir + File.separator + Integer.toHexString(data.hashCode());
		if (MimeType.IMAGE_PNG.equals(myMimeType)) {
			myDecodedFileName += ".png";
		} else if (MimeType.IMAGE_JPEG.equals(myMimeType)) {
			myDecodedFileName += ".jpg";
		}

		if (isCacheValid(new File(myDecodedFileName))) {
			return;
		}

		File file = new File(encodedFileName());
		if (isCacheValid(file)) {
			return;
		}
		try {
			final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			try {
				writer.write(data, 0, data.length());
			} finally {
				writer.close();
			}
		} catch (IOException e) {
		}
	}

	@Override
	protected boolean isCacheValid(File file) {
		if (file.exists()) {
			final long diff = System.currentTimeMillis() - file.lastModified();
			final long valid = 24 * 60 * 60 * 1000; // one day in milliseconds; FIXME: hardcoded const
			if (diff >= 0 && diff <= valid) {
				return true;
			}
			file.delete();
		}
		return false;
	}

	@Override
	protected String encodedFileName() {
		return myDecodedFileName + ENCODED_SUFFIX;
	}

	@Override
	protected String decodedFileName() {
		return myDecodedFileName;
	}
}
