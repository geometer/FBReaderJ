/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.HashMap;
import java.util.Map;

class BearerAuthenticationException extends RuntimeException {
	public final Map<String,String> Params = new HashMap<String,String>();

	BearerAuthenticationException(String challenge) {
		super("Authentication failed");
		if (challenge != null && "bearer".equalsIgnoreCase(challenge.substring(0, 6))) {
			for (String param : challenge.substring(6).split(",")) {
				final int index = param.indexOf("=");
				if (index != -1) {
					final String key = param.substring(0, index).trim();
					String value = param.substring(index + 1).trim();
					final int len = value.length();
					if (len > 1 && value.charAt(0) == '"' && value.charAt(len - 1) == '"') {
						value = value.substring(1, len - 1);
					}
					Params.put(key, value);
				}
			}
		}
	}
}
