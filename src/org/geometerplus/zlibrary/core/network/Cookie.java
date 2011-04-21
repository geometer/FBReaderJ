/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.network;

import java.util.*;
import java.text.*;

import java.net.URI;

public class Cookie {
	private static DateFormat ourDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");

	public static Cookie create(URI uri, String cookieString) {
		if (uri == null || cookieString == null) {
			return null;
		}
		final String host = uri.getHost();
		if (host == null) {
			return null;
		}
		final String[] parts = cookieString.split(";");
		if (parts.length == 0) {
			return null;
		}

		final String mainPart = parts[0].trim();
		int index = mainPart.indexOf('=');
		if (index == -1) {
			return null;
		}
		final String name = mainPart.substring(0, index).trim();
		final String value = mainPart.substring(index + 1).trim();
		if ("".equals(name) || "".equals(value)) {
			return null;
		}
		
		boolean secure = false;
		boolean discard = false;
		String path = "/";
		Date dateOfExpiration = null;
		Set<Integer> ports = null;

		for (int i = 1; i < parts.length; ++i) {
			final String p = parts[i].trim();
			if ("secure".equalsIgnoreCase(p)) {
				secure = true;
				continue;
			}
			if ("discard".equalsIgnoreCase(p)) {
				discard = true;
				continue;
			}
			index = p.indexOf('=');
			if (index == -1) {
				continue;
			}
			final String pName = p.substring(0, index).trim();
			String pValue = p.substring(index + 1).trim();
			if (pValue.length() > 1 &&
				pValue.charAt(0) == '"' &&
				pValue.charAt(pValue.length() - 1) == '"') {
				pValue = pValue.substring(1, pValue.length() - 2);
			}
			if ("expires".equalsIgnoreCase(pName)) {
				try {
					dateOfExpiration = ourDateFormat.parse(pValue);
				} catch (ParseException e) {
				}
			} else if ("max-age".equalsIgnoreCase(pName)) {
				try {
					final int seconds = Integer.parseInt(pValue);
					dateOfExpiration = new Date(System.currentTimeMillis() + seconds * 1000);
				} catch (NumberFormatException e) {
				}
			} else if ("domain".equalsIgnoreCase(pName)) {
				if (!pValue.startsWith(".") || !host.endsWith(pValue)) {
					return null;
				}
			} else if ("path".equalsIgnoreCase(pName)) {
				path = pValue;
			} else if ("port".equalsIgnoreCase(pName)) {
				ports = new HashSet<Integer>();
				for (String s : pValue.split(" ")) {
					try {
						ports.add(Integer.parseInt(s));
					} catch (NumberFormatException e) {
					}
				}
			}
		}

		return new Cookie(name, value, host, path, ports, dateOfExpiration, secure, discard);
	}

	public final String Name;
	public final String Value;

	public final String Host;
	public final String Path;
	public final Set<Integer> Ports;
	public final Date DateOfExpiration;
	public final boolean Secure;
	public final boolean Discard;

	public Cookie(String name, String value, String host, String path, Set<Integer> ports, Date dateOfExpiration, boolean secure, boolean discard) {
		Name = name;
		Value = value;
		Host = host;
		Path = path;
		Ports = ports;
		DateOfExpiration = dateOfExpiration;
		Secure = secure;
		Discard = discard;
	}

	boolean isApplicable(URI uri) {
		final String scheme = uri.getScheme();
		if (Secure && !"https".equalsIgnoreCase(scheme)) {
			return false;
		}
		if (!Host.equalsIgnoreCase(uri.getHost())) {
			return false;
		}
		if (Ports != null) {
			int port = uri.getPort();
			if (port == -1) {
				if ("http".equalsIgnoreCase(scheme)) {
					port = 80;
				} else if ("https".equalsIgnoreCase(scheme)) {
					port = 443;
				}
			}
			if (!Ports.contains(port)) {
				return false;
			}
		}
		final String path = uri.getPath();
		if (path == null || !path.startsWith(Path)) {
			return false;
		}
		if (DateOfExpiration != null && DateOfExpiration.before(new Date())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Cookie)) {
			return false;
		}
		final Cookie c = (Cookie)o;
		return
			Host.equals(c.Host) &&
			Path.equals(c.Path) &&
			Name.equals(c.Name) &&
			((Ports == null && c.Ports == null) || Ports.equals(c.Ports)) &&
			Secure == c.Secure &&
			Discard == c.Discard;
	}

	@Override
	public int hashCode() {
		return Host.hashCode() + Path.hashCode() + Name.hashCode();
	}
}
