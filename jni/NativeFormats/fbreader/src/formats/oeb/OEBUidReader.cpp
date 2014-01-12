/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#include <cstdlib>

#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>
#include <ZLXMLNamespace.h>

#include "OEBUidReader.h"
#include "../util/EntityFilesCollector.h"

#include "../../library/Book.h"

OEBUidReader::OEBUidReader(Book &book) : myBook(book) {
	myBook.removeAllUids();
}

static const std::string METADATA = "metadata";
static const std::string DC_METADATA = "dc-metadata";
static const std::string META = "meta";

void OEBUidReader::characterDataHandler(const char *text, std::size_t len) {
	switch (myReadState) {
		default:
			break;
		case READ_IDENTIFIER:
			myBuffer.append(text, len);
			break;
	}
}

bool OEBUidReader::testDCTag(const std::string &name, const std::string &tag) const {
	return
		testTag(ZLXMLNamespace::DublinCore, name, tag) ||
		testTag(ZLXMLNamespace::DublinCoreLegacy, name, tag);
}

bool OEBUidReader::isNSName(const std::string &fullName, const std::string &shortName, const std::string &fullNSId) const {
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

void OEBUidReader::startElementHandler(const char *tag, const char **attributes) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	switch (myReadState) {
		default:
			break;
		case READ_NONE:
			if (testTag(ZLXMLNamespace::OpenPackagingFormat, METADATA, tagString) ||
					DC_METADATA == tagString) {
				myReadState = READ_METADATA;
			}
			break;
		case READ_METADATA:
			if (testDCTag("identifier", tagString)) {
				myReadState = READ_IDENTIFIER;
				static const FullNamePredicate schemePredicate(ZLXMLNamespace::OpenPackagingFormat, "scheme");
				const char *scheme = attributeValue(attributes, schemePredicate);
				myIdentifierScheme = scheme != 0 ? scheme : "EPUB-NOSCHEME";
			}
			break;
	}
}

void OEBUidReader::endElementHandler(const char *tag) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	ZLUnicodeUtil::utf8Trim(myBuffer);
	switch (myReadState) {
		case READ_NONE:
			break;
		case READ_METADATA:
			if (testTag(ZLXMLNamespace::OpenPackagingFormat, METADATA, tagString) ||
		 			DC_METADATA == tagString) {
				interrupt();
				myReadState = READ_NONE;
				return;
			}
			break;
		case READ_IDENTIFIER:
			if (!myBuffer.empty()) {
				myBook.addUid(myIdentifierScheme, myBuffer);
			}
			myReadState = READ_METADATA;
			break;
	}
	myBuffer.erase();
}

bool OEBUidReader::processNamespaces() const {
	return true;
}

bool OEBUidReader::readUids(const ZLFile &file) {
	myReadState = READ_NONE;
	if (!readDocument(file)) {
		return false;
	}
	return true;
}

const std::vector<std::string> &OEBUidReader::externalDTDs() const {
	return EntityFilesCollector::Instance().externalDTDs("xhtml");
}
