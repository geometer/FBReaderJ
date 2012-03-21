/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

public final class MimeType {
	private static Map<String,MimeType> ourSimpleTypesMap = new HashMap<String,MimeType>();

	public static MimeType get(String text) {
		if (text == null) {
			return NULL;
		}

		final String[] items = text.split(";");
		if (items.length == 0) {
			return NULL;
		}

		final String name = items[0].intern();
		Map<String,String> parameters = null;
		for (int i = 1; i < items.length; ++i) {
			final String[] pair = items[i].split("=");
			if (pair.length == 2) {
				if (parameters == null) {
					parameters = new TreeMap<String,String>();
				}
				parameters.put(pair[0].trim(), pair[1].trim());
			}
		}

		if (parameters == null) {
			MimeType type = ourSimpleTypesMap.get(name);
			if (type == null) {
				type = new MimeType(name, null);
				ourSimpleTypesMap.put(name, type);
			}
			return type;
		}

		return new MimeType(name, parameters);
	}

	// MIME types / application
	// ???
	public static final MimeType APP_EPUB = get("application/epub+zip");
	// ???
	public static final MimeType APP_MOBI = get("application/x-mobipocket-ebook");
	// RFC 3778
	public static final MimeType APP_PDF = get("application/pdf");
	// ???
	public static final MimeType APP_XPDF = get("application/x-pdf");
	// ???
	public static final MimeType APP_RTF = get("application/rtf");
	// ???
	public static final MimeType APP_MSWORD = get("application/msword");
	// ???
	public static final MimeType APP_ATOM = get("application/atom+xml");
    // used in data.fbreader.org LitRes catalog
	public static final MimeType APP_FB2ZIP = get("application/fb2+zip");
	// ???
	public static final MimeType APP_OPENSEARCHDESCRIPTION = get("application/opensearchdescription+xml");
    // used in data.fbreader.org LitRes catalog
	public static final MimeType APP_LITRES = get("application/litres+xml");

	// MIME types / text
	// RFC 2854
	public static final MimeType TEXT_HTML = get("text/html");
	// ???
	public static final MimeType TEXT_XHTML = get("text/xhtml");
	// RFC 2046, RFC 3676
	public static final MimeType TEXT_PLAIN = get("text/plain");
	// ???
	public static final MimeType TEXT_RTF = get("text/rtf");
	// used in Calibre server
	public static final MimeType TEXT_FB2 = get("text/fb2+xml");

	// MIME images
	public static final String IMAGE_PREFIX = "image/";
	// RFC 2083
	public static final MimeType IMAGE_PNG = get("image/png");
	// RFC 2045, RFC 2046
	public static final MimeType IMAGE_JPEG = get("image/jpeg");
	// ???
	public static final MimeType IMAGE_AUTO = get("image/auto");
	// ???
	public static final MimeType IMAGE_PALM = get("image/palm");
	// ???
	public static final MimeType IMAGE_VND_DJVU = get("image/vnd.djvu");
	// ???
	public static final MimeType IMAGE_XDJVU = get("image/x-djvu");

	public static final MimeType NULL = new MimeType(null, null);

	public static final List<MimeType> TYPES_FB2 = Collections.unmodifiableList(Arrays.asList(APP_FICTIONBOOK, APP_FB2, APP_FICTIONBOOK_XML, TEXT_FB2));
	public static final List<MimeType> TYPES_EPUB = Collections.unmodifiableList(Arrays.asList(APP_EPUB_ZIP, APP_EPUB));
	public static final List<MimeType> TYPES_MOBIPOCKET = Collections.unmodifiableList(Arrays.asList(APP_MOBIPOCKET, APP_MOBI));
	public static final List<MimeType> TYPES_TXT = Collections.unmodifiableList(Arrays.asList(TEXT_PLAIN, APP_TXT));
	public static final List<MimeType> TYPES_RTF = Collections.unmodifiableList(Arrays.asList(APP_RTF, TEXT_RTF));
	public static final List<MimeType> TYPES_HTML = Collections.unmodifiableList(Arrays.asList(TEXT_HTML, APP_HTML));
	public static final List<MimeType> TYPES_PDF = Collections.unmodifiableList(Arrays.asList(APP_PDF, APP_XPDF));
	public static final List<MimeType> TYPES_DJVU = Collections.unmodifiableList(Arrays.asList(IMAGE_VND_DJVU, APP_DJVU, IMAGE_XDJVU));
	public static final List<MimeType> TYPES_DOC = Collections.unmodifiableList(Arrays.asList(APP_MSWORD, APP_DOC));

	public final String Name;

	private final Map<String,String> myParameters;

	private MimeType(String name, Map<String,String> parameters) {
		Name = name;
		myParameters = parameters;
	}

	public String getParameter(String key) {
		return myParameters != null ? myParameters.get(key) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MimeType)) {
			return false;
		}
		final MimeType type = (MimeType)o;
		return
			ZLMiscUtil.equals(Name, type.Name) &&
			ZLMiscUtil.mapsEquals(myParameters, type.myParameters);
	}

	public boolean weakEquals(MimeType type) {
		return ZLMiscUtil.equals(Name, type.Name);
	}

	@Override
	public int hashCode() {
		return ZLMiscUtil.hashCode(Name);
	}
}
