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

#include <cstdlib>

#include <ZLUnicodeUtil.h>
#include <ZLLogger.h>
#include <ZLXMLNamespace.h>

#include "OEBMetaInfoReader.h"

#include "../../library/Book.h"

OEBMetaInfoReader::OEBMetaInfoReader(Book &book) : myBook(book) {
	myBook.removeAllAuthors();
	myBook.setTitle("");
	myBook.removeAllTags();
	myBook.removeAllUids();
}

static const std::string META = "meta";
static const std::string AUTHOR_ROLE = "aut";

void OEBMetaInfoReader::characterDataHandler(const char *text, std::size_t len) {
	switch (myReadState) {
		case READ_NONE:
		case READ_METADATA:
			break;
		case READ_AUTHOR:
		case READ_AUTHOR2:
		case READ_SUBJECT:
		case READ_LANGUAGE:
		case READ_TITLE:
		case READ_IDENTIFIER:
			myBuffer.append(text, len);
			break;
	}
}

void OEBMetaInfoReader::startElementHandler(const char *tag, const char **attributes) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	switch (myReadState) {
		default:
			break;
		case READ_NONE:
			if (isMetadataTag(tagString)) {
				myReadState = READ_METADATA;
			}
			break;
		case READ_METADATA:
			if (testDCTag("title", tagString)) {
				myReadState = READ_TITLE;
			} else if (testDCTag("creator", tagString)) {
				const char *role = attributeValue(attributes, "role");
				if (role == 0) {
					myReadState = READ_AUTHOR2;
				} else if (AUTHOR_ROLE == role) {
					myReadState = READ_AUTHOR;
				}
			} else if (testDCTag("subject", tagString)) {
				myReadState = READ_SUBJECT;
			} else if (testDCTag("language", tagString)) {
				myReadState = READ_LANGUAGE;
			} else if (testDCTag("identifier", tagString)) {
				myReadState = READ_IDENTIFIER;
				static const FullNamePredicate schemePredicate(ZLXMLNamespace::OpenPackagingFormat, "scheme");
				const char *scheme = attributeValue(attributes, schemePredicate);
				myIdentifierScheme = scheme != 0 ? scheme : "EPUB-NOSCHEME";
			} else if (testTag(ZLXMLNamespace::OpenPackagingFormat, META, tagString)) {
				const char *name = attributeValue(attributes, "name");
				const char *content = attributeValue(attributes, "content");
				if (name != 0 && content != 0) {
					std::string sName = name;
					if (sName == "calibre:series" || isNSName(sName, "series", ZLXMLNamespace::CalibreMetadata)) {
						myBook.setSeries(content, myBook.indexInSeries());
					} else if (sName == "calibre:series_index" || isNSName(sName, "series_index", ZLXMLNamespace::CalibreMetadata)) {
						myBook.setSeries(myBook.seriesTitle(), content);
					}
				}
			}
			break;
	}
}

void OEBMetaInfoReader::endElementHandler(const char *tag) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	ZLUnicodeUtil::utf8Trim(myBuffer);
	switch (myReadState) {
		case READ_NONE:
			return;
		case READ_METADATA:
			if (isMetadataTag(tagString)) {
				myReadState = READ_NONE;
				interrupt();
				return;
			}
			break;
		case READ_AUTHOR:
			if (!myBuffer.empty()) {
				myAuthorList.push_back(myBuffer);
			}
			break;
		case READ_AUTHOR2:
			if (!myBuffer.empty()) {
				myAuthorList2.push_back(myBuffer);
			}
			break;
		case READ_SUBJECT:
			if (!myBuffer.empty()) {
				myBook.addTag(myBuffer);
			}
			break;
		case READ_TITLE:
			if (!myBuffer.empty()) {
				myBook.setTitle(myBuffer);
			}
			break;
		case READ_LANGUAGE:
			if (!myBuffer.empty()) {
				int index = myBuffer.find('-');
				if (index >= 0) {
					myBuffer = myBuffer.substr(0, index);
				}
				index = myBuffer.find('_');
				if (index >= 0) {
					myBuffer = myBuffer.substr(0, index);
				}
				myBook.setLanguage(myBuffer);
			}
			break;
		case READ_IDENTIFIER:
			if (!myBuffer.empty()) {
				myBook.addUid(myIdentifierScheme, myBuffer);
			}
			break;
	}
	myBuffer.erase();
	myReadState = READ_METADATA;
}

bool OEBMetaInfoReader::readMetainfo(const ZLFile &file) {
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
