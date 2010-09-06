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
	String REL_SUBSECTION = "subsection";

	// Entry level / acquisition links
	String REL_ACQUISITION_PREFIX = "http://opds-spec.org/acquisition";
	String REL_FBREADER_ACQUISITION_PREFIX = "http://data.fbreader.org/acquisition";
	String REL_ACQUISITION = "http://opds-spec.org/acquisition";
	String REL_ACQUISITION_OPEN = "http://opds-spec.org/acquisition/open-access";
	String REL_ACQUISITION_SAMPLE = "http://opds-spec.org/acquisition/sample";
	String REL_ACQUISITION_BUY = "http://opds-spec.org/acquisition/buy";
	//String REL_ACQUISITION_BORROW = "http://opds-spec.org/acquisition/borrow";
	//String REL_ACQUISITION_SUBSCRIBE = "http://opds-spec.org/acquisition/subscribe";
	String REL_ACQUISITION_CONDITIONAL = "http://data.fbreader.org/acquisition/conditional";
	String REL_ACQUISITION_SAMPLE_OR_FULL = "http://data.fbreader.org/acquisition/sampleOrFull";

	// Entry level / other
	String REL_IMAGE_PREFIX = "http://opds-spec.org/image";
	//String REL_IMAGE = "http://opds-spec.org/image";
	String REL_IMAGE_THUMBNAIL = "http://opds-spec.org/image/thumbnail";
	// FIXME: This relations have been removed from OPDS-1.0 standard. Use RelationAlias instead???
	String REL_COVER = "http://opds-spec.org/cover";
	String REL_THUMBNAIL = "http://opds-spec.org/thumbnail";

	// Entry level / OPDS Link Relations
	String REL_LINK_SIGN_IN = "http://data.fbreader.org/catalog/sign-in";
	String REL_LINK_SIGN_OUT = "http://data.fbreader.org/catalog/sign-out";
	String REL_LINK_SIGN_UP = "http://data.fbreader.org/catalog/sign-up";
	String REL_LINK_REFILL_ACCOUNT = "http://data.fbreader.org/catalog/refill-account";
	String REL_LINK_RECOVER_PASSWORD = "http://data.fbreader.org/catalog/recover-password";

	// Entry level / OPDS Link Conditions
	String REL_CONDITION_NEVER = "http://data.fbreader.org/condition/never";
	String REL_CONDITION_SIGNED_IN = "http://data.fbreader.org/condition/signed-in";

	// MIME types / application
	String MIME_APP_FB2ZIP = "application/fb2+zip";
	String MIME_APP_EPUB = "application/epub+zip";
	String MIME_APP_MOBI = "application/x-mobipocket-ebook";
	String MIME_APP_PDF = "application/pdf";
	String MIME_APP_ATOM = "application/atom+xml";

	// MIME type for Opensearch Description XML document
	String MIME_APP_OPENSEARCHDESCRIPTION = "application/opensearchdescription+xml";

	// a special MIME type for the litres OPDS catalog
	String MIME_APP_LITRES = "application/litres+xml";

	// MIME types / text
	String MIME_TEXT_HTML = "text/html";
}
