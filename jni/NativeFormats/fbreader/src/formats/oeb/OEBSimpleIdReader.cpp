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
#include <ZLXMLNamespace.h>

#include "OEBSimpleIdReader.h"

void OEBSimpleIdReader::characterDataHandler(const char *text, std::size_t len) {
	switch (myReadState) {
		default:
			break;
		case READ_IDENTIFIER:
			myBuffer.append(text, len);
			break;
	}
}

void OEBSimpleIdReader::startElementHandler(const char *tag, const char **attributes) {
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
			if (testDCTag("identifier", tagString)) {
				myReadState = READ_IDENTIFIER;
			}
			break;
	}
}

void OEBSimpleIdReader::endElementHandler(const char *tag) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	switch (myReadState) {
		case READ_NONE:
			break;
		case READ_METADATA:
			if (isMetadataTag(tagString)) {
				myReadState = READ_NONE;
				interrupt();
				return;
			}
			break;
		case READ_IDENTIFIER:
			ZLUnicodeUtil::utf8Trim(myBuffer);
			if (!myBuffer.empty()) {
				if (!myPublicationId.empty()) {
					myPublicationId += " ";
				}
				myPublicationId += myBuffer;
				myBuffer.erase();
			}
			myReadState = READ_METADATA;
			break;
	}
}

std::string OEBSimpleIdReader::readId(const ZLFile &file) {
	myPublicationId.erase();
	myBuffer.erase();
	myReadState = READ_NONE;
	readDocument(file);
	return myPublicationId;
}
