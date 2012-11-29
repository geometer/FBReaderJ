/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.urlInfo;

import java.io.File;

import android.net.Uri;

import org.geometerplus.fbreader.Paths;

import org.geometerplus.zlibrary.core.util.MimeType;

// resolvedReferenceType -- reference type without any ambiguity (for example, DOWNLOAD_FULL_OR_DEMO is ambiguous)

public class BookUrlInfo extends UrlInfo {
	private static final long serialVersionUID = -893514485257788221L;

	public interface Format {
		int NONE = 0;
		int MOBIPOCKET = 1;
		int FB2 = 2;
		int FB2_ZIP = 3;
		int EPUB = 4;
	}

	public final int BookFormat;

	public BookUrlInfo(Type type, int format, String url, MimeType mime) {
		super(type, url, mime);
		BookFormat = format;
	}

	private static final String TOESCAPE = "<>:\"|?*\\";

	public static String makeBookFileName(String url, int format, Type resolvedReferenceType) {
		final Uri uri = Uri.parse(url);

		String host = uri.getHost();
		if (host == null) {
			host = "host.unknown";
		}

		final StringBuilder path = new StringBuilder(host);
		if (host.startsWith("www.")) {
			path.delete(0, 4);
		}
		path.insert(0, File.separator);
		if (resolvedReferenceType == Type.BookDemo) {
			path.insert(0, "Demos");
			path.insert(0, File.separator);
		}
		path.insert(0, Paths.mainBookDirectory());

		int index = path.length();
		final String uriPath = uri.getPath();
		if (uriPath != null) {
			path.append(uriPath);
		}
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
					path.setCharAt(index, File.separatorChar);
					nameIndex = index + 1;
				}
			}
			++index;
		}

		String ext = null;
		switch (format) {
			case Format.EPUB:
				ext = ".epub";
				break;
			case Format.MOBIPOCKET:
				ext = ".mobi";
				break;
			case Format.FB2:
				ext = ".fb2";
				break;
			case Format.FB2_ZIP:
				ext = ".fb2.zip";
				break;
		}

		if (ext == null) {
			int j = path.indexOf(".", nameIndex); // using not lastIndexOf to preserve extensions like `.fb2.zip`
			if (j != -1) {
				ext = path.substring(j);
				path.delete(j, path.length());
			} else {
				return null;
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

	// Url with no user-dependent info; is overridden in DecoratedBookUrlInfo
	public String cleanUrl() {
		return Url;
	}

	public final String makeBookFileName(Type resolvedReferenceType) {
		return makeBookFileName(cleanUrl(), BookFormat, resolvedReferenceType);
	}

	public final String localCopyFileName(Type resolvedReferenceType) {
		String fileName = makeBookFileName(resolvedReferenceType);
		if (fileName != null && new File(fileName).exists()) {
			return fileName;
		}
		return null;
	}

	public String toString() {
		return "BookReference[type=" + InfoType + ";format=" + BookFormat + ";URL=" + Url + "]";
	}
}
