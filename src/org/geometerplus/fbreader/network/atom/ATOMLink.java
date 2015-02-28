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

package org.geometerplus.fbreader.network.atom;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class ATOMLink extends ATOMCommonAttributes {
	public static final String HREF = "href";
	public static final String REL = "rel";
	public static final String TYPE = "type";
	public static final String HREFLANG = "hreflang";
	public static final String TITLE = "title";
	public static final String LENGTH = "length";

	protected ATOMLink(ZLStringMap source) {
		super(source);
		readAttribute(HREF, source);
		readAttribute(REL, source);
		readAttribute(TYPE, source);
		readAttribute(HREFLANG, source);
		readAttribute(TITLE, source);
		readAttribute(LENGTH, source);
	}

	public final String getHref() {
		return getAttribute(HREF);
	}

	public final String getRel() {
		return getAttribute(REL);
	}

	public final String getType() {
		return getAttribute(TYPE);
	}

	public final String getHrefLang() {
		return getAttribute(HREFLANG);
	}

	public final String getTitle() {
		return getAttribute(TITLE);
	}

	public final String getLength() {
		return getAttribute(LENGTH);
	}
}
