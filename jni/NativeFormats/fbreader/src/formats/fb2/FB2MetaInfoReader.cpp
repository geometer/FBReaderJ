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

#include <cstdlib>

#include <ZLInputStream.h>
#include <ZLStringUtil.h>

#include "FB2MetaInfoReader.h"
#include "FB2TagManager.h"

#include "../../library/Book.h"

FB2MetaInfoReader::FB2MetaInfoReader(Book &book) : myBook(book) {
	myBook.removeAllAuthors();
	myBook.setTitle(std::string());
	myBook.setLanguage(std::string());
	myBook.removeAllTags();
}

void FB2MetaInfoReader::characterDataHandler(const char *text, size_t len) {
	switch (myReadState) {
		case READ_TITLE:
			myBuffer.append(text, len);
			break;
		case READ_LANGUAGE:
			myBuffer.append(text, len);
			break;
		case READ_AUTHOR_NAME_0:
			myAuthorNames[0].append(text, len);
			break;
		case READ_AUTHOR_NAME_1:
			myAuthorNames[1].append(text, len);
			break;
		case READ_AUTHOR_NAME_2:
			myAuthorNames[2].append(text, len);
			break;
		case READ_GENRE:
			myBuffer.append(text, len);
			break;
		default:
			break;
	}
}

void FB2MetaInfoReader::startElementHandler(int tag, const char **attributes) {
	switch (tag) {
		case _BODY:
			myReturnCode = true;
			interrupt();
			break;
		case _TITLE_INFO:
			myReadState = READ_SOMETHING;
			break;
		case _BOOK_TITLE:
			if (myReadState == READ_SOMETHING) {
				myReadState = READ_TITLE;
			}
			break;
		case _GENRE:
			if (myReadState == READ_SOMETHING) {
				myReadState = READ_GENRE;
			}
			break;
		case _AUTHOR:
			if (myReadState == READ_SOMETHING) {
				myReadState = READ_AUTHOR;
			}
			break;
		case _LANG:
			if (myReadState == READ_SOMETHING) {
				myReadState = READ_LANGUAGE;
			}
			break;
		case _FIRST_NAME:
			if (myReadState == READ_AUTHOR) {
				myReadState = READ_AUTHOR_NAME_0;
			}
			break;
		case _MIDDLE_NAME:
			if (myReadState == READ_AUTHOR) {
				myReadState = READ_AUTHOR_NAME_1;
			}
			break;
		case _LAST_NAME:
			if (myReadState == READ_AUTHOR) {
				myReadState = READ_AUTHOR_NAME_2;
			}
			break;
		case _SEQUENCE:
			if (myReadState == READ_SOMETHING) {
				const char *name = attributeValue(attributes, "name");
				if (name != 0) {
					std::string seriesTitle = name;
					ZLStringUtil::stripWhiteSpaces(seriesTitle);
					const char *number = attributeValue(attributes, "number");
					myBook.setSeries(seriesTitle, number != 0 ? std::string(number) : std::string());
				}
			}
			break;
		default:
			break;
	}
}

void FB2MetaInfoReader::endElementHandler(int tag) {
	switch (tag) {
		case _TITLE_INFO:
			myReadState = READ_NOTHING;
			break;
		case _BOOK_TITLE:
			if (myReadState == READ_TITLE) {
				myBook.setTitle(myBuffer);
				myBuffer.erase();
				myReadState = READ_SOMETHING;
			}
			break;
		case _GENRE:
			if (myReadState == READ_GENRE) {
				ZLStringUtil::stripWhiteSpaces(myBuffer);
				if (!myBuffer.empty()) {
					const std::vector<std::string> &tags =
						FB2TagManager::Instance().humanReadableTags(myBuffer);
					if (!tags.empty()) {
						for (std::vector<std::string>::const_iterator it = tags.begin(); it != tags.end(); ++it) {
							myBook.addTag(*it);
						}
					} else {
						myBook.addTag(myBuffer);
					}
					myBuffer.erase();
				}
				myReadState = READ_SOMETHING;
			}
			break;
		case _AUTHOR:
			if (myReadState == READ_AUTHOR) {
				ZLStringUtil::stripWhiteSpaces(myAuthorNames[0]);
				ZLStringUtil::stripWhiteSpaces(myAuthorNames[1]);
				ZLStringUtil::stripWhiteSpaces(myAuthorNames[2]);
				std::string fullName = myAuthorNames[0];
				if (!fullName.empty() && !myAuthorNames[1].empty()) {
					fullName += ' ';
				}
				fullName += myAuthorNames[1];
				if (!fullName.empty() && !myAuthorNames[2].empty()) {
					fullName += ' ';
				}
				fullName += myAuthorNames[2];
				myBook.addAuthor(fullName, myAuthorNames[2]);
				myAuthorNames[0].erase();
				myAuthorNames[1].erase();
				myAuthorNames[2].erase();
				myReadState = READ_SOMETHING;
			}
			break;
		case _LANG:
			if (myReadState == READ_LANGUAGE) {
				myBook.setLanguage(myBuffer);
				myBuffer.erase();
				myReadState = READ_SOMETHING;
			}
			break;
		case _FIRST_NAME:
			if (myReadState == READ_AUTHOR_NAME_0) {
				myReadState = READ_AUTHOR;
			}
			break;
		case _MIDDLE_NAME:
			if (myReadState == READ_AUTHOR_NAME_1) {
				myReadState = READ_AUTHOR;
			}
			break;
		case _LAST_NAME:
			if (myReadState == READ_AUTHOR_NAME_2) {
				myReadState = READ_AUTHOR;
			}
			break;
		default:
			break;
	}
}

bool FB2MetaInfoReader::readMetaInfo() {
	myReadState = READ_NOTHING;
	for (int i = 0; i < 3; ++i) {
		myAuthorNames[i].erase();
	}
	return readDocument(myBook.file());
}
