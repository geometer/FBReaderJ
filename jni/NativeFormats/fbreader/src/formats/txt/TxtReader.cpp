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

#include <cctype>

#include <ZLInputStream.h>

#include "TxtReader.h"

class TxtReaderCore {

public:
	TxtReaderCore(TxtReader &reader);
	virtual void readDocument(ZLInputStream &stream);

protected:
	TxtReader &myReader;
};

class TxtReaderCoreUtf16 : public TxtReaderCore {

public:
	TxtReaderCoreUtf16(TxtReader &reader);
	void readDocument(ZLInputStream &stream);

protected:
	bool isAscii(char *ptr);
	bool isAscii(char *ptr, char ascii);
};

class TxtReaderCoreUtf16BE : public TxtReaderCore {

public:
	TxtReaderCoreUtf16BE(TxtReader &reader);
	void readDocument(ZLInputStream &stream);

protected:
	bool isAscii(char *ptr);
	bool isAscii(char *ptr, char ascii);
};

TxtReader::TxtReader(const std::string &encoding) : EncodedTextReader(encoding) {
	if (ZLEncodingConverter::UTF16 == encoding) {
		myCore = new TxtReaderCoreUtf16(*this);
	} else if (ZLEncodingConverter::UTF16BE == encoding) {
		myCore = new TxtReaderCoreUtf16BE(*this);
	} else {
		myCore = new TxtReaderCore(*this);
	}
}

TxtReader::~TxtReader() {
}

void TxtReader::readDocument(ZLInputStream &stream) {
	if (!stream.open()) {
		return;
	}
	startDocumentHandler();
	myCore->readDocument(stream);
	endDocumentHandler();
	stream.close();
}

TxtReaderCore::TxtReaderCore(TxtReader &reader) : myReader(reader) {
}

TxtReaderCoreUtf16::TxtReaderCoreUtf16(TxtReader &reader) : TxtReaderCore(reader) {
}

TxtReaderCoreUtf16BE::TxtReaderCoreUtf16BE(TxtReader &reader) : TxtReaderCore(reader) {
}

void TxtReaderCore::readDocument(ZLInputStream &stream) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr != end; ++ptr) {
			if (*ptr == '\n' || *ptr == '\r') {
				bool skipNewLine = false;
				if (*ptr == '\r' && (ptr + 1) != end && *(ptr + 1) == '\n') {
					skipNewLine = true;
					*ptr = '\n';
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 1);
					myReader.characterDataHandler(str);
				}
				if (skipNewLine) {
					++ptr;
				}
				start = ptr + 1;
				myReader.newLineHandler();
			} else if (isspace((unsigned char)*ptr)) {
				if (*ptr != '\t') {
					*ptr = ' ';
				}
			} else {
			}
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			myReader.characterDataHandler(str);
		}
	} while (length == BUFSIZE);
	delete[] buffer;
}

bool TxtReaderCoreUtf16::isAscii(char *ptr) {
	return *(ptr + 1) == '\0';
}

bool TxtReaderCoreUtf16::isAscii(char *ptr, char ascii) {
	return *ptr == ascii && *(ptr + 1) == '\0';
}

void TxtReaderCoreUtf16::readDocument(ZLInputStream &stream) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr < end; ptr += 2) {
			if (isAscii(ptr, '\n') || isAscii(ptr, '\r')) {
				bool skipNewLine = false;
				if (isAscii(ptr, '\r') && ptr + 2 != end && isAscii(ptr + 2, '\n')) {
					skipNewLine = true;
					*ptr = '\n';
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 2);
					myReader.characterDataHandler(str);
				}
				if (skipNewLine) {
					ptr += 2;
				}
				start = ptr + 2;
				myReader.newLineHandler();
			} else if (isspace((unsigned char)*ptr) && *(ptr + 1) == '\0') {
				if (*ptr != '\t') {
					*ptr = ' ';
				}
			} else {
			}
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			myReader.characterDataHandler(str);
		}
	} while (length == BUFSIZE);
	delete[] buffer;
}

bool TxtReaderCoreUtf16BE::isAscii(char *ptr) {
	return *ptr == '\0';
}

bool TxtReaderCoreUtf16BE::isAscii(char *ptr, char ascii) {
	return *ptr == '\0' && *(ptr + 1) == ascii;
}

void TxtReaderCoreUtf16BE::readDocument(ZLInputStream &stream) {
	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr < end; ptr += 2) {
			if (isAscii(ptr, '\n') || isAscii(ptr, '\r')) {
				bool skipNewLine = false;
				if (isAscii(ptr, '\r') && ptr + 2 != end && isAscii(ptr + 2, '\n')) {
					skipNewLine = true;
					*(ptr + 1) = '\n';
				}
				if (start != ptr) {
					str.erase();
					myReader.myConverter->convert(str, start, ptr + 2);
					myReader.characterDataHandler(str);
				}
				if (skipNewLine) {
					ptr += 2;
				}
				start = ptr + 2;
				myReader.newLineHandler();
			} else if (isspace((unsigned char)*(ptr + 1)) && *ptr == '\0') {
				if (*(ptr + 1) != '\t') {
					*(ptr + 1) = ' ';
				}
			} else {
			}
		}
		if (start != end) {
			str.erase();
			myReader.myConverter->convert(str, start, end);
			myReader.characterDataHandler(str);
		}
	} while (length == BUFSIZE);
	delete[] buffer;
}
