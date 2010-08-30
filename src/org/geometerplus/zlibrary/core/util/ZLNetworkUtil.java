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

package org.geometerplus.zlibrary.core.util;

import org.geometerplus.zlibrary.core.library.ZLibrary;

public class ZLNetworkUtil {

	public static String url(String baseUrl, String relativePath) {
		if (relativePath == null || relativePath.length() == 0) {
			return relativePath;
		}

		if (relativePath.contains("://")
				|| relativePath.matches("(?s)^[a-zA-Z][a-zA-Z0-9+-.]*:.*$")) { // matches Non-relative URI; see rfc3986
			return relativePath;
		}

		if (relativePath.charAt(0) == '/') {
			int index = baseUrl.indexOf("://");
			index = baseUrl.indexOf("/", index + 3);
			if (index == -1) {
				return baseUrl + relativePath;
			} else {
				return baseUrl.substring(0, index) + relativePath;
			}
		} else {
			int index = baseUrl.lastIndexOf('/'); // FIXME: if (baseUrl.charAt(baseUrl.length() - 1) == '/')
			while (index > 0 && relativePath.startsWith("../")) {
				index = baseUrl.lastIndexOf('/', index - 1);
				relativePath = relativePath.substring(3);
			}
			return baseUrl.substring(0, index + 1) + relativePath;
		}
	}

	private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	public static String htmlEncode(String stringToEncode) {
		if (stringToEncode == null) {
			return null;
		}
		StringBuilder encodedString = new StringBuilder();
		for (int i = 0; i < stringToEncode.length(); ++i) {
			final char ch = stringToEncode.charAt(i);
			if ((ch >= '0' && ch <= '9') ||
					(ch >= 'a' && ch <= 'z') ||
					(ch >= 'A' && ch <= 'Z') ||
					(ch == '.') ||
					(ch == '~') ||
					(ch == '-') ||
					(ch == '_')) {
				encodedString.append(ch);
			} else {
				try {
					byte[] bytes = String.valueOf(ch).getBytes("UTF-8");
					for (byte b: bytes) {
						encodedString.append('%').append(hexDigits[(b >>> 4) & 15]).append(hexDigits[b & 15]);
					}
				} catch (java.io.UnsupportedEncodingException ex) {
				}
			}
		}
		return encodedString.toString();
	}

	public static boolean hasParameter(String url, String name) {
		int index = url.lastIndexOf('/') + 1;
		if (index == -1 || index >= url.length()) {
			return false;
		}
		index = url.indexOf('?', index);
		while (index != -1) {
			int start = index + 1;
			if (start >= url.length()) {
				return false;
			}
			int eqIndex = url.indexOf('=', start);
			if (eqIndex == -1) {
				return false;
			}
			if (url.substring(start, eqIndex).equals(name)) {
				return true;
			}
			index = url.indexOf('&', start);
		}
		return false;
	}

	public static String appendParameter(String url, String name, String value) {
		if (name == null || value == null) {
			return url;
		}
		value = value.trim();
		if (value.length() == 0) {
			return url;
		}
		value = htmlEncode(value);
		int index = url.indexOf('?', url.lastIndexOf('/') + 1);
		char delimiter = (index == -1) ? '?' : '&';
		while (index != -1) {
			final int start = index + 1;
			final int eqIndex = url.indexOf('=', start);
			index = url.indexOf('&', start);
			if (eqIndex != -1 && url.substring(start, eqIndex).equals(name)) {
				final int end = (index != -1 ? index : url.length());
				if (url.substring(eqIndex + 1, end).equals(value)) {
					return url;
				} else {
					return new StringBuilder(url).replace(eqIndex + 1, end, value).toString();
				}
			}
		}
		return new StringBuilder(url).append(delimiter).append(name).append('=').append(value).toString();
	}

	public static String hostFromUrl(String url) {
		String host = url;
		int index = host.indexOf("://");
		if (index != -1) {
			host = host.substring(index + 3);
		}
		index = host.indexOf("/");
		if (index != -1) {
			host = host.substring(0, index);
		}
		return host;
	}

	public static String getUserAgent() {
		return "FBReader/" + ZLibrary.Instance().getVersionName() + "(java)";
	}

	public static String filterMimeType(String mime) {
		if (mime == null) {
			return null;
		}
		final int index = mime.indexOf(';');
		if (index != -1) {
			return mime.substring(0, index).intern();
		}
		return mime.intern();
	}
}
