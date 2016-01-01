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

#include <cstring>
#include <cstdlib>
#include <string>

#include "RtfReader.h"
#include "RtfReaderStream.h"

class RtfTextOnlyReader : public RtfReader {

public:
	RtfTextOnlyReader(char *buffer, std::size_t maxSize);
	~RtfTextOnlyReader();
	std::size_t readSize() const;

protected:
	void addCharData(const char *data, std::size_t len, bool convert);
	void insertImage(const std::string &mimeType, const std::string &fileName, std::size_t startOffset, std::size_t size);
	void setEncoding(int code);
	void switchDestination(DestinationType destination, bool on);
	void setAlignment();
	void setFontProperty(FontProperty property);
	void newParagraph();

	void interrupt();

private:
	struct RtfTextOnlyReaderState {
		bool ReadText;
	};

	RtfTextOnlyReaderState myCurrentState;

private:
	char* myBuffer;
	const std::size_t myMaxSize;
	std::size_t myFilledSize;
};

RtfTextOnlyReader::RtfTextOnlyReader(char *buffer, std::size_t maxSize) : RtfReader(std::string()), myBuffer(buffer), myMaxSize(maxSize), myFilledSize(0) {
	myCurrentState.ReadText = true;
}

RtfTextOnlyReader::~RtfTextOnlyReader() {
}

void RtfTextOnlyReader::addCharData(const char *data, std::size_t len, bool) {
	if (myBuffer == 0) {
		return;
	}
	if (myCurrentState.ReadText) {
		if (myFilledSize < myMaxSize) {
			len = std::min((std::size_t)len, myMaxSize - myFilledSize);
			std::memcpy(myBuffer + myFilledSize, data, len);
			myFilledSize += len;
		}
		if (myFilledSize < myMaxSize) {
			myBuffer[myFilledSize++]=' ';
		} else {
			interrupt();
		}
	}
}

std::size_t RtfTextOnlyReader::readSize() const {
	return myFilledSize;
}

void RtfTextOnlyReader::insertImage(const std::string&, const std::string&, std::size_t, std::size_t) {
}

void RtfTextOnlyReader::setEncoding(int) {
}

void RtfTextOnlyReader::switchDestination(DestinationType destination, bool on) {
	switch (destination) {
		case DESTINATION_NONE:
			break;
		case DESTINATION_SKIP:
		case DESTINATION_INFO:
		case DESTINATION_TITLE:
		case DESTINATION_AUTHOR:
		case DESTINATION_STYLESHEET:
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_PICTURE:
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_FOOTNOTE:
			if (on) {
				myCurrentState.ReadText = true;
			}
			break;
	}
}

void RtfTextOnlyReader::setAlignment() {
}

void RtfTextOnlyReader::setFontProperty(FontProperty) {
}

void RtfTextOnlyReader::newParagraph() {
}

void RtfTextOnlyReader::interrupt() {
}

RtfReaderStream::RtfReaderStream(const ZLFile& file, std::size_t maxSize) : myFile(file), myBuffer(0), mySize(maxSize) {
}

RtfReaderStream::~RtfReaderStream() {
	close();
}

bool RtfReaderStream::open() {
	if (mySize != 0) {
		myBuffer = new char[mySize];
	}
	RtfTextOnlyReader reader(myBuffer, mySize);
	reader.readDocument(myFile);
	mySize = reader.readSize();
	myOffset = 0;
	return true;
}

std::size_t RtfReaderStream::read(char *buffer, std::size_t maxSize) {
	maxSize = std::min(maxSize, mySize - myOffset);
	if ((buffer != 0) && (myBuffer !=0)) {
		std::memcpy(buffer, myBuffer + myOffset, maxSize);
	}
	myOffset += maxSize;
	return maxSize;
}

void RtfReaderStream::close() {
	if (myBuffer != 0) {
		delete[] myBuffer;
		myBuffer = 0;
	}
}

void RtfReaderStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	myOffset = std::min(mySize, (std::size_t)std::max(0, offset));
}

std::size_t RtfReaderStream::offset() const {
	return myOffset;
}

std::size_t RtfReaderStream::sizeOfOpened() {
	return mySize;
}
