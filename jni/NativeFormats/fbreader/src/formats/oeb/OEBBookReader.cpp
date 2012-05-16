/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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
#include "XHTMLImageFinder.h"
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

static const std::string COVER = "cover";
static const std::string COVER_IMAGE = "other.ms-coverimage-standard";

bool OEBBookReader::isOPFTag(const std::string &expected, const std::string &tag) const {
	return expected == tag || testTag(ZLXMLNamespace::OpenPackagingFormat, expected, tag);
}

void OEBBookReader::startElementHandler(const char *tag, const char **xmlattributes) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);

	switch (myState) {
		case READ_NONE:
			if (isOPFTag(MANIFEST, tagString)) {
				myState = READ_MANIFEST;
			} else if (isOPFTag(SPINE, tagString)) {
				const char *toc = attributeValue(xmlattributes, "toc");
				if (toc != 0) {
					myNCXTOCFileName = myIdToHref[toc];
				}
				myState = READ_SPINE;
			} else if (isOPFTag(GUIDE, tagString)) {
				myState = READ_GUIDE;
			} else if (isOPFTag(TOUR, tagString)) {
				myState = READ_TOUR;
			}
			break;
		case READ_MANIFEST:
			if (isOPFTag(ITEM, tagString)) {
				const char *id = attributeValue(xmlattributes, "id");
				const char *href = attributeValue(xmlattributes, "href");
				if (id != 0 && href != 0) {
					myIdToHref[id] = MiscUtil::decodeHtmlURL(href);
				}
			}
			break;
		case READ_SPINE:
			if (isOPFTag(ITEMREF, tagString)) {
				const char *id = attributeValue(xmlattributes, "idref");
				if (id != 0) {
					const std::string &fileName = myIdToHref[id];
					if (!fileName.empty()) {
						myHtmlFileNames.push_back(fileName);
					}
				}
			}
			break;
		case READ_GUIDE:
			if (isOPFTag(REFERENCE, tagString)) {
				const char *type = attributeValue(xmlattributes, "type");
				const char *title = attributeValue(xmlattributes, "title");
				const char *href = attributeValue(xmlattributes, "href");
				if (href != 0) {
					const std::string reference = MiscUtil::decodeHtmlURL(href);
					if (title != 0) {
						myGuideTOC.push_back(std::make_pair(std::string(title), reference));
					}
					if (type != 0) {
						if (COVER == type) {
							ZLFile imageFile(myFilePrefix + reference);
							myCoverFileName = imageFile.path();
							const std::string imageName = imageFile.name(false);
							shared_ptr<const ZLImage> image = XHTMLImageFinder().readImage(imageFile);
							if (!image.isNull()) {
								myModelReader.setMainTextModel();
								myModelReader.addImageReference(imageName, (short)0, true);
								myModelReader.addImage(imageName, image);
								myModelReader.insertEndOfSectionParagraph();
							} else {
								myCoverFileName.erase();
							}
						} else if (COVER_IMAGE == type) {
							ZLFile imageFile(myFilePrefix + reference);
							myCoverFileName = imageFile.path();
							const std::string imageName = imageFile.name(false);
							myModelReader.setMainTextModel();
							myModelReader.addImageReference(imageName, 0, true);
							myModelReader.addImage(imageName, new ZLFileImage(imageFile, "", 0));
							myModelReader.insertEndOfSectionParagraph();
						}
					}
				}
			}
			break;
		case READ_TOUR:
			if (isOPFTag(SITE, tagString)) {
				const char *title = attributeValue(xmlattributes, "title");
				const char *href = attributeValue(xmlattributes, "href");
				if ((title != 0) && (href != 0)) {
					myTourTOC.push_back(std::make_pair(title, MiscUtil::decodeHtmlURL(href)));
				}
			}
			break;
	}
}

void OEBBookReader::endElementHandler(const char *tag) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);

	switch (myState) {
		case READ_MANIFEST:
			if (isOPFTag(MANIFEST, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_SPINE:
			if (isOPFTag(SPINE, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_GUIDE:
			if (isOPFTag(GUIDE, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_TOUR:
			if (isOPFTag(TOUR, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_NONE:
			break;
	}
}

bool OEBBookReader::readBook(const ZLFile &file) {
	myFilePrefix = MiscUtil::htmlDirectoryPrefix(file.path());

	myIdToHref.clear();
	myHtmlFileNames.clear();
	myNCXTOCFileName.erase();
	myCoverFileName.erase();
	myTourTOC.clear();
	myGuideTOC.clear();
	myState = READ_NONE;

	if (!readDocument(file)) {
		return false;
	}

	myModelReader.setMainTextModel();
	myModelReader.pushKind(REGULAR);

	XHTMLReader xhtmlReader(myModelReader);
	bool firstFile = true;
	for (std::vector<std::string>::const_iterator it = myHtmlFileNames.begin(); it != myHtmlFileNames.end(); ++it) {
		const ZLFile xhtmlFile(myFilePrefix + *it);
		if (firstFile && myCoverFileName == xhtmlFile.path()) {
			continue;
		}
		if (!firstFile) {
			myModelReader.insertEndOfSectionParagraph();
		}
		xhtmlReader.readFile(xhtmlFile, *it);
		firstFile = false;
	}

	generateTOC(xhtmlReader);

	return true;
}

void OEBBookReader::generateTOC(const XHTMLReader &xhtmlReader) {
	if (!myNCXTOCFileName.empty()) {
		NCXReader ncxReader(myModelReader);
		if (ncxReader.readDocument(ZLFile(myFilePrefix + myNCXTOCFileName))) {
			const std::map<int,NCXReader::NavPoint> navigationMap = ncxReader.navigationMap();
			if (!navigationMap.empty()) {
				size_t level = 0;
				for (std::map<int,NCXReader::NavPoint>::const_iterator it = navigationMap.begin(); it != navigationMap.end(); ++it) {
					const NCXReader::NavPoint &point = it->second;
					int index = myModelReader.model().label(xhtmlReader.normalizedReference(point.ContentHRef)).ParagraphNumber;
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

const std::vector<std::string> &OEBBookReader::externalDTDs() const {
	return EntityFilesCollector::Instance().externalDTDs("xhtml");
}
