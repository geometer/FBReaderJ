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

#include <algorithm>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>

#include "ZLAsynchronousInputStream.h"

#include "ZLXMLReader.h"

#include "expat/ZLXMLReaderInternal.h"

class ZLXMLReaderHandler : public ZLAsynchronousInputStream::Handler {

public:
	ZLXMLReaderHandler(ZLXMLReader &reader);

	void initialize(const char *encoding);
	void shutdown();
	bool handleBuffer(const char *data, std::size_t len);

private:
	ZLXMLReader &myReader;
};

ZLXMLReaderHandler::ZLXMLReaderHandler(ZLXMLReader &reader) : myReader(reader) {
}

void ZLXMLReaderHandler::initialize(const char *encoding) {
	myReader.initialize(encoding);
}

void ZLXMLReaderHandler::shutdown() {
	myReader.shutdown();
}

bool ZLXMLReaderHandler::handleBuffer(const char *data, std::size_t len) {
	return myReader.readFromBuffer(data, len);
}

static const std::size_t BUFFER_SIZE = 2048;

void ZLXMLReader::startElementHandler(const char*, const char**) {
}

void ZLXMLReader::endElementHandler(const char*) {
}

void ZLXMLReader::characterDataHandler(const char*, std::size_t) {
}

const ZLXMLReader::nsMap &ZLXMLReader::namespaces() const {
	return *myNamespaces.back();
}

ZLXMLReader::ZLXMLReader(const char *encoding) {
	myInternalReader = new ZLXMLReaderInternal(*this, encoding);
	myParserBuffer = new char[BUFFER_SIZE];
}

ZLXMLReader::~ZLXMLReader() {
	delete[] myParserBuffer;
	delete myInternalReader;
}

bool ZLXMLReader::readDocument(const ZLFile &file) {
	return readDocument(file.inputStream());
}

bool ZLXMLReader::readDocument(shared_ptr<ZLInputStream> stream) {
	if (stream.isNull() || !stream->open()) {
		return false;
	}

	bool useWindows1252 = false;
	stream->read(myParserBuffer, 256);
	std::string stringBuffer(myParserBuffer, 256);
	stream->seek(0, true);
	int index = stringBuffer.find('>');
	if (index > 0) {
		stringBuffer = stringBuffer.substr(0, index);
		if (!ZLUnicodeUtil::isUtf8String(stringBuffer)) {
			return false;
		}
		stringBuffer = ZLUnicodeUtil::toLower(stringBuffer);
		int index = stringBuffer.find("\"iso-8859-1\"");
		if (index > 0) {
			useWindows1252 = true;
		}
	}
	initialize(useWindows1252 ? "windows-1252" : 0);

	std::size_t length;
	do {
		length = stream->read(myParserBuffer, BUFFER_SIZE);
		if (!readFromBuffer(myParserBuffer, length)) {
			break;
		}
	} while ((length == BUFFER_SIZE) && !myInterrupted);

	stream->close();

	shutdown();

	return true;
}

void ZLXMLReader::initialize(const char *encoding) {
	myInternalReader->init(encoding);
	myInterrupted = false;
	myNamespaces.push_back(new nsMap());
}

void ZLXMLReader::shutdown() {
	myNamespaces.clear();
}

bool ZLXMLReader::readFromBuffer(const char *data, std::size_t len) {
	return myInternalReader->parseBuffer(data, len);
}

bool ZLXMLReader::processNamespaces() const {
	return false;
}

const std::vector<std::string> &ZLXMLReader::externalDTDs() const {
	static const std::vector<std::string> EMPTY_VECTOR;
	return EMPTY_VECTOR;
}

void ZLXMLReader::collectExternalEntities(std::map<std::string,std::string> &entityMap) {
}

const char *ZLXMLReader::attributeValue(const char **xmlattributes, const char *name) const {
	while (*xmlattributes != 0) {
		bool useNext = std::strcmp(*xmlattributes, name) == 0;
		++xmlattributes;
		if (*xmlattributes == 0) {
			return 0;
		}
		if (useNext) {
			return *xmlattributes;
		}
		++xmlattributes;
	}
	return 0;
}

std::map<std::string,std::string> ZLXMLReader::attributeMap(const char **xmlattributes) const {
	std::map<std::string,std::string> map;
	while (*xmlattributes != 0) {
		std::string key = *xmlattributes;
		++xmlattributes;
		if (*xmlattributes == 0) {
			break;
		}
		map[key] = *xmlattributes;
		++xmlattributes;
	}
	return map;
}

ZLXMLReader::NamePredicate::~NamePredicate() {
}

ZLXMLReader::SimpleNamePredicate::SimpleNamePredicate(const std::string &name) : myName(name) {
}

bool ZLXMLReader::SimpleNamePredicate::accepts(const ZLXMLReader&, const char *name) const {
	return myName == name;
}

bool ZLXMLReader::SimpleNamePredicate::accepts(const ZLXMLReader&, const std::string &name) const {
	return myName == name;
}

ZLXMLReader::IgnoreCaseNamePredicate::IgnoreCaseNamePredicate(const std::string &lowerCaseName) : myLowerCaseName(lowerCaseName) {
}

bool ZLXMLReader::IgnoreCaseNamePredicate::accepts(const ZLXMLReader &reader, const char *name) const {
	std::string lc = name;
	ZLStringUtil::asciiToLowerInline(lc);
	return myLowerCaseName == lc;
}

bool ZLXMLReader::IgnoreCaseNamePredicate::accepts(const ZLXMLReader&, const std::string &name) const {
	std::string lc = name;
	ZLStringUtil::asciiToLowerInline(lc);
	return myLowerCaseName == lc;
}

ZLXMLReader::FullNamePredicate::FullNamePredicate(const std::string &ns, const std::string &name) : myNamespaceName(ns), myName(name) {
}

bool ZLXMLReader::FullNamePredicate::accepts(const ZLXMLReader &reader, const char *name) const {
	return accepts(reader, std::string(name));
}

bool ZLXMLReader::FullNamePredicate::accepts(const ZLXMLReader &reader, const std::string &name) const {
	const std::size_t index = name.find(':');
	const std::string namespaceId =
		index == std::string::npos ? std::string() : name.substr(0, index);

	const nsMap &namespaces = reader.namespaces();
	nsMap::const_iterator it = namespaces.find(namespaceId);
	return
		it != namespaces.end() &&
		it->second == myNamespaceName &&
		name.substr(index + 1) == myName;
}

ZLXMLReader::BrokenNamePredicate::BrokenNamePredicate(const std::string &name) : myName(name) {
}

bool ZLXMLReader::BrokenNamePredicate::accepts(const ZLXMLReader &reader, const char *name) const {
	return accepts(reader, std::string(name));
}

bool ZLXMLReader::BrokenNamePredicate::accepts(const ZLXMLReader &reader, const std::string &name) const {
	return myName == name.substr(name.find(':') + 1);
}

const char *ZLXMLReader::attributeValue(const char **xmlattributes, const NamePredicate &predicate) const {
	while (*xmlattributes != 0) {
		bool useNext = predicate.accepts(*this, *xmlattributes);
		++xmlattributes;
		if (*xmlattributes == 0) {
			return 0;
		}
		if (useNext) {
			return *xmlattributes;
		}
		++xmlattributes;
	}
	return 0;
}

bool ZLXMLReader::testTag(const std::string &ns, const std::string &name, const std::string &tag) const {
	const nsMap &nspaces = namespaces();

	if (name == tag) {
		const nsMap::const_iterator it = nspaces.find(std::string());
		return it != nspaces.end() && ns == it->second;
	}
	const int nameLen = name.size();
	const int tagLen = tag.size();
	if (tagLen < nameLen + 2) {
		return false;
	}
	if (ZLStringUtil::stringEndsWith(tag, name) && tag[tagLen - nameLen - 1] == ':') {
		const nsMap::const_iterator it = nspaces.find(tag.substr(0, tagLen - nameLen - 1));
		return it != nspaces.end() && ns == it->second;
	}
	return false;
}

bool ZLXMLReader::readDocument(shared_ptr<ZLAsynchronousInputStream> stream) {
	ZLXMLReaderHandler handler(*this);
	return stream->processInput(handler);
}

const std::string &ZLXMLReader::errorMessage() const {
	return myErrorMessage;
}

void ZLXMLReader::setErrorMessage(const std::string &message) {
	myErrorMessage = message;
	interrupt();
}

std::size_t ZLXMLReader::getCurrentPosition() const {
	return myInternalReader != 0 ? myInternalReader->getCurrentPosition() : (std::size_t)-1;
}
