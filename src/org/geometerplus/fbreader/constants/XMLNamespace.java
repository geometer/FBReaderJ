/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.constants;

public interface XMLNamespace {
	String DublinCorePrefix = "http://purl.org/dc/elements";
	String DublinCoreLegacyPrefix = "http://purl.org/metadata/dublin_core";
	String XLink = "http://www.w3.org/1999/xlink";
	String OpenPackagingFormat = "http://www.idpf.org/2007/opf";

	String Atom = "http://www.w3.org/2005/Atom";
	String Opds = "http://opds-spec.org/2010/catalog";
	String DublinCoreTerms = "http://purl.org/dc/terms/";
	String OpenSearch = "http://a9.com/-/spec/opensearch/1.1/";
	String CalibreMetadata = "http://calibre.kovidgoyal.net/2009/metadata";

	String FBReaderCatalogMetadata = "http://data.fbreader.org/catalog/metadata/";
}
