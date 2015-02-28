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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geometerplus.zlibrary.core.util.MimeType;

class OpenSearchDescription {
	public static OpenSearchDescription createDefault(String tmpl, MimeType mime) {
		return new OpenSearchDescription(tmpl, -1, -1, mime);
	}

	public final String Template;
	public final int IndexOffset;
	public final int PageOffset;
	public final MimeType Mime;

	public final int ItemsPerPage = 20;

	OpenSearchDescription(String tmpl, int indexOffset, int pageOffset, MimeType mime) {
		Template = tmpl;
		IndexOffset = indexOffset;
		PageOffset = pageOffset;
		Mime = mime;
	}

	public boolean isValid() {
		return makeQuery("") != null;
	}

	// searchTerms -- an HTML-encoded string
	public String makeQuery(String searchTerms) {
		final StringBuffer query = new StringBuffer();
		final Matcher m = Pattern.compile("\\{([^}]*)\\}").matcher(Template);
		while (m.find()) {
			String name = m.group(1);
			if (name == null || name.length() == 0 || name.contains(":")) {
				return null;
			}
			final boolean optional = name.endsWith("?");
			if (optional) {
				name = name.substring(0, name.length() - 1);
			}
			name = name.intern();
			if (name == "searchTerms") {
				m.appendReplacement(query, searchTerms);
			} else if (name == "count") {
				m.appendReplacement(query, String.valueOf(ItemsPerPage));
			} else if (optional) {
				m.appendReplacement(query, "");
			} else if (name == "startIndex") {
				if (IndexOffset > 0) {
					m.appendReplacement(query, String.valueOf(IndexOffset));
				} else {
					return null;
				}
			} else if (name == "startPage") {
				if (PageOffset > 0) {
					m.appendReplacement(query, String.valueOf(PageOffset));
				} else {
					return null;
				}
			} else if (name == "language") {
				m.appendReplacement(query, "*");
			} else if (name == "inputEncoding" || name == "outputEncoding") {
				m.appendReplacement(query, "UTF-8");
			} else {
				return null;
			}
		}
		m.appendTail(query);
		return query.toString();
	}
}
