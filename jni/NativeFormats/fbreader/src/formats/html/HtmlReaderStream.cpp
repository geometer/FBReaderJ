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

#include <cstdlib>
#include <cstring>
#include <algorithm>

#include "HtmlReaderStream.h"
#include "HtmlReader.h"

class HtmlTextOnlyReader : public HtmlReader {

public:
	HtmlTextOnlyReader(char *buffer, std::size_t maxSize);
	std::size_t size() const;

private:
	void startDocumentHandler();
	void endDocumentHandler();

	bool tagHandler(const HtmlTag &tag);
	bool characterDataHandler(const char *text, std::size_t len, bool convert);

private:
	char *myBuffer;
	std::size_t myMaxSize;
	std::size_t myFilledSize;
	bool myIgnoreText;
};

HtmlTextOnlyReader::HtmlTextOnlyReader(char *buffer, std::size_t maxSize) : HtmlReader(std::string()), myBuffer(buffer), myMaxSize(maxSize), myFilledSize(0), myIgnoreText(false) {
}

std::size_t HtmlTextOnlyReader::size() const {
	return myFilledSize;
}

void HtmlTextOnlyReader::startDocumentHandler() {
}

void HtmlTextOnlyReader::endDocumentHandler() {
}

bool HtmlTextOnlyReader::tagHandler(const HtmlTag &tag) {
	if (tag.Name == "script") {
		myIgnoreText = tag.Start;
	}
	if ((myFilledSize < myMaxSize) && (myFilledSize > 0) && (myBuffer[myFilledSize - 1] != '\n')) {
		myBuffer[myFilledSize++] = '\n';
	}
	return myFilledSize < myMaxSize;
}

bool HtmlTextOnlyReader::characterDataHandler(const char *text, std::size_t len, bool) {
	if (!myIgnoreText) {
		len = std::min((std::size_t)len, myMaxSize - myFilledSize);
		std::memcpy(myBuffer + myFilledSize, text, len);
		myFilledSize += len;
	}
	return myFilledSize < myMaxSize;
}

HtmlReaderStream::HtmlReaderStream(shared_ptr<ZLInputStream> base, std::size_t maxSize) : myBase(base), myBuffer(0), mySize(maxSize) {
}

HtmlReaderStream::~HtmlReaderStream() {
	close();
}

bool HtmlReaderStream::open() {
	if (myBase.isNull() || !myBase->open()) {
		return false;
	}
	myBuffer = new char[mySize];
	HtmlTextOnlyReader reader(myBuffer, mySize);
	reader.readDocument(*myBase);
	mySize = reader.size();
	myOffset = 0;
	myBase->close();
	return true;
}

std::size_t HtmlReaderStream::read(char *buffer, std::size_t maxSize) {
	maxSize = std::min(maxSize, mySize - myOffset);
	if (buffer != 0) {
		std::memcpy(buffer, myBuffer, maxSize);
	}
	myOffset += maxSize;
	return maxSize;
}

void HtmlReaderStream::close() {
	if (myBuffer != 0) {
		delete[] myBuffer;
		myBuffer = 0;
	}
}

void HtmlReaderStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	myOffset = std::min(mySize, (std::size_t)std::max(0, offset));
}

std::size_t HtmlReaderStream::offset() const {
	return myOffset;
}

std::size_t HtmlReaderStream::sizeOfOpened() {
	return mySize;
}
