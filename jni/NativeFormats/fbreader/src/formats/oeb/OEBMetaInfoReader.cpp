/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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
#include <ZLLogger.h>
#include <ZLXMLNamespace.h>

#include "OEBMetaInfoReader.h"
#include "../util/EntityFilesCollector.h"

#include "../../library/Book.h"

OEBMetaInfoReader::OEBMetaInfoReader(Book &book) : myBook(book) {
	myBook.removeAllAuthors();
	myBook.setTitle("");
	myBook.removeAllTags();
}

static const std::string METADATA = "metadata";
static const std::string DC_METADATA = "dc-metadata";
static const std::string TITLE_SUFFIX = ":title";
static const std::string AUTHOR_SUFFIX = ":creator";
static const std::string SUBJECT_SUFFIX = ":subject";
static const std::string LANGUAGE_SUFFIX = ":language";
static const std::string SERIES = "series";
static const std::string SERIES_INDEX = "series_index";
static const std::string META = "meta";
static const std::string AUTHOR_ROLE = "aut";

void OEBMetaInfoReader::characterDataHandler(const char *text, size_t len) {
	switch (myReadState) {
		case READ_NONE:
			break;
		case READ_AUTHOR:
		case READ_AUTHOR2:
		case READ_SUBJECT:
		case READ_LANGUAGE:
		case READ_TITLE:
			myBuffer.append(text, len);
			break;
	}
}

bool OEBMetaInfoReader::isDublinCoreNamespace(const std::string &nsId) const {
	const std::map<std::string,std::string> &namespaceMap = namespaces();
	std::map<std::string,std::string>::const_iterator iter = namespaces().find(nsId);
	return
		((iter != namespaceMap.end()) &&
		 (ZLStringUtil::stringStartsWith(iter->second, ZLXMLNamespace::DublinCorePrefix) ||
		  ZLStringUtil::stringStartsWith(iter->second, ZLXMLNamespace::DublinCoreLegacyPrefix)));
}

bool OEBMetaInfoReader::isNSName(const std::string &fullName, const std::string &shortName, const std::string &fullNSId) const {
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

void OEBMetaInfoReader::startElementHandler(const char *tag, const char **attributes) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (METADATA == tagString || DC_METADATA == tagString ||
			isNSName(tagString, METADATA, ZLXMLNamespace::OpenPackagingFormat)) {
		myDCMetadataTag = tagString;
		myReadMetaData = true;
	} else if (myReadMetaData) {
		if (ZLStringUtil::stringEndsWith(tagString, TITLE_SUFFIX)) {
			if (isDublinCoreNamespace(tagString.substr(0, tagString.length() - TITLE_SUFFIX.length()))) {
				myReadState = READ_TITLE;
			}
		} else if (ZLStringUtil::stringEndsWith(tagString, AUTHOR_SUFFIX)) {
			if (isDublinCoreNamespace(tagString.substr(0, tagString.length() - AUTHOR_SUFFIX.length()))) {
				const char *role = attributeValue(attributes, "role");
				if (role == 0) {
					myReadState = READ_AUTHOR2;
				} else if (AUTHOR_ROLE == role) {
					myReadState = READ_AUTHOR;
				}
			}
		} else if (ZLStringUtil::stringEndsWith(tagString, SUBJECT_SUFFIX)) {
			if (isDublinCoreNamespace(tagString.substr(0, tagString.length() - SUBJECT_SUFFIX.length()))) {
				myReadState = READ_SUBJECT;
			}
		} else if (ZLStringUtil::stringEndsWith(tagString, LANGUAGE_SUFFIX)) {
			if (isDublinCoreNamespace(tagString.substr(0, tagString.length() - LANGUAGE_SUFFIX.length()))) {
				myReadState = READ_LANGUAGE;
			}
		} else if (tagString == META) {
			const char *name = attributeValue(attributes, "name");
			const char *content = attributeValue(attributes, "content");
			if (name != 0 && content != 0) {
				std::string sName = name;
				if (isNSName(sName, SERIES, ZLXMLNamespace::CalibreMetadata)) {
					myBook.setSeries(content, myBook.indexInSeries());
				} else if (isNSName(sName, SERIES_INDEX, ZLXMLNamespace::CalibreMetadata)) {
					myBook.setSeries(myBook.seriesTitle(), atoi(content));
				}
			}
		}
	}
}

void OEBMetaInfoReader::endElementHandler(const char *tag) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (myDCMetadataTag == tagString) {
		interrupt();
	} else {
		ZLStringUtil::stripWhiteSpaces(myBuffer);
		if (!myBuffer.empty()) {
			if (myReadState == READ_AUTHOR) {
				myAuthorList.push_back(myBuffer);
			} else if (myReadState == READ_AUTHOR2) {
				myAuthorList2.push_back(myBuffer);
			} else if (myReadState == READ_SUBJECT) {
				myBook.addTag(myBuffer);
			} else if (myReadState == READ_TITLE) {
				myBook.setTitle(myBuffer);
			} else if (myReadState == READ_LANGUAGE) {
				int index = myBuffer.find('-');
				if (index >= 0) {
					myBuffer = myBuffer.substr(0, index);
				}
				index = myBuffer.find('_');
				if (index >= 0) {
					myBuffer = myBuffer.substr(0, index);
				}
				if (myBuffer == "cz") {
					myBuffer = "cs";
				}
				myBook.setLanguage(myBuffer);
			}
			myBuffer.erase();
		}
		myReadState = READ_NONE;
	}
}

bool OEBMetaInfoReader::processNamespaces() const {
	return true;
}

bool OEBMetaInfoReader::readMetaInfo(const ZLFile &file) {
	myReadMetaData = false;
	myReadState = READ_NONE;
	if (!readDocument(file)) {
		ZLLogger::Instance().println("epub", "Failure while reading info from " + file.path());
		return false;
	}

	if (!myAuthorList.empty()) {
		for (std::vector<std::string>::const_iterator it = myAuthorList.begin(); it != myAuthorList.end(); ++it) {
			myBook.addAuthor(*it);
		}
	} else {
		for (std::vector<std::string>::const_iterator it = myAuthorList2.begin(); it != myAuthorList2.end(); ++it) {
			myBook.addAuthor(*it);
		}
	}
	return true;
}

const std::vector<std::string> &OEBMetaInfoReader::externalDTDs() const {
	return EntityFilesCollector::Instance().externalDTDs("xhtml");
}
