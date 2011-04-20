/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#include <stdlib.h>
#include <string.h>

#include <ZLUnicodeUtil.h>
#include <ZLibrary.h>
#include <ZLFile.h>
#include <ZLDir.h>
#include <ZLXMLReader.h>

#include "MyEncodingConverter.h"

class MyOneByteEncodingConverter : public ZLEncodingConverter {

private:
	MyOneByteEncodingConverter(const std::string &encoding, char **encodingMap);

public:
	~MyOneByteEncodingConverter();
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);
	void reset();
	bool fillTable(int *map);

private:
	const std::string myEncoding;
	char *myEncodingMap;

friend class MyEncodingConverterProvider;
};

class MyTwoBytesEncodingConverter : public ZLEncodingConverter {

private:
	MyTwoBytesEncodingConverter(char **encodingMap);

public:
	~MyTwoBytesEncodingConverter();
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);
	void reset();
	bool fillTable(int *map);

private:
	char **myEncodingMap;
	
	char myLastChar;
	bool myLastCharIsNotProcessed;

friend class MyEncodingConverterProvider;
};

class EncodingReader : public ZLXMLReader {

protected:
	EncodingReader(const std::string &encoding);

public:
	virtual ~EncodingReader();

public:
	virtual void startElementHandler(const char *tag, const char **attributes);
	int bytesNumber() const { return myBytesNumber; }

protected:
	const ZLFile myFile;
	int myBytesNumber;
};

class EncodingIntReader : public EncodingReader {

public:
	EncodingIntReader(const std::string &encoding);
	~EncodingIntReader();
	bool fillTable(int *map);

public:
	void startElementHandler(const char *tag, const char **attributes);

private:
	int *myMap;
};

class EncodingCharReader : public EncodingReader {

public:
	EncodingCharReader(const std::string &encoding);
	~EncodingCharReader();
	char **createTable();

public:
	void startElementHandler(const char *tag, const char **attributes);

private:
	char **myMap;
	char myBuffer[3];
};

MyEncodingConverterProvider::MyEncodingConverterProvider() {
	shared_ptr<ZLDir> dir =
		ZLFile(ZLEncodingCollection::encodingDescriptionPath()).directory();
	if (!dir.isNull()) {
		std::vector<std::string> files;
		dir->collectFiles(files, false);
		myProvidedEncodings.insert(files.begin(), files.end());
	}
}

bool MyEncodingConverterProvider::providesConverter(const std::string &encoding) {
	return myProvidedEncodings.find(encoding) != myProvidedEncodings.end();
}

shared_ptr<ZLEncodingConverter> MyEncodingConverterProvider::createConverter(const std::string &encoding) {
	EncodingCharReader er(encoding);
	char **encodingMap = er.createTable();
	if (encodingMap != 0) {
		if (er.bytesNumber() == 1) {
			return new MyOneByteEncodingConverter(encoding, encodingMap);
		} else if (er.bytesNumber() == 2) {
			return new MyTwoBytesEncodingConverter(encodingMap);
		}
	}
	return 0;
}

MyOneByteEncodingConverter::MyOneByteEncodingConverter(const std::string &encoding, char **encodingMap) : myEncoding(encoding) {
	myEncodingMap = new char[1024];
	memset(myEncodingMap, '\0', 1024);
	for (int i = 0; i < 256; ++i) {
		ZLUnicodeUtil::ucs4ToUtf8(myEncodingMap + 4 * i, i);
	}
	if (encodingMap != 0) {
		for (int i = 0; i < 256; ++i) {
			if (encodingMap[i] != 0) {
				strcpy(myEncodingMap + 4 * i, encodingMap[i]);
			}
		}
	}
}

MyOneByteEncodingConverter::~MyOneByteEncodingConverter() {
	delete[] myEncodingMap;
}

void MyOneByteEncodingConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	size_t oldLength = dst.length();
	dst.append(3 * (srcEnd - srcStart), '\0');
	char *dstStartPtr = (char*)dst.data() + oldLength;
	char *dstPtr = dstStartPtr;
	const char *p;
	for (const char *ptr = srcStart; ptr != srcEnd; ++ptr) {
		for (p = myEncodingMap + 4 * (unsigned char)*ptr; *p != '\0'; ++p) {
			*(dstPtr++) = *p;
		}
	}
	dst.erase(dstPtr - dstStartPtr + oldLength);
}

void MyOneByteEncodingConverter::reset() {
}

bool MyOneByteEncodingConverter::fillTable(int *map) {
	return EncodingIntReader(myEncoding).fillTable(map);
}

MyTwoBytesEncodingConverter::MyTwoBytesEncodingConverter(char **encodingMap) : myEncodingMap(encodingMap), myLastCharIsNotProcessed(false) {
}

MyTwoBytesEncodingConverter::~MyTwoBytesEncodingConverter() {
	for (int i = 0; i < 32768; ++i) {
		if (myEncodingMap[i] != 0) {
			delete[] myEncodingMap[i];
		}
	}
	delete[] myEncodingMap;
}

void MyTwoBytesEncodingConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	if (srcStart == srcEnd) {
		return;
	}

	dst.reserve(dst.length() + 3 * (srcEnd - srcStart) / 2);
	if (myLastCharIsNotProcessed) {
		const char *utf8 = myEncodingMap[0x100 * (myLastChar & 0x7F) + (unsigned char)*srcStart];
		if (utf8 != 0) {
			dst += utf8;
		}
		++srcStart;
		myLastCharIsNotProcessed = false;
	}
	for (const char *ptr = srcStart; ptr != srcEnd; ++ptr) {
		if (((*ptr) & 0x80) == 0) {
			dst += *ptr;
		} else if (ptr + 1 == srcEnd) {
			myLastChar = *ptr;
			myLastCharIsNotProcessed = true;
		} else {
			const char *utf8 = myEncodingMap[0x100 * ((*ptr) & 0x7F) + (unsigned char)*(ptr + 1)];
			if (utf8 != 0) {
				dst += utf8;
			}
			++ptr;
		}
	}
}

void MyTwoBytesEncodingConverter::reset() {
	myLastCharIsNotProcessed = false;
}

bool MyTwoBytesEncodingConverter::fillTable(int*) {
	return false;
}

EncodingReader::EncodingReader(const std::string &encoding) : myFile(ZLEncodingCollection::encodingDescriptionPath() + ZLibrary::FileNameDelimiter + encoding) {
}

EncodingReader::~EncodingReader() {
}

static const std::string ENCODING = "encoding";
static const std::string CHAR = "char";

void EncodingReader::startElementHandler(const char *tag, const char **attributes) {
  static const std::string BYTES = "bytes";

	if (ENCODING == tag) {
		myBytesNumber = 1;
		if ((attributes[0] != 0) && (BYTES == attributes[0])) {
			myBytesNumber = atoi(attributes[1]);
		}
	}
}

EncodingIntReader::EncodingIntReader(const std::string &encoding) : EncodingReader(encoding) {
}

EncodingIntReader::~EncodingIntReader() {
}

bool EncodingIntReader::fillTable(int *map) {
	myMap = map;
	for (int i = 0; i < 256; ++i) {
		myMap[i] = i;
	}
	return readDocument(myFile);
}

void EncodingIntReader::startElementHandler(const char *tag, const char **attributes) {
	EncodingReader::startElementHandler(tag, attributes);
	if ((CHAR == tag) && (attributes[0] != 0) && (attributes[2] != 0)) {
		char *ptr = 0;
		myMap[strtol(attributes[1], &ptr, 16)] = strtol(attributes[3], &ptr, 16);
	}
}

EncodingCharReader::EncodingCharReader(const std::string &encoding) : EncodingReader(encoding) {
}

EncodingCharReader::~EncodingCharReader() {
}

char **EncodingCharReader::createTable() {
	myMap = 0;
	if (!readDocument(myFile) && (myMap != 0)) {
		int length = (myBytesNumber == 1) ? 256 : 32768;
		for (int i = 0; i < length; ++i) {
			if (myMap[i] != 0) {
				delete[] myMap[i];
			}
		}
		delete[] myMap;
		myMap = 0;
	}

	return myMap;
}

void EncodingCharReader::startElementHandler(const char *tag, const char **attributes) {
	EncodingReader::startElementHandler(tag, attributes);

	if (ENCODING == tag) {
		int length = (myBytesNumber == 1) ? 256 : 32768;
		myMap = new char*[length];
		memset(myMap, 0, length * sizeof(char*));
	} else if ((CHAR == tag) && (attributes[0] != 0) && (attributes[2] != 0)) {
		static char *ptr = 0;
		int index = strtol(attributes[1], &ptr, 16);
		if (myBytesNumber == 1) {
			if ((index < 0) || (index >= 256)) {
				return;
			}
		} else {
			index -= 32768;
			if ((index < 0) || (index >= 32768)) {
				return;
			}
		}
		int value = strtol(attributes[3], &ptr, 16);
		int len = ZLUnicodeUtil::ucs4ToUtf8(myBuffer, value);
		myMap[index] = new char[len + 1];
		memcpy(myMap[index], myBuffer, len);
		myMap[index][len] = '\0';
	}
}
