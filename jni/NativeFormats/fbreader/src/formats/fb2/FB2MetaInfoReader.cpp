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

#include <ZLInputStream.h>
#include <ZLUnicodeUtil.h>

#include "FB2MetaInfoReader.h"
#include "FB2TagManager.h"

#include "../../library/Book.h"

FB2MetaInfoReader::FB2MetaInfoReader(Book &book) : myBook(book) {
	myBook.removeAllAuthors();
	myBook.setTitle(std::string());
	myBook.setLanguage(std::string());
	myBook.removeAllTags();
	myBook.removeAllUids();
}

void FB2MetaInfoReader::characterDataHandler(const char *text, std::size_t len) {
	switch (myReadState) {
		case READ_TITLE:
		case READ_LANGUAGE:
		case READ_GENRE:
		case READ_ID:
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
			myReadState = READ_TITLE_INFO;
			break;
		case _DOCUMENT_INFO:
			myReadState = READ_DOCUMENT_INFO;
			break;
		case _BOOK_TITLE:
			if (myReadState == READ_TITLE_INFO) {
				myReadState = READ_TITLE;
			}
			break;
		case _GENRE:
			if (myReadState == READ_TITLE_INFO) {
				myReadState = READ_GENRE;
			}
			break;
		case _AUTHOR:
			if (myReadState == READ_TITLE_INFO) {
				myReadState = READ_AUTHOR;
			}
			break;
		case _LANG:
			if (myReadState == READ_TITLE_INFO) {
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
			if (myReadState == READ_TITLE_INFO) {
				const char *name = attributeValue(attributes, "name");
				if (name != 0) {
					std::string seriesTitle = name;
					ZLUnicodeUtil::utf8Trim(seriesTitle);
					const char *number = attributeValue(attributes, "number");
					myBook.setSeries(seriesTitle, number != 0 ? std::string(number) : std::string());
				}
			}
			break;
		case _ID:
			if (myReadState == READ_DOCUMENT_INFO) {
				myReadState = READ_ID;
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
		case _DOCUMENT_INFO:
			myReadState = READ_NOTHING;
			break;
		case _BOOK_TITLE:
			if (myReadState == READ_TITLE) {
				myBook.setTitle(myBuffer);
				myBuffer.erase();
				myReadState = READ_TITLE_INFO;
			}
			break;
		case _GENRE:
			if (myReadState == READ_GENRE) {
				ZLUnicodeUtil::utf8Trim(myBuffer);
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
				myReadState = READ_TITLE_INFO;
			}
			break;
		case _AUTHOR:
			if (myReadState == READ_AUTHOR) {
				ZLUnicodeUtil::utf8Trim(myAuthorNames[0]);
				ZLUnicodeUtil::utf8Trim(myAuthorNames[1]);
				ZLUnicodeUtil::utf8Trim(myAuthorNames[2]);
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
				myReadState = READ_TITLE_INFO;
			}
			break;
		case _LANG:
			if (myReadState == READ_LANGUAGE) {
				myBook.setLanguage(myBuffer);
				myBuffer.erase();
				myReadState = READ_TITLE_INFO;
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
		case _ID:
			if (myReadState == READ_ID) {
				myBook.addUid("FB2-DOC-ID", myBuffer);
				myBuffer.erase();
				myReadState = READ_DOCUMENT_INFO;
			}
			break;
		default:
			break;
	}
}

bool FB2MetaInfoReader::readMetainfo() {
	myReadState = READ_NOTHING;
	myBuffer.erase();
	for (int i = 0; i < 3; ++i) {
		myAuthorNames[i].erase();
	}
	return readDocument(myBook.file());
}
