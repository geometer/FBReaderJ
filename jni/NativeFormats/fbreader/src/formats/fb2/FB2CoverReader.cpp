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

#include <ZLFileImage.h>

#include "FB2CoverReader.h"

#include "../../library/Book.h"

FB2CoverReader::FB2CoverReader(const ZLFile &file) : myFile(file) {
}

shared_ptr<const ZLImage> FB2CoverReader::readCover() {
	myReadCoverPage = false;
	myLookForImage = false;
	myImageId.erase();
	myImageStart = -1;

	readDocument(myFile);

	return myImage;
}

bool FB2CoverReader::processNamespaces() const {
	return true;
}

void FB2CoverReader::startElementHandler(int tag, const char **attributes) {
	switch (tag) {
		case _COVERPAGE:
			myReadCoverPage = true;
			break;
		case _IMAGE:
			if (myReadCoverPage) {
				const char *ref = attributeValue(attributes, myHrefPredicate);
				if (ref != 0 && *ref == '#' && *(ref + 1) != '\0') {
					myImageId = ref + 1;
				}
			}
			break;
		case _BINARY:
		{
			const char *id = attributeValue(attributes, "id");
			const char *contentType = attributeValue(attributes, "content-type");
			if (id != 0 && contentType != 0 && myImageId == id) {
				myLookForImage = true;
			}
		}
	}
}

void FB2CoverReader::endElementHandler(int tag) {
	switch (tag) {
		case _COVERPAGE:
			myReadCoverPage = false;
			break;
		case _DESCRIPTION:
			if (myImageId.empty()) {
				interrupt();
			}
			break;
		case _BINARY:
			if (!myImageId.empty() && myImageStart >= 0) {
				myImage = new ZLFileImage(myFile, "base64", myImageStart, getCurrentPosition() - myImageStart);
				interrupt();
			}
			break;
	}
}

void FB2CoverReader::characterDataHandler(const char *text, std::size_t len) {
	if (len > 0 && myLookForImage) {
		myImageStart = getCurrentPosition();
		myLookForImage = false;
	}
}
