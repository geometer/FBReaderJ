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

package org.geometerplus.zlibrary.core.constants;

public interface XMLNamespaces {
	String DublinCore = "http://purl.org/dc/elements/1.1/";
	String DublinCoreLegacy = "http://purl.org/metadata/dublin_core";
	String XLink = "http://www.w3.org/1999/xlink";
	String OpenPackagingFormat = "http://www.idpf.org/2007/opf";

	String Atom = "http://www.w3.org/2005/Atom";
	String Opds = "http://opds-spec.org/2010/catalog";
	String DublinCoreTerms = "http://purl.org/dc/terms/";
	String DublinCoreSyndication = "http://purl.org/syndication/thread/1.0";
	String OpenSearch = "http://a9.com/-/spec/opensearch/1.1/";
	String CalibreMetadata = "http://calibre.kovidgoyal.net/2009/metadata";

	String FBReaderCatalogMetadata = "http://data.fbreader.org/catalog/metadata/";

	String MarlinEpub = "http://marlin-drm.com/epub";
	String XMLEncryption = "http://www.w3.org/2001/04/xmlenc#";
	String XMLDigitalSignature = "http://www.w3.org/2000/09/xmldsig#";
	String EpubContainer = "urn:oasis:names:tc:opendocument:xmlns:container";
}
