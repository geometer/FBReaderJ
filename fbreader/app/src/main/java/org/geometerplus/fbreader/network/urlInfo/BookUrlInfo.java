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

package org.geometerplus.fbreader.network.urlInfo;

import java.io.File;
import java.util.Arrays;

import android.net.Uri;

import org.geometerplus.zlibrary.core.filetypes.FileType;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.formats.PluginCollection;

// resolvedReferenceType -- reference type without any ambiguity (for example, DOWNLOAD_FULL_OR_DEMO is ambiguous)

public class BookUrlInfo extends UrlInfo {
	private static final long serialVersionUID = -893514485257788221L;

	public BookUrlInfo(Type type, String url, MimeType mime) {
		super(type, url, mime);
	}

	private static final String TOESCAPE = "<>:\"|?*\\";

	public static boolean isMimeSupported(MimeType mime, SystemInfo systemInfo) {
		if (mime == null) {
			return false;
		}
		final FileType type = FileTypeCollection.Instance.typeForMime(mime);
		if (type == null) {
			return false;
		}
		return PluginCollection.Instance(systemInfo).getPlugin(type) != null;
	}

	private static int mimePriority(MimeType mime) {
		if (mime == null) {
			return -1;
		} else if (MimeType.TYPES_MOBIPOCKET.contains(mime)) {
			return 1;
		} else if (MimeType.TYPES_FB2.contains(mime)) {
			return 2;
		} else if (MimeType.TYPES_FB2_ZIP.contains(mime)) {
			return 3;
		} else if (MimeType.TYPES_EPUB.contains(mime)) {
			return 4;
		}
		return 0;
	}

	public static boolean isMimeBetterThan(MimeType mime0, MimeType mime1) {
		return mimePriority(mime0) > mimePriority(mime1);
	}

	public static String makeBookFileName(String url, MimeType mime, Type resolvedReferenceType) {
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

		final int port = uri.getPort();
		if (port != -1) {
			path.append("_").append(port);
			path.insert(0, File.separator);
		}

		if (resolvedReferenceType == Type.BookDemo) {
			path.insert(0, "Demos");
			path.insert(0, File.separator);
		}
		path.insert(0, Paths.DownloadsDirectoryOption.getValue());

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

		final FileType type = mime != null ? FileTypeCollection.Instance.typeForMime(mime) : null;
		String ext = type != null ? "." + type.defaultExtension(mime) : null;

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
		return makeBookFileName(cleanUrl(), Mime, resolvedReferenceType);
	}

	public final String localCopyFileName(Type resolvedReferenceType) {
		String fileName = makeBookFileName(resolvedReferenceType);
		if (fileName != null && new File(fileName).exists()) {
			return fileName;
		}
		return null;
	}

	public String toString() {
		return "BookReference[type=" + InfoType + ";mime=" + Mime + ";URL=" + Url + "]";
	}
}
