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

#include <map>

#include <ZLFile.h>
#include <ZLXMLReader.h>
#include <ZLUnicodeUtil.h>

#include "OEBTextStream.h"
#include "../util/MiscUtil.h"
#include "../util/XMLTextStream.h"

class XHTMLFilesCollector : public ZLXMLReader {

public:
	XHTMLFilesCollector(std::vector<std::string> &xhtmlFileNames);

private:
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);

private:
	std::vector<std::string> &myXHTMLFileNames;
	std::map<std::string,std::string> myIdToHref;
	enum {
		READ_NONE,
		READ_MANIFEST,
		READ_SPINE
	} myState;
};

XHTMLFilesCollector::XHTMLFilesCollector(std::vector<std::string> &xhtmlFileNames) : myXHTMLFileNames(xhtmlFileNames), myState(READ_NONE) {
}

static const std::string MANIFEST = "manifest";
static const std::string SPINE = "spine";
static const std::string ITEM = "item";
static const std::string ITEMREF = "itemref";

void XHTMLFilesCollector::startElementHandler(const char *tag, const char **xmlattributes) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (MANIFEST == tagString) {
		myState = READ_MANIFEST;
	} else if (SPINE == tagString) {
		myState = READ_SPINE;
	} else if ((myState == READ_MANIFEST) && (ITEM == tagString)) {
		const char *id = attributeValue(xmlattributes, "id");
		const char *href = attributeValue(xmlattributes, "href");
		if ((id != 0) && (href != 0)) {
			myIdToHref[id] = href;
		}
	} else if ((myState == READ_SPINE) && (ITEMREF == tagString)) {
		const char *id = attributeValue(xmlattributes, "idref");
		if (id != 0) {
			const std::string &fileName = myIdToHref[id];
			if (!fileName.empty()) {
				myXHTMLFileNames.push_back(fileName);
			}
		}
	}
}

void XHTMLFilesCollector::endElementHandler(const char *tag) {
	if (SPINE == ZLUnicodeUtil::toLower(tag)) {
		interrupt();
	}
}

OEBTextStream::OEBTextStream(const ZLFile &opfFile) {
	myFilePrefix = MiscUtil::htmlDirectoryPrefix(opfFile.path());
	XHTMLFilesCollector(myXHTMLFileNames).readDocument(opfFile);
}

void OEBTextStream::resetToStart() {
	myIndex = 0;
}

shared_ptr<ZLInputStream> OEBTextStream::nextStream() {
	if (myIndex >= myXHTMLFileNames.size()) {
		return 0;
	}
	ZLFile xhtmlFile(myFilePrefix + myXHTMLFileNames[myIndex++]);
	return new XMLTextStream(xhtmlFile.inputStream(), "body");
}
