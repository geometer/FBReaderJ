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

package org.geometerplus.zlibrary.core.constants;

public interface MimeTypes {
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
	String MIME_TEXT_XHTML = "text/xhtml";
	String MIME_TEXT_PLAIN = "text/plain";

	// MIME images
	String MIME_IMAGE_PREFIX = "image/";
	String MIME_IMAGE_PNG = "image/png";
	String MIME_IMAGE_JPEG = "image/jpeg";
	String MIME_IMAGE_AUTO = "image/auto";
	String MIME_IMAGE_PALM = "image/palm";
}
