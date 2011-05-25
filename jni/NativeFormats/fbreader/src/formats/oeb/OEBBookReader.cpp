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

#include <algorithm>

#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>
#include <ZLFile.h>
#include <ZLFileImage.h>
#include <ZLXMLNamespace.h>

#include "OEBBookReader.h"
#include "NCXReader.h"
#include "../xhtml/XHTMLReader.h"
#include "../util/MiscUtil.h"
#include "../util/EntityFilesCollector.h"
#include "../../bookmodel/BookModel.h"

OEBBookReader::OEBBookReader(BookModel &model) : myModelReader(model) {
}

static const std::string MANIFEST = "manifest";
static const std::string SPINE = "spine";
static const std::string GUIDE = "guide";
static const std::string TOUR = "tour";
static const std::string SITE = "site";

static const std::string ITEM = "item";
static const std::string ITEMREF = "itemref";
static const std::string REFERENCE = "reference";

static const std::string COVER_IMAGE = "other.ms-coverimage-standard";

void OEBBookReader::startElementHandler(const char *tag, const char **xmlattributes) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (!myOPFSchemePrefix.empty() &&
			ZLStringUtil::stringStartsWith(tagString, myOPFSchemePrefix)) {
		tagString = tagString.substr(myOPFSchemePrefix.length());
	}
	if (MANIFEST == tagString) {
		myState = READ_MANIFEST;
	} else if (SPINE == tagString) {
		const char *toc = attributeValue(xmlattributes, "toc");
		if (toc != 0) {
			myNCXTOCFileName = myIdToHref[toc];
		}
		myState = READ_SPINE;
	} else if (GUIDE == tagString) {
		myState = READ_GUIDE;
	} else if (TOUR == tagString) {
		myState = READ_TOUR;
	} else if ((myState == READ_MANIFEST) && (ITEM == tagString)) {
		const char *id = attributeValue(xmlattributes, "id");
		const char *href = attributeValue(xmlattributes, "href");
		if ((id != 0) && (href != 0)) {
			myIdToHref[id] = MiscUtil::decodeHtmlURL(href);
		}
	} else if ((myState == READ_SPINE) && (ITEMREF == tagString)) {
		const char *id = attributeValue(xmlattributes, "idref");
		if (id != 0) {
			const std::string &fileName = myIdToHref[id];
			if (!fileName.empty()) {
				myHtmlFileNames.push_back(fileName);
			}
		}
	} else if ((myState == READ_GUIDE) && (REFERENCE == tagString)) {
		const char *type = attributeValue(xmlattributes, "type");
		const char *title = attributeValue(xmlattributes, "title");
		const char *href = attributeValue(xmlattributes, "href");
		if (href != 0) {
			const std::string reference = MiscUtil::decodeHtmlURL(href);
			if (title != 0) {
				myGuideTOC.push_back(std::make_pair(std::string(title), reference));
			}
			if ((type != 0) && (COVER_IMAGE == type)) {
				myModelReader.setMainTextModel();
				ZLFile imageFile(myFilePrefix + reference);
				const std::string imageName = imageFile.name(false);
				myModelReader.addImageReference(imageName);
				myModelReader.addImage(imageName, new ZLFileImage(imageFile, 0));
			}
		}
	} else if ((myState == READ_TOUR) && (SITE == tagString)) {
		const char *title = attributeValue(xmlattributes, "title");
		const char *href = attributeValue(xmlattributes, "href");
		if ((title != 0) && (href != 0)) {
			myTourTOC.push_back(std::make_pair(title, MiscUtil::decodeHtmlURL(href)));
		}
	}
}

void OEBBookReader::endElementHandler(const char *tag) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (!myOPFSchemePrefix.empty() &&
			ZLStringUtil::stringStartsWith(tagString, myOPFSchemePrefix)) {
		tagString = tagString.substr(myOPFSchemePrefix.length());
	}
	if ((MANIFEST == tagString) || (SPINE == tagString) || (GUIDE == tagString) || (TOUR == tagString)) {
		myState = READ_NONE;
	}
}

bool OEBBookReader::readBook(const ZLFile &file) {
	myFilePrefix = MiscUtil::htmlDirectoryPrefix(file.path());

	myIdToHref.clear();
	myHtmlFileNames.clear();
	myNCXTOCFileName.erase();
	myTourTOC.clear();
	myGuideTOC.clear();
	myState = READ_NONE;

	if (!readDocument(file)) {
		return false;
	}

	myModelReader.setMainTextModel();
	myModelReader.pushKind(REGULAR);

	for (std::vector<std::string>::const_iterator it = myHtmlFileNames.begin(); it != myHtmlFileNames.end(); ++it) {
		if (it != myHtmlFileNames.begin()) {
			myModelReader.insertEndOfSectionParagraph();
		}
		XHTMLReader xhtmlReader(myModelReader);
		xhtmlReader.readFile(ZLFile(myFilePrefix + *it), *it);
	}

	generateTOC();

	return true;
}

void OEBBookReader::generateTOC() {
	if (!myNCXTOCFileName.empty()) {
		NCXReader ncxReader(myModelReader);
		if (ncxReader.readDocument(ZLFile(myFilePrefix + myNCXTOCFileName))) {
			const std::map<int,NCXReader::NavPoint> navigationMap = ncxReader.navigationMap();
			if (!navigationMap.empty()) {
				size_t level = 0;
				for (std::map<int,NCXReader::NavPoint>::const_iterator it = navigationMap.begin(); it != navigationMap.end(); ++it) {
					const NCXReader::NavPoint &point = it->second;
					int index = myModelReader.model().label(point.ContentHRef).ParagraphNumber;
					while (level > point.Level) {
						myModelReader.endContentsParagraph();
						--level;
					}
					while (++level <= point.Level) {
						myModelReader.beginContentsParagraph(-2);
						myModelReader.addContentsData("...");
					}
					myModelReader.beginContentsParagraph(index);
					myModelReader.addContentsData(point.Text);
				}
				while (level > 0) {
					myModelReader.endContentsParagraph();
					--level;
				}
				return;
			}
		}
	}

	std::vector<std::pair<std::string,std::string> > &toc = myTourTOC.empty() ? myGuideTOC : myTourTOC;
	for (std::vector<std::pair<std::string,std::string> >::const_iterator it = toc.begin(); it != toc.end(); ++it) {
		int index = myModelReader.model().label(it->second).ParagraphNumber;
		if (index != -1) {
			myModelReader.beginContentsParagraph(index);
			myModelReader.addContentsData(it->first);
			myModelReader.endContentsParagraph();
		}
	}
}

bool OEBBookReader::processNamespaces() const {
	return true;
}

void OEBBookReader::namespaceListChangedHandler() {
	const std::map<std::string,std::string> &namespaceMap = namespaces();
	std::map<std::string,std::string>::const_iterator iter = namespaceMap.begin();
	for (; iter != namespaceMap.end(); ++iter) {
		if (iter->second == ZLXMLNamespace::OpenPackagingFormat) {
			break;
		}
	}
	if (iter != namespaceMap.end()) {
		myOPFSchemePrefix = iter->first + ":";
	} else {
		myOPFSchemePrefix.erase();
	}
}

const std::vector<std::string> &OEBBookReader::externalDTDs() const {
	return EntityFilesCollector::Instance().externalDTDs("xhtml");
}
