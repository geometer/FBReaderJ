/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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
import java.net.*;

import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

import org.geometerplus.fbreader.Paths;


public final class NetworkImage extends ZLSingleImage {

	public static final String MIME_PNG = "image/png";
	public static final String MIME_JPEG = "image/jpeg";

	public final String Url;
	private volatile boolean mySynchronized;

	// mimeType string MUST be interned
	public NetworkImage(String url, String mimeType) {
		super(mimeType);
		Url = url;
		new File(Paths.networkCacheDirectory()).mkdirs();
	}

	private static final String TOESCAPE = "<>:\"|?*\\";

	// mimeType string MUST be interned
	public static String makeImageFileName(String url, String mimeType) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (java.net.URISyntaxException ex) {
			return null;
		}

		String host = uri.getHost();

		StringBuilder path = new StringBuilder(host);
		if (host.startsWith("www.")) {
			path.delete(0, 4);
		}
		path.insert(0, File.separator);
		path.insert(0, Paths.networkCacheDirectory());

		int index = path.length();

		path.append(uri.getPath());

		int nameIndex = index;
		while (index < path.length()) {
			char ch = path.charAt(index);
			if (TOESCAPE.indexOf(ch) != -1) {
				path.setCharAt(index, '_');
			}
			if (ch == '/') {
				if (index + 1 == path.length()) {
					path.deleteCharAt(index);
				} else {
					path.setCharAt(index, '_');
					nameIndex = index + 1;
				}
			}
			++index;
		}

		String ext = null;
		if (mimeType == MIME_PNG) {
			ext = ".png";
		} else if (mimeType == MIME_JPEG) {
			if (path.length() > 5 && path.substring(path.length() - 5).equals(".jpeg")) {
				ext = ".jpeg";
			} else {
				ext = ".jpg";
			}
		}

		if (ext == null) {
			int j = path.lastIndexOf(".");
			if (j > nameIndex) {
				ext = path.substring(j);
				path.delete(j, path.length());
			} else {
				ext = "";
			}
		} else if (path.length() > ext.length() && path.substring(path.length() - ext.length()).equals(ext)) {
			path.delete(path.length() - ext.length(), path.length());
		}

		String query = uri.getQuery();
		if (query != null) {
			index = 0;
			while (index < query.length()) {
				int j = query.indexOf("&", index);
				if (j == -1) {
					j = query.length();
				}
				String param = query.substring(index, j);
				if (!param.startsWith("username=")
					&& !param.startsWith("password=")
					&& !param.endsWith("=")) {
					int k = path.length();
					path.append("_").append(param);
					while (k < path.length()) {
						char ch = path.charAt(k);
						if (TOESCAPE.indexOf(ch) != -1 || ch == '/') {
							path.setCharAt(k, '_');
						}
						++k;
					}
				}
				index = j + 1;
			}
		}
		return path.append(ext).toString();
	}

	public String getFileName() {
		return makeImageFileName(Url, mimeType());
	}

	public boolean isSynchronized() {
		return mySynchronized;
	}

	public void synchronize() {
		synchronizeInternal(false);
	}

	public void synchronizeFast() {
		synchronizeInternal(true);
	}

	private final void synchronizeInternal(boolean doFast) {
		if (mySynchronized) {
			return;
		}
		try {
			final String fileName = getFileName();
			if (fileName == null) {
				// TODO: error message ???
				return;
			}
			final int index = fileName.lastIndexOf(File.separator);
			if (index != -1) {
				final String dir = fileName.substring(0, index);
				final File dirFile = new File(dir);
				if (!dirFile.exists() && !dirFile.mkdirs()) {
					// TODO: error message ???
					return;
				}
				if (!dirFile.exists() || !dirFile.isDirectory()) {
					// TODO: error message ???
					return;
				}
			}
			final File imageFile = new File(fileName);
			if (imageFile.exists()) {
				final long diff = System.currentTimeMillis() - imageFile.lastModified();
				final long valid = 7 * 24 * 60 * 60 * 1000; // one week in milliseconds; FIXME: hardcoded const
				if (diff >= 0 && diff <= valid) {
					return;
				}
				imageFile.delete();
			}
			if (doFast) {
				return;
			}

			ZLNetworkManager.Instance().downloadToFile(Url, imageFile);
		} finally {
			mySynchronized = true;
		}
	}

	@Override
	public byte [] byteData() {
		if (!mySynchronized) {
			return null;
		}
		final String fileName = getFileName();
		if (fileName == null) {
			return null;
		}
		final File imageFile = new File(fileName);
		if (!imageFile.exists()) {
			return null;
		}
		try {
			final byte[] data = new byte[(int)imageFile.length()];
			final FileInputStream stream = new FileInputStream(imageFile);
			stream.read(data);
			stream.close();
			return data;
		} catch (IOException e) {
			return null;
		}
	}

}
