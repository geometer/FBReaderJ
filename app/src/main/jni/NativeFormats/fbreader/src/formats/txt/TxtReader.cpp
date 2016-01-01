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
	virtual char getAscii(const char *ptr) = 0;
	virtual void setAscii(char *ptr, char ascii) = 0;
};

class TxtReaderCoreUtf16LE : public TxtReaderCoreUtf16 {

public:
	TxtReaderCoreUtf16LE(TxtReader &reader);

protected:
	char getAscii(const char *ptr);
	void setAscii(char *ptr, char ascii);
};

class TxtReaderCoreUtf16BE : public TxtReaderCoreUtf16 {

public:
	TxtReaderCoreUtf16BE(TxtReader &reader);

protected:
	char getAscii(const char *ptr);
	void setAscii(char *ptr, char ascii);
};

TxtReader::TxtReader(const std::string &encoding) : EncodedTextReader(encoding) {
	if (ZLEncodingConverter::UTF16 == encoding) {
		myCore = new TxtReaderCoreUtf16LE(*this);
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

void TxtReaderCore::readDocument(ZLInputStream &stream) {
	const std::size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	std::size_t length;
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
			} else if (((*ptr) & 0x80) == 0 && std::isspace((unsigned char)*ptr)) {
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

void TxtReaderCoreUtf16::readDocument(ZLInputStream &stream) {
	const std::size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	std::size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr < end; ptr += 2) {
			const char chr = getAscii(ptr);
			if (chr == '\n' || chr == '\r') {
				bool skipNewLine = false;
				if (chr == '\r' && ptr + 2 != end && getAscii(ptr + 2) == '\n') {
					skipNewLine = true;
					setAscii(ptr, '\n');
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
			} else if (chr != 0 && ((*ptr) & 0x80) == 0 && std::isspace(chr)) {
				if (chr != '\t') {
					setAscii(ptr, ' ');
				}
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

TxtReaderCoreUtf16LE::TxtReaderCoreUtf16LE(TxtReader &reader) : TxtReaderCoreUtf16(reader) {
}

char TxtReaderCoreUtf16LE::getAscii(const char *ptr) {
	return *(ptr + 1) == '\0' ? *ptr : '\0';
}

void TxtReaderCoreUtf16LE::setAscii(char *ptr, char ascii) {
	*ptr = ascii;
}

TxtReaderCoreUtf16BE::TxtReaderCoreUtf16BE(TxtReader &reader) : TxtReaderCoreUtf16(reader) {
}

char TxtReaderCoreUtf16BE::getAscii(const char *ptr) {
	return *ptr == '\0' ? *(ptr + 1) : '\0';
}

void TxtReaderCoreUtf16BE::setAscii(char *ptr, char ascii) {
	*(ptr + 1) = ascii;
}
