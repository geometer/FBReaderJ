/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <ZLStringUtil.h>
#include <ZLXMLNamespace.h>

#include "OPFReader.h"
#include "../util/EntityFilesCollector.h"

OPFReader::OPFReader() {
}

bool OPFReader::testOPFTag(const std::string &expected, const std::string &tag) const {
	return
		expected == tag ||
		testTag(ZLXMLNamespace::OpenPackagingFormat, expected, tag);
}

bool OPFReader::testDCTag(const std::string &expected, const std::string &tag) const {
	return
		testTag(ZLXMLNamespace::DublinCore, expected, tag) ||
		testTag(ZLXMLNamespace::DublinCoreLegacy, expected, tag);
}

bool OPFReader::isNSName(const std::string &fullName, const std::string &shortName, const std::string &fullNSId) const {
	const int prefixLength = fullName.length() - shortName.length() - 1;
	if (prefixLength <= 0 ||
			fullName[prefixLength] != ':' ||
			!ZLStringUtil::stringEndsWith(fullName, shortName)) {
		return false;
	}
	const std::map<std::string,std::string> &namespaceMap = namespaces();
	std::map<std::string,std::string>::const_iterator iter =
		namespaceMap.find(fullName.substr(0, prefixLength));
	return iter != namespaceMap.end() && iter->second == fullNSId;
}

bool OPFReader::isMetadataTag(const std::string &tagName) {
	static const std::string METADATA = "metadata";
	static const std::string DC_METADATA = "dc-metadata";

	return
		testTag(ZLXMLNamespace::OpenPackagingFormat, METADATA, tagName) ||
		DC_METADATA == tagName;
}

bool OPFReader::processNamespaces() const {
	return true;
}

const std::vector<std::string> &OPFReader::externalDTDs() const {
	return EntityFilesCollector::xhtmlDTDs();
}
