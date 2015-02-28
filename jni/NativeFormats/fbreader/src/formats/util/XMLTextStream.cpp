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

#include <cstring>

#include <ZLXMLReader.h>
#include <ZLUnicodeUtil.h>

#include <ZLPlainAsynchronousInputStream.h>

#include "XMLTextStream.h"

class XMLTextReader : public ZLXMLReader {

public:
	XMLTextReader(std::string &buffer, const std::string &startTag);

private:
	void startElementHandler(const char *tag, const char **attributes);
	void characterDataHandler(const char *text, std::size_t len);

private:
	const std::string myStartTag;
	std::string &myBuffer;
	bool myStarted;
};

XMLTextReader::XMLTextReader(std::string &buffer, const std::string &startTag) : myStartTag(ZLUnicodeUtil::toLower(startTag)), myBuffer(buffer), myStarted(myStartTag.empty()) {
}

void XMLTextReader::startElementHandler(const char *tag, const char**) {
	if (!myStarted && (myStartTag == ZLUnicodeUtil::toLower(tag))) {
		myStarted = true;
	}
}

void XMLTextReader::characterDataHandler(const char *text, std::size_t len) {
	if (myStarted) {
		myBuffer.append(text, len);
	}
}

XMLTextStream::XMLTextStream(shared_ptr<ZLInputStream> base, const std::string &startTag) : myBase(base), myStreamBuffer(2048, '\0') {
	myReader = new XMLTextReader(myDataBuffer, startTag);
}

XMLTextStream::~XMLTextStream() {
}

bool XMLTextStream::open() {
	close();
	if (myBase.isNull() || !myBase->open()) {
		return false;
	}
	myStream = new ZLPlainAsynchronousInputStream();
	myOffset = 0;
	return true;
}

std::size_t XMLTextStream::read(char *buffer, std::size_t maxSize) {
	while (myDataBuffer.size() < maxSize) {
		std::size_t len = myBase->read((char*)myStreamBuffer.data(), 2048);
		/*if ((len == 0) || !myReader->readFromBuffer(myStreamBuffer.data(), len)) {
			break;
		}*/
		if (len == 0) {
			break;
		}
		myStream->setBuffer(myStreamBuffer.data(), len);
		if (!myReader->readDocument(myStream)) {
			break;
		}
	}
	std::size_t realSize = std::min(myDataBuffer.size(), maxSize);
	if (buffer != 0) {
		std::memcpy(buffer, myDataBuffer.data(), realSize);
	}
	myDataBuffer.erase(0, realSize);
	myOffset += realSize;
	return realSize;
}

void XMLTextStream::close() {
	if (!myStream.isNull()) {
		myStream->setEof();
		myReader->readDocument(myStream);
		myStream.reset();
	}
	myBase->close();
	myDataBuffer.erase();
}

void XMLTextStream::seek(int offset, bool absoluteOffset) {
	// works for nonnegative offsets only
	if (absoluteOffset) {
		offset -= myOffset;
	}
	read(0, offset);
}

std::size_t XMLTextStream::offset() const {
	return myOffset;
}

std::size_t XMLTextStream::sizeOfOpened() {
	// couldn't be implemented
	return 0;
}
