/*
 * Copyright (C) 2008-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __ZLXMLNAMESPACE_H__
#define __ZLXMLNAMESPACE_H__

#include <string>

class ZLXMLNamespace {

private:
	ZLXMLNamespace();

public:
	static const std::string DublinCore;
	static const std::string DublinCoreLegacy;
	static const std::string DublinCoreTerms;
	static const std::string XLink;
	static const std::string XHTML;
	static const std::string OpenPackagingFormat;
	static const std::string Atom;
	static const std::string OpenSearch;
	static const std::string CalibreMetadata;
	static const std::string Opds;
	static const std::string DaisyNCX;
	static const std::string Svg;
	static const std::string MarlinEpub;
	static const std::string XMLEncryption;
	static const std::string XMLDigitalSignature;
	static const std::string EpubContainer;
	static const std::string FBReaderXhtml;
};

#endif /* __ZLXMLNAMESPACE_H__ */
