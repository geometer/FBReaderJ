/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <ZLFile.h>
#include <ZLFileImage.h>
#include <ZLXMLNamespace.h>

#include "OEBCoverReader.h"
#include "XHTMLImageFinder.h"

#include "../util/MiscUtil.h"

OEBCoverReader::OEBCoverReader() {
}

shared_ptr<const ZLImage> OEBCoverReader::readCover(const ZLFile &file) {
	myPathPrefix = MiscUtil::htmlDirectoryPrefix(file.path());
	myReadState = READ_NOTHING;
	myImage.reset();
	myCoverXHTML.erase();
	readDocument(file);
	if (myImage.isNull() && !myCoverXHTML.empty()) {
		const ZLFile coverFile(myCoverXHTML);
		const std::string ext = coverFile.extension();
		if (ext == "gif" || ext == "jpeg" || ext == "jpg" || ext == "png") {
			myImage = new ZLFileImage(coverFile, "", 0);
		} else {
			myImage = XHTMLImageFinder().readImage(coverFile);
		}
	}
	return myImage;
}

static const std::string METADATA = "metadata";
static const std::string META = "meta";
static const std::string MANIFEST = "manifest";
static const std::string ITEM = "item";
static const std::string GUIDE = "guide";
static const std::string REFERENCE = "reference";
static const std::string COVER = "cover";
static const std::string COVER_IMAGE = "other.ms-coverimage-standard";

bool OEBCoverReader::processNamespaces() const {
	return true;
}

void OEBCoverReader::startElementHandler(const char *tag, const char **attributes) {
	switch (myReadState) {
		case READ_NOTHING:
			if (GUIDE == tag) {
				myReadState = READ_GUIDE;
			} else if (MANIFEST == tag) {
				myReadState = READ_MANIFEST;
			} else if (testTag(ZLXMLNamespace::OpenPackagingFormat, METADATA, tag)) {
				myReadState = READ_METADATA;
			}
			break;
		case READ_GUIDE:
			if (REFERENCE == tag) {
				const char *type = attributeValue(attributes, "type");
				if (type != 0) {
					if (COVER == type) {
						const char *href = attributeValue(attributes, "href");
						if (href != 0) {
							myCoverXHTML = myPathPrefix + MiscUtil::decodeHtmlURL(href);
							interrupt();
						}
					} else if (COVER_IMAGE == type) {
						createImage(attributeValue(attributes, "href"));
					}
				}
			}
			break;
		case READ_METADATA:
			if (testTag(ZLXMLNamespace::OpenPackagingFormat, META, tag)) {
				const char *name = attributeValue(attributes, "name");
				if (name != 0 && COVER == name) {
					const char *coverId = attributeValue(attributes, "content");
					if (coverId != 0) {
						myCoverId = coverId;
					}
				}
			}
			break;
		case READ_MANIFEST:
			if (ITEM == tag) {
				const char *href = attributeValue(attributes, "href");
				if (href == 0) {
					break;
				}
				const char *prop = attributeValue(attributes, "properties");
				if (prop != 0 && std::string("cover-image") == prop) {
					createImage(href);
					break;
				}
				const char *id = attributeValue(attributes, "id");
				if (id != 0 && !myCoverId.empty() && myCoverId == id) {
					createImage(href);
				}
			}
			break;
	}
}

void OEBCoverReader::createImage(const char *href) {
	if (href != 0) {
		myImage = new ZLFileImage(ZLFile(myPathPrefix + MiscUtil::decodeHtmlURL(href)), "", 0);
		interrupt();
	}
}

void OEBCoverReader::endElementHandler(const char *tag) {
	switch (myReadState) {
		case READ_NOTHING:
			break;
		case READ_GUIDE:
			if (GUIDE == tag) {
				myReadState = READ_NOTHING;
			}
			break;
		case READ_METADATA:
			if (testTag(ZLXMLNamespace::OpenPackagingFormat, METADATA, tag)) {
				myReadState = READ_NOTHING;
			}
			break;
		case READ_MANIFEST:
			if (MANIFEST == tag) {
				myReadState = READ_NOTHING;
			}
			break;
	}
}
