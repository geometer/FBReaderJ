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

#include <ZLUnicodeUtil.h>

#include "HtmlMetainfoReader.h"

#include "../../library/Book.h"

HtmlMetainfoReader::HtmlMetainfoReader(Book &book, ReadType readType) : 
	HtmlReader(book.encoding()), myBook(book), myReadType(readType) {
}

bool HtmlMetainfoReader::tagHandler(const HtmlReader::HtmlTag &tag) {
	if (tag.Name == "body") {
		return false;
	} else if ((myReadType & TAGS) == TAGS && tag.Name == "dc:subject") {
		myReadTags = tag.Start;
		if (!tag.Start && !myBuffer.empty()) {
			myBook.addTag(myBuffer);
			myBuffer.erase();
		}
	} else if ((myReadType & TITLE) == TITLE && tag.Name == "dc:title") {
		myReadTitle = tag.Start;
		if (!tag.Start && !myBuffer.empty()) {
			myBook.setTitle(myBuffer);
			myBuffer.erase();
		}
	} else if ((myReadType & AUTHOR) == AUTHOR && tag.Name == "dc:creator") {
		if (tag.Start) {
			const std::string *role = tag.find("role");
			if (role != 0 && ZLUnicodeUtil::toLower(*role) == "aut") {
				if (!myBuffer.empty()) {
					myBuffer += ", ";
				}
				myReadAuthor = true;
			}
		} else {
			myReadAuthor = false;
			if (!myBuffer.empty()) {
				myBook.addAuthor(myBuffer);
			}
			myBuffer.erase();
		}
	}
	return true;
}

void HtmlMetainfoReader::startDocumentHandler() {
	myReadAuthor = false;
	myReadTitle = false;
	myReadTags = false;
}

void HtmlMetainfoReader::endDocumentHandler() {
}

bool HtmlMetainfoReader::characterDataHandler(const char *text, size_t len, bool convert) {
	if (myReadTitle || myReadAuthor || myReadTags) {
		if (convert) {
			myConverter->convert(myBuffer, text, text + len);
		} else {
			myBuffer.append(text, len);
		}
	}
	return true;
}
