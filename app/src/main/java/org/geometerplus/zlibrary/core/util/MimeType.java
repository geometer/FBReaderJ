/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.fbreader.util.ComparisonUtil;

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
	public static final MimeType APP_ZIP = get("application/zip");
	public static final MimeType APP_RAR = get("application/x-rar-compressed");
	// unofficial, http://en.wikipedia.org/wiki/EPUB
	public static final MimeType APP_EPUB_ZIP = get("application/epub+zip");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_EPUB = get("application/epub");
	// ???
	public static final MimeType APP_MOBIPOCKET = get("application/x-mobipocket-ebook");
	// ???
	//public static final MimeType APP_MOBI = get("application/mobi");
	// unofficial, used by Calibre server
	public static final MimeType APP_FB2 = get("application/fb2");
	// ???
	public static final MimeType APP_XFB2 = get("application/x-fb2");
	// ???
	public static final MimeType APP_FICTIONBOOK = get("application/x-fictionbook");
	// ???
	public static final MimeType APP_FICTIONBOOK_XML = get("application/x-fictionbook+xml");
	// unofficial, used by FBReder book network
	public static final MimeType APP_FB2_XML = get("application/fb2+xml");
	// http://www.iana.org/assignments/media-types/application/index.html
	public static final MimeType APP_PDF = get("application/pdf");
	// ???
	public static final MimeType APP_XPDF = get("application/x-pdf");
	// ???
	public static final MimeType TEXT_PDF = get("text/pdf");
	// ???
	public static final MimeType APP_VND_PDF = get("application/vnd.pdf");
	// http://www.iana.org/assignments/media-types/application/index.html
	public static final MimeType APP_RTF = get("application/rtf");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_TXT = get("application/txt");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_DJVU = get("application/djvu");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_HTML = get("application/html");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_HTMLHTM = get("application/html+htm");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_DOC = get("application/doc");
	// http://www.iana.org/assignments/media-types/application/index.html
	public static final MimeType APP_MSWORD = get("application/msword");
	// unofficial, used by data.fbreader.org LitRes catalog & FBReader book nework
	public static final MimeType APP_FB2_ZIP = get("application/fb2+zip");
	// http://www.iana.org/assignments/media-types/application/index.html
	public static final MimeType APP_ATOM_XML = get("application/atom+xml");
	public static final MimeType APP_ATOM_XML_ENTRY = get("application/atom+xml;type=entry");
	public static final MimeType OPDS = get("application/atom+xml;profile=opds");
	// http://tools.ietf.org/id/draft-nottingham-rss-media-type-00.txt
	public static final MimeType APP_RSS_XML = get("application/rss+xml");
	// ???
	public static final MimeType APP_OPENSEARCHDESCRIPTION = get("application/opensearchdescription+xml");
	// unofficial, used by data.fbreader.org LitRes catalog
	public static final MimeType APP_LITRES = get("application/litres+xml");
	//???
	public static final MimeType APP_CBZ = get("application/x-cbz");
	public static final MimeType APP_CBR = get("application/x-cbr");

	// MIME types / text
	// ???
	public static final MimeType TEXT_XML = get("text/xml");
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_HTML = get("text/html");
	// ???
	public static final MimeType TEXT_XHTML = get("text/xhtml");
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_PLAIN = get("text/plain");
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_RTF = get("text/rtf");
	// unofficial, used by Calibre OPDS server
	public static final MimeType TEXT_FB2 = get("text/fb2+xml");

	// MIME images
	public static final String IMAGE_PREFIX = "image/";
	// http://www.iana.org/assignments/media-types/image/index.html
	public static final MimeType IMAGE_PNG = get("image/png");
	// http://www.iana.org/assignments/media-types/image/index.html
	public static final MimeType IMAGE_JPEG = get("image/jpeg");
	// ???
	public static final MimeType IMAGE_AUTO = get("image/auto");
	// ???
	public static final MimeType IMAGE_PALM = get("image/palm");
	// http://www.iana.org/assignments/media-types/image/index.html
	public static final MimeType IMAGE_VND_DJVU = get("image/vnd.djvu");
	// ???
	public static final MimeType IMAGE_XDJVU = get("image/x-djvu");

	// video
	public static final MimeType VIDEO_MP4 = get("video/mp4");
	public static final MimeType VIDEO_WEBM = get("video/webm");
	public static final MimeType VIDEO_OGG = get("video/ogg");

	public static final MimeType UNKNOWN = get("*/*");
	public static final MimeType NULL = new MimeType(null, null);

	public static final List<MimeType> TYPES_VIDEO
		 = Arrays.asList(VIDEO_WEBM, VIDEO_OGG, VIDEO_MP4);

	public static final List<MimeType> TYPES_FB2
		 = Arrays.asList(APP_FICTIONBOOK, APP_FICTIONBOOK_XML, APP_FB2, APP_XFB2, APP_FB2_XML, TEXT_FB2);
	public static final List<MimeType> TYPES_EPUB
		 = Arrays.asList(APP_EPUB_ZIP, APP_EPUB);
	public static final List<MimeType> TYPES_MOBIPOCKET
		 = Arrays.asList(APP_MOBIPOCKET);
	public static final List<MimeType> TYPES_TXT
		 = Arrays.asList(TEXT_PLAIN, APP_TXT);
	public static final List<MimeType> TYPES_RTF
		 = Arrays.asList(APP_RTF, TEXT_RTF);
	public static final List<MimeType> TYPES_HTML
		 = Arrays.asList(TEXT_HTML, APP_HTML, APP_HTMLHTM);
	public static final List<MimeType> TYPES_PDF
		 = Arrays.asList(APP_PDF, APP_XPDF, TEXT_PDF, APP_VND_PDF);
	public static final List<MimeType> TYPES_DJVU
		 = Arrays.asList(IMAGE_VND_DJVU, IMAGE_XDJVU, APP_DJVU);
	public static final List<MimeType> TYPES_COMIC_BOOK
		 = Arrays.asList(APP_CBZ, APP_CBR);
	public static final List<MimeType> TYPES_DOC
		 = Arrays.asList(APP_MSWORD, APP_DOC);
	public static final List<MimeType> TYPES_FB2_ZIP
		 = Arrays.asList(APP_FB2_ZIP);

	public final String Name;

	private final Map<String,String> myParameters;

	private MimeType(String name, Map<String,String> parameters) {
		Name = name;
		myParameters = parameters;
	}

	public MimeType clean() {
		if (myParameters == null) {
			return this;
		}
		return get(Name);
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
			ComparisonUtil.equal(Name, type.Name) &&
			MiscUtil.mapsEquals(myParameters, type.myParameters);
	}

	public boolean weakEquals(MimeType type) {
		return ComparisonUtil.equal(Name, type.Name);
	}

	@Override
	public int hashCode() {
		return ComparisonUtil.hashCode(Name);
	}

	@Override
	public String toString() {
		if (myParameters == null) {
			return Name;
		}

		final StringBuilder buffer = new StringBuilder(Name);
		for (Map.Entry<String,String> entry : myParameters.entrySet()) {
			buffer.append(';');
			buffer.append(entry.getKey());
			buffer.append('=');
			buffer.append(entry.getValue());
		}
		return buffer.toString();
	}
}
