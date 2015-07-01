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

#include "FB2UidReader.h"

#include "../../library/Book.h"

FB2UidReader::FB2UidReader(Book &book) : myBook(book) {
	myBook.removeAllUids();
}

void FB2UidReader::characterDataHandler(const char *text, std::size_t len) {
	if (myReadState == READ_ID) {
		myBuffer.append(text, len);
	}
}

void FB2UidReader::startElementHandler(int tag, const char **attributes) {
	switch (tag) {
		case _BODY:
			myReturnCode = true;
			interrupt();
			break;
		case _DOCUMENT_INFO:
			myReadState = READ_DOCUMENT_INFO;
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

void FB2UidReader::endElementHandler(int tag) {
	switch (tag) {
		case _DOCUMENT_INFO:
			myReadState = READ_NOTHING;
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

bool FB2UidReader::readUids() {
	myReadState = READ_NOTHING;
	myBuffer.erase();
	return readDocument(myBook.file());
}
