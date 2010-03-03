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

package org.geometerplus.fbreader.network.opds;


public final class OPDSConstants {

	// Feed level
	//public static final String REL_BOOKSHELF = "http://opds-spec.org/bookshelf";
	//public static final String REL_SUBSCRIPTIONS = "http://opds-spec.org/subscriptions";

	// Entry level / acquisition links
	public static final String REL_ACQUISITION = "http://opds-spec.org/acquisition";
	//public static final String REL_ACQUISITION_BUY = "http://opds-spec.org/acquisition/buy";
	//public static final String REL_ACQUISITION_BORROW = "http://opds-spec.org/acquisition/borrow";
	//public static final String REL_ACQUISITION_SUBSCRIBE = "http://opds-spec.org/acquisition/subscribe";
	public static final String REL_ACQUISITION_SAMPLE = "http://opds-spec.org/acquisition/sample";

	// Entry level / other
	public static final String REL_COVER = "http://opds-spec.org/cover";
	public static final String REL_STANZA_COVER = "x-stanza-cover-image";
	public static final String REL_THUMBNAIL = "http://opds-spec.org/thumbnail";
	public static final String REL_STANZA_THUMBNAIL = "x-stanza-cover-image-thumbnail";

	// MIME types / MIME type for "Full Entry" atom:link element
	//public static final String MIME_OPDS_FULLENTRY = "application/atom+xml;type=entry";

	// MIME types / application
	public static final String MIME_APP_FB2ZIP = "application/fb2+zip";
	public static final String MIME_APP_EPUB = "application/epub+zip";
	public static final String MIME_APP_MOBI = "application/x-mobipocket-ebook";
	public static final String MIME_APP_PDF = "application/pdf";
	public static final String MIME_APP_ATOM = "application/atom+xml";

	// MIME types / image
	public static final String MIME_IMG_PNG = "image/png";
	public static final String MIME_IMG_JPEG = "image/jpeg";

	// MIME types / text
	public static final String MIME_TEXT_HTML = "text/html";
}
