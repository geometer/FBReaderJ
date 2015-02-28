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

package org.geometerplus.fbreader.network.opds;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

class URLRewritingRule {
	// rule types:
	public static final int ADD_URL_PARAMETER = 0;
	public static final int REWRITE = 1;
	public static final int UNKNOWN = 2;

	// apply values:
	public static final int APPLY_EXTERNAL = 1;
	public static final int APPLY_INTERNAL = 2;
	public static final int APPLY_ALWAYS = APPLY_EXTERNAL | APPLY_INTERNAL;

	private int myType = UNKNOWN;
	private int myApply = APPLY_ALWAYS;

	private final HashMap<String,String> myParameters = new HashMap<String,String>();

	public URLRewritingRule(ZLStringMap map) {
		for (int i = map.getSize() - 1; i >= 0; --i) {
			final String key = map.getKey(i);
			final String value = map.getValue(key);
			if ("type".equals(key)) {
				if ("addUrlParameter".equals(value)) {
					myType = ADD_URL_PARAMETER;
				} else if ("rewrite".equals(value)) {
					myType = REWRITE;
				}
			} else if ("apply".equals(key)) {
				if ("internal".equals(value)) {
					myApply = APPLY_INTERNAL;
				} else if ("external".equals(value)) {
					myApply = APPLY_EXTERNAL;
				}
			} else {
				myParameters.put(key, value);
			}
		}
	}

	public int whereToApply() {
		return myApply;
	}

	public String apply(String url) {
		switch (myType) {
			default:
				return url;
			case ADD_URL_PARAMETER:
			{
				final String name = myParameters.get("name");
				final String value = myParameters.get("value");
				if (name == null || value == null) {
					return url;
				}
				return ZLNetworkUtil.appendParameter(url, name, value);
			}
			case REWRITE:
			{
				final String pattern = myParameters.get("pattern");
				final String replacement = myParameters.get("replacement");
				if (pattern == null || replacement == null) {
					return url;
				}
				final Matcher matcher = Pattern.compile(pattern).matcher(url);
				if (matcher.matches()) {
					for (int i = matcher.groupCount(); i >= 1; --i) {
						url = replacement.replace("%" + i, matcher.group(1));
					}
				}
				return url;
			}
		}
	}
	/*
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof URLRewritingRule)) {
			return false;
		}
		final URLRewritingRule rule = (URLRewritingRule) o;
		if (Type != rule.Type
				|| Apply != rule.Apply
				|| !MiscUtil.equals(Name, rule.Name)
				|| !MiscUtil.equals(Value, rule.Value)) {
			return false;
		}
		return true;
	}
	*/
}
