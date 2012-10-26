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

#include "DocStreams.h"
#include "OleStreamReader.h"

class DocReader : public OleStreamReader {

public:
	DocReader(char *buffer, size_t maxSize);
	~DocReader();
	size_t readSize() const;

private:
	bool readStream(OleMainStream &stream);
	void dataHandler(const char *buffer, size_t len);
	void ansiSymbolHandler(ZLUnicodeUtil::Ucs2Char symbol);
	void footnoteHandler();

protected:
	char *myBuffer;
	const size_t myMaxSize;
	size_t myActualSize;
};

class DocCharReader : public DocReader {

public:
	DocCharReader(char *buffer, size_t maxSize);
	~DocCharReader();

private:
	void dataHandler(const char *buffer, size_t len);
};

class DocAnsiReader : public DocReader {

public:
	DocAnsiReader(char *buffer, size_t maxSize);
	~DocAnsiReader();

private:
	void ansiSymbolHandler(ZLUnicodeUtil::Ucs2Char symbol);
};

DocReader::DocReader(char *buffer, size_t maxSize) : myBuffer(buffer), myMaxSize(maxSize), myActualSize(0) {
}

DocReader::~DocReader() {
}

bool DocReader::readStream(OleMainStream &stream) {
	while (myActualSize < myMaxSize) {
		if (!readNextPiece(stream)) {
			break;
		}
	}
	return true;
}

void DocReader::dataHandler(const char*, size_t) {
}

void DocReader::ansiSymbolHandler(ZLUnicodeUtil::Ucs2Char) {
}

void DocReader::footnoteHandler() {
}

size_t DocReader::readSize() const {
	return myActualSize;
}

DocCharReader::DocCharReader(char *buffer, size_t maxSize) : DocReader(buffer, maxSize) {
}

DocCharReader::~DocCharReader() {
}

void DocCharReader::dataHandler(const char *buffer, size_t dataLength) {
	if (myActualSize < myMaxSize) {
		const size_t len = std::min(dataLength, myMaxSize - myActualSize);
		strncpy(myBuffer + myActualSize, buffer, len);
		myActualSize += len;
	}
}

DocAnsiReader::DocAnsiReader(char *buffer, size_t maxSize) : DocReader(buffer, maxSize) {
}

DocAnsiReader::~DocAnsiReader() {
}

void DocAnsiReader::ansiSymbolHandler(ZLUnicodeUtil::Ucs2Char symbol) {
	if (myActualSize < myMaxSize) {
		char buffer[4];
		const size_t dataLength = ZLUnicodeUtil::ucs2ToUtf8(buffer, symbol);
		const size_t len = std::min(dataLength, myMaxSize - myActualSize);
		strncpy(myBuffer + myActualSize, buffer, len);
		myActualSize += len;
	}
}

DocStream::DocStream(const ZLFile& file, size_t maxSize) : myFile(file), myBuffer(0), mySize(maxSize) {
}

DocStream::~DocStream() {
	close();
}

bool DocStream::open() {
	if (mySize != 0) {
		myBuffer = new char[mySize];
	}
	shared_ptr<DocReader> reader = createReader(myBuffer, mySize);
	shared_ptr<ZLInputStream> stream = myFile.inputStream();
	if (stream.isNull() || !stream->open()) {
		return false;
	}
	if (!reader->readDocument(stream)) {
		return false;
	}
	mySize = reader->readSize();
	myOffset = 0;
	return true;
}

size_t DocStream::read(char *buffer, size_t maxSize) {
	maxSize = std::min(maxSize, mySize - myOffset);
	if ((buffer != 0) && (myBuffer !=0)) {
		memcpy(buffer, myBuffer + myOffset, maxSize);
	}
	myOffset += maxSize;
	return maxSize;
}

void DocStream::close() {
	if (myBuffer != 0) {
		delete[] myBuffer;
		myBuffer = 0;
	}
}

void DocStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	myOffset = std::min(mySize, (size_t)std::max(0, offset));
}

size_t DocStream::offset() const {
	return myOffset;
}

size_t DocStream::sizeOfOpened() {
	return mySize;
}

DocCharStream::DocCharStream(const ZLFile& file, size_t maxSize) : DocStream(file, maxSize) {
}

DocCharStream::~DocCharStream() {
}

shared_ptr<DocReader> DocCharStream::createReader(char *buffer, size_t maxSize) {
	return new DocCharReader(buffer, maxSize);
}

DocAnsiStream::DocAnsiStream(const ZLFile& file, size_t maxSize) : DocStream(file, maxSize) {
}

DocAnsiStream::~DocAnsiStream() {
}

shared_ptr<DocReader> DocAnsiStream::createReader(char *buffer, size_t maxSize) {
	return new DocAnsiReader(buffer, maxSize);
}
