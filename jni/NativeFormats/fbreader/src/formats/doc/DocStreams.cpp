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

#include "DocStreams.h"
#include "OleStreamReader.h"

class DocReader : public OleStreamReader {

public:
	DocReader(char *buffer, std::size_t maxSize);
	~DocReader();
	std::size_t readSize() const;

private:
	bool readStream(OleMainStream &stream);
	void ansiDataHandler(const char *buffer, std::size_t len);
	void ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char symbol);
	void footnotesStartHandler();

protected:
	char *myBuffer;
	const std::size_t myMaxSize;
	std::size_t myActualSize;
};

class DocAnsiReader : public DocReader {

public:
	DocAnsiReader(char *buffer, std::size_t maxSize);
	~DocAnsiReader();

private:
	void ansiDataHandler(const char *buffer, std::size_t len);
};

class DocUcs2Reader : public DocReader {

public:
	DocUcs2Reader(char *buffer, std::size_t maxSize);
	~DocUcs2Reader();

private:
	void ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char symbol);
};

DocReader::DocReader(char *buffer, std::size_t maxSize) : myBuffer(buffer), myMaxSize(maxSize), myActualSize(0) {
}

DocReader::~DocReader() {
}

bool DocReader::readStream(OleMainStream &stream) {
	// TODO make 2 optmizations:
	//	1) If another piece is too big, reading of next piece can be stopped if some size parameter will be specified
	//		(it can be transfered as a parameter (with default 0 value, that means no need to use it) to readNextPiece method)
	//	2) We can specify as a parameter for readNextPiece, what kind of piece should be read next (ANSI or not ANSI).
	//		As type of piece is known already, there's no necessary to read other pieces.
	while (myActualSize < myMaxSize) {
		if (!readNextPiece(stream)) {
			break;
		}
	}
	return true;
}

void DocReader::ansiDataHandler(const char*, std::size_t) {
}

void DocReader::ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char) {
}

void DocReader::footnotesStartHandler() {
}

std::size_t DocReader::readSize() const {
	return myActualSize;
}

DocAnsiReader::DocAnsiReader(char *buffer, std::size_t maxSize) : DocReader(buffer, maxSize) {
}

DocAnsiReader::~DocAnsiReader() {
}

void DocAnsiReader::ansiDataHandler(const char *buffer, std::size_t dataLength) {
	if (myActualSize < myMaxSize) {
		const std::size_t len = std::min(dataLength, myMaxSize - myActualSize);
		std::strncpy(myBuffer + myActualSize, buffer, len);
		myActualSize += len;
	}
}

DocUcs2Reader::DocUcs2Reader(char *buffer, std::size_t maxSize) : DocReader(buffer, maxSize) {
}

DocUcs2Reader::~DocUcs2Reader() {
}

void DocUcs2Reader::ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char symbol) {
	if (myActualSize < myMaxSize) {
		char buffer[4];
		const std::size_t dataLength = ZLUnicodeUtil::ucs2ToUtf8(buffer, symbol);
		const std::size_t len = std::min(dataLength, myMaxSize - myActualSize);
		std::strncpy(myBuffer + myActualSize, buffer, len);
		myActualSize += len;
	}
}

DocStream::DocStream(const ZLFile& file, std::size_t maxSize) : myFile(file), myBuffer(0), mySize(maxSize) {
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
	if (!reader->readDocument(stream, false)) {
		return false;
	}
	mySize = reader->readSize();
	myOffset = 0;
	return true;
}

std::size_t DocStream::read(char *buffer, std::size_t maxSize) {
	maxSize = std::min(maxSize, mySize - myOffset);
	if (buffer != 0 && myBuffer != 0) {
		std::memcpy(buffer, myBuffer + myOffset, maxSize);
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
	myOffset = std::min(mySize, (std::size_t)std::max(0, offset));
}

std::size_t DocStream::offset() const {
	return myOffset;
}

std::size_t DocStream::sizeOfOpened() {
	return mySize;
}

DocAnsiStream::DocAnsiStream(const ZLFile& file, std::size_t maxSize) : DocStream(file, maxSize) {
}

DocAnsiStream::~DocAnsiStream() {
}

shared_ptr<DocReader> DocAnsiStream::createReader(char *buffer, std::size_t maxSize) {
	return new DocAnsiReader(buffer, maxSize);
}

DocUcs2Stream::DocUcs2Stream(const ZLFile& file, std::size_t maxSize) : DocStream(file, maxSize) {
}

DocUcs2Stream::~DocUcs2Stream() {
}

shared_ptr<DocReader> DocUcs2Stream::createReader(char *buffer, std::size_t maxSize) {
	return new DocUcs2Reader(buffer, maxSize);
}
