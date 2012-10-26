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

#include <cstring>
#include <cstdlib>
#include <string>

#include "DocReaderStream.h"
#include "OleStreamReader.h"

class DocTextOnlyReader : public OleStreamReader {

public:
	DocTextOnlyReader(char *buffer, size_t maxSize);
	~DocTextOnlyReader();
	size_t readSize() const;

private:
	void dataHandler(const char *buffer, size_t len);

	void handleChar(ZLUnicodeUtil::Ucs2Char ucs2char);
	void handleHardLinebreak();
	void handleParagraphEnd();
	void handlePageBreak();
	void handleTableSeparator();
	void handleTableEndRow();
	void handleFootNoteMark();
	void handleStartField();
	void handleSeparatorField();
	void handleEndField();
	void handleImage(const ZLFileImage::Blocks &blocks);
	void handleOtherControlChar(ZLUnicodeUtil::Ucs2Char ucs2char);
	void handleFontStyle(unsigned int fontStyle);
	void handleParagraphStyle(const OleMainStream::Style &styleInfo);
	void handleBookmark(const std::string &name);

private:
	char *myBuffer;
	const size_t myMaxSize;
	size_t myActualSize;
};

DocTextOnlyReader::DocTextOnlyReader(char *buffer, size_t maxSize) : OleStreamReader(std::string()), myBuffer(buffer), myMaxSize(maxSize), myActualSize(0) {
}

DocTextOnlyReader::~DocTextOnlyReader() {
}

void DocTextOnlyReader::dataHandler(const char *buffer, size_t dataLength) {
	if (myActualSize >= myMaxSize) {
		// break stream reading
	} else {
		const size_t len = std::min(dataLength, myMaxSize - myActualSize);
		strncpy(myBuffer + myActualSize, buffer, len);
		myActualSize += len;
	}
	OleStreamReader::dataHandler(buffer, dataLength);
}

void DocTextOnlyReader::handleChar(ZLUnicodeUtil::Ucs2Char ucs2char) {
}

void DocTextOnlyReader::handleHardLinebreak() {
}

void DocTextOnlyReader::handleParagraphEnd() {
}

void DocTextOnlyReader::handlePageBreak() {
}

void DocTextOnlyReader::handleTableSeparator() {
}

void DocTextOnlyReader::handleTableEndRow() {
}

void DocTextOnlyReader::handleFootNoteMark() {
}

void DocTextOnlyReader::handleStartField() {
}

void DocTextOnlyReader::handleSeparatorField() {
}

void DocTextOnlyReader::handleEndField() {
}

void DocTextOnlyReader::handleImage(const ZLFileImage::Blocks &blocks) {
}

void DocTextOnlyReader::handleOtherControlChar(ZLUnicodeUtil::Ucs2Char ucs2char) {
}

void DocTextOnlyReader::handleFontStyle(unsigned int fontStyle) {
}

void DocTextOnlyReader::handleParagraphStyle(const OleMainStream::Style &styleInfo) {
}

void DocTextOnlyReader::handleBookmark(const std::string &name) {
}

size_t DocTextOnlyReader::readSize() const {
	return myActualSize;
}

DocReaderStream::DocReaderStream(const ZLFile& file, size_t maxSize) : myFile(file), myBuffer(0), mySize(maxSize) {
}

DocReaderStream::~DocReaderStream() {
	close();
}

bool DocReaderStream::open() {
	if (mySize != 0) {
		myBuffer = new char[mySize];
	}
	DocTextOnlyReader reader(myBuffer, mySize);
	shared_ptr<ZLInputStream> stream = myFile.inputStream();
	if (stream.isNull() || !stream->open()) {
		return false;
	}
	if (!reader.readDocument(stream)) {
		return false;
	}
	mySize = reader.readSize();
	myOffset = 0;
	return true;
}

size_t DocReaderStream::read(char *buffer, size_t maxSize) {
	maxSize = std::min(maxSize, mySize - myOffset);
	if ((buffer != 0) && (myBuffer !=0)) {
		memcpy(buffer, myBuffer + myOffset, maxSize);
	}
	myOffset += maxSize;
	return maxSize;
}

void DocReaderStream::close() {
	if (myBuffer != 0) {
		delete[] myBuffer;
		myBuffer = 0;
	}
}

void DocReaderStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	myOffset = std::min(mySize, (size_t)std::max(0, offset));
}

size_t DocReaderStream::offset() const {
	return myOffset;
}

size_t DocReaderStream::sizeOfOpened() {
	return mySize;
}
