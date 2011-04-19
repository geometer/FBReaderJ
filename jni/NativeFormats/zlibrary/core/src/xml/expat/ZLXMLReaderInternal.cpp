/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include <string.h>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLEncodingConverter.h>

#include "ZLXMLReaderInternal.h"
#include "../ZLXMLReader.h"

void ZLXMLReaderInternal::fCharacterDataHandler(void *userData, const char *text, int len) {
	ZLXMLReader &reader = *(ZLXMLReader*)userData;
	if (!reader.isInterrupted()) {
		reader.characterDataHandler(text, len);
	}
}

void ZLXMLReaderInternal::fStartElementHandler(void *userData, const char *name, const char **attributes) {
	ZLXMLReader &reader = *(ZLXMLReader*)userData;
	if (!reader.isInterrupted()) {
		if (reader.processNamespaces()) {
			int count = 0;
			for (const char **a = attributes; (*a != 0) && (*(a + 1) != 0); a += 2) {
				if (strncmp(*a, "xmlns:", 6) == 0) {
					if (count == 0) {
						reader.myNamespaces.push_back(
							new std::map<std::string,std::string>(*reader.myNamespaces.back())
						);
					}
					++count;
					const std::string id(*a + 6);
					const std::string reference(*(a + 1));
					(*reader.myNamespaces.back())[id] = reference;
				}
			}
			if (count == 0) {
				reader.myNamespaces.push_back(reader.myNamespaces.back());
			} else {
				reader.namespaceListChangedHandler();
			}
		}
		reader.startElementHandler(name, attributes);
	}
}

void ZLXMLReaderInternal::fEndElementHandler(void *userData, const char *name) {
	ZLXMLReader &reader = *(ZLXMLReader*)userData;
	if (!reader.isInterrupted()) {
		reader.endElementHandler(name);
		if (reader.processNamespaces()) {
			shared_ptr<std::map<std::string,std::string> > oldMap = reader.myNamespaces.back();
			reader.myNamespaces.pop_back();
			if (reader.myNamespaces.back() != oldMap) {
				reader.namespaceListChangedHandler();
			}
		}
	}
}

static int fUnknownEncodingHandler(void*, const XML_Char *name, XML_Encoding *encoding) {
	ZLEncodingConverterInfoPtr info = ZLEncodingCollection::Instance().info(name);
	if (!info.isNull()) {
		shared_ptr<ZLEncodingConverter> converter = info->createConverter();
		if (!converter.isNull() && converter->fillTable(encoding->map)) {
			return XML_STATUS_OK;
		}
	}
	return XML_STATUS_ERROR;
}

static void parseDTD(XML_Parser parser, const std::string &fileName) {
	XML_Parser entityParser = XML_ExternalEntityParserCreate(parser, 0, 0);
	ZLFile dtdFile(fileName);
	shared_ptr<ZLInputStream> entityStream = dtdFile.inputStream();
	if (!entityStream.isNull() && entityStream->open()) {
		const size_t BUFSIZE = 2048;
		char buffer[BUFSIZE];
		size_t length;
		do {
			length = entityStream->read(buffer, BUFSIZE);
			if (XML_Parse(entityParser, buffer, length, 0) == XML_STATUS_ERROR) {
				break;
			}
		} while (length == BUFSIZE);
	}
	XML_ParserFree(entityParser);
}

ZLXMLReaderInternal::ZLXMLReaderInternal(ZLXMLReader &reader, const char *encoding) : myReader(reader) {
	myParser = XML_ParserCreate(encoding);
	myInitialized = false;
}

ZLXMLReaderInternal::~ZLXMLReaderInternal() {
	XML_ParserFree(myParser);
}

void ZLXMLReaderInternal::init(const char *encoding) {
	if (myInitialized) {
		XML_ParserReset(myParser, encoding);
	}

	myInitialized = true;
	XML_UseForeignDTD(myParser, XML_TRUE);

	const std::vector<std::string> &dtds = myReader.externalDTDs();
	for (std::vector<std::string>::const_iterator it = dtds.begin(); it != dtds.end(); ++it) {
		myDTDStreamLocks.insert(ZLFile(*it).inputStream());
		parseDTD(myParser, *it);
	}

	XML_SetUserData(myParser, &myReader);
	if (encoding != 0) {
		XML_SetEncoding(myParser, encoding);
	}
	XML_SetStartElementHandler(myParser, fStartElementHandler);
	XML_SetEndElementHandler(myParser, fEndElementHandler);
	XML_SetCharacterDataHandler(myParser, fCharacterDataHandler);
	XML_SetUnknownEncodingHandler(myParser, fUnknownEncodingHandler, 0);
}

bool ZLXMLReaderInternal::parseBuffer(const char *buffer, size_t len) {
	return XML_Parse(myParser, buffer, len, 0) != XML_STATUS_ERROR;
}
