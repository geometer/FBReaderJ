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

import java.io.File;
import java.net.URI;

import org.geometerplus.fbreader.Paths;

public class BookReference {

	public interface Type {
		int UNKNOWN = 0; // Unknown reference type
		int DOWNLOAD_FULL = 1; // reference for download full version of the book
		int DOWNLOAD_FULL_CONDITIONAL = 2; // reference for download full version of the book, useful only when book is bought
		int DOWNLOAD_DEMO = 3; // reference for downloading demo version of the book
		int DOWNLOAD_FULL_OR_DEMO = 4; // reference for downloading unknown version of the book
		int BUY = 5; // reference for buying the book (useful only when authentication is supported)
		int BUY_IN_BROWSER = 6; // reference to the site page, when it is possible to buy the book
	}
	// resolvedReferenceType -- reference type without any ambiguity (for example, DOWNLOAD_FULL_OR_DEMO is ambiguous)

	public interface Format {
		int NONE = 0;
		int MOBIPOCKET = 1;
		int FB2_ZIP = 2;
		int EPUB = 3;
	}

	public final String URL;
	public final int BookFormat;
	public final int ReferenceType;

	public BookReference(String url, int format, int type) {
		URL = url;
		BookFormat = format;
		ReferenceType = type;
	}

	// returns clean URL without any account/user-specific parts
	public String cleanURL() {
		return URL;
	}

	private static final String TOESCAPE = "<>:\"|?*\\";

	public static String makeBookFileName(String url, int format, int resolvedReferenceType) {
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
		if (resolvedReferenceType == Type.DOWNLOAD_DEMO) {
			path.insert(0, "Demos");
			path.insert(0, File.separator);
		}
		path.insert(0, Paths.BooksDirectoryOption.getValue());

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

	public final String makeBookFileName(int resolvedReferenceType) {
		return makeBookFileName(cleanURL(), BookFormat, resolvedReferenceType);
	}

	public final String localCopyFileName(int resolvedReferenceType) {
		String fileName = makeBookFileName(resolvedReferenceType);
		if (fileName != null && new File(fileName).exists()) {
			return fileName;
		}
		return null;
	}

	public String toString() {
		return "BookReference[type=" + ReferenceType + ";format=" + BookFormat + ";URL=" + URL + "]";
	}
}
