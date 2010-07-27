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


interface OPDSConstants {

	// Feed level
	String REL_BOOKSHELF = "http://opds-spec.org/bookshelf";
	//String REL_SUBSCRIPTIONS = "http://opds-spec.org/subscriptions";

	// Entry level / catalog types
	String REL_CATALOG_AUTHOR = "http://data.fbreader.org/catalog/author";

	// Entry level / acquisition links
	String REL_ACQUISITION_PREFIX = "http://opds-spec.org/acquisition";
	String REL_ACQUISITION = "http://opds-spec.org/acquisition";
	String REL_ACQUISITION_SAMPLE = "http://opds-spec.org/acquisition/sample";
	String REL_ACQUISITION_BUY = "http://opds-spec.org/acquisition/buy";
	//String REL_ACQUISITION_BORROW = "http://opds-spec.org/acquisition/borrow";
	//String REL_ACQUISITION_SUBSCRIBE = "http://opds-spec.org/acquisition/subscribe";
	String REL_ACQUISITION_CONDITIONAL = "http://data.fbreader.org/acquisition/conditional";
	String REL_ACQUISITION_SAMPLE_OR_FULL = "http://data.fbreader.org/acquisition/sampleOrFull";

	// Entry level / other
	String REL_COVER = "http://opds-spec.org/cover";
	String REL_THUMBNAIL = "http://opds-spec.org/thumbnail";

	// MIME types / application
	String MIME_APP_FB2ZIP = "application/fb2+zip";
	String MIME_APP_EPUB = "application/epub+zip";
	String MIME_APP_MOBI = "application/x-mobipocket-ebook";
	String MIME_APP_PDF = "application/pdf";
	String MIME_APP_ATOM = "application/atom+xml";

	// a special MIME type for the litres OPDS catalog
	String MIME_APP_LITRES = "application/litres+xml";

	// MIME types / text
	String MIME_TEXT_HTML = "text/html";
}
