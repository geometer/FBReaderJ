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

#include "XHTMLImageFinder.h"
#include "../util/MiscUtil.h"

static const std::string TAG_IMG = "img";
static const std::string TAG_IMAGE = "image";

shared_ptr<const ZLImage> XHTMLImageFinder::readImage(const ZLFile &file) {
	myImage.reset();
	myPathPrefix = MiscUtil::htmlDirectoryPrefix(file.path());
	readDocument(file);
	return myImage;
}

bool XHTMLImageFinder::processNamespaces() const {
	return true;
}

void XHTMLImageFinder::startElementHandler(const char *tag, const char **attributes) {
	const char *reference = 0;
	if (TAG_IMG == tag) {
		reference = attributeValue(attributes, "src");
	} else if (TAG_IMAGE == tag) {
		reference = attributeValue(
			attributes, FullNamePredicate(ZLXMLNamespace::XLink, "href")
		);
	}
	if (reference != 0) {
		myImage = new ZLFileImage(ZLFile(myPathPrefix + reference), "", 0);
		interrupt();
	}
}
