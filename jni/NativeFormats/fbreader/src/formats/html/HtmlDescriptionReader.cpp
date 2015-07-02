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

#include "HtmlDescriptionReader.h"

#include "../../library/Book.h"

HtmlDescriptionReader::HtmlDescriptionReader(Book &book) : HtmlReader(book.encoding()), myBook(book) {
	myBook.setTitle("");
}

void HtmlDescriptionReader::startDocumentHandler() {
	myReadTitle = false;
}

void HtmlDescriptionReader::endDocumentHandler() {
	if (!myBook.title().empty()) {
		const char *titleStart = myBook.title().data();
		const char *titleEnd = titleStart + myBook.title().length();
		std::string newTitle;
		myConverter->convert(newTitle, titleStart, titleEnd);
		myBook.setTitle(newTitle);
	}
}

bool HtmlDescriptionReader::tagHandler(const HtmlTag &tag) {
	if (tag.Name == "title") {
		if (myReadTitle && !tag.Start) {
			myBook.setTitle(myBuffer);
			myBuffer.erase();
		}
		myReadTitle = tag.Start && myBook.title().empty();
		return true;
	} else if (tag.Start && tag.Name == "meta") {
		std::vector<HtmlAttribute>::const_iterator it = tag.Attributes.begin();
		for (; it != tag.Attributes.end(); ++it) {
			if (it->Name == "content") {
				break;
			}
		}
		if (it != tag.Attributes.end()) {
			const std::string prefix = "charset=";
			std::size_t index = it->Value.find(prefix);
			if (index != std::string::npos) {
				std::string charset = it->Value.substr(index + prefix.length());
				index = charset.find(';');
				if (index != std::string::npos) {
					charset = charset.substr(0, index);
				}
				index = charset.find(' ');
				if (index != std::string::npos) {
					charset = charset.substr(0, index);
				}
				myBook.setEncoding(charset);
			}
		}
	}
	return tag.Name != "body";
}

bool HtmlDescriptionReader::characterDataHandler(const char *text, std::size_t len, bool) {
	if (myReadTitle) {
		myBuffer.append(text, len);
	}
	return true;
}
