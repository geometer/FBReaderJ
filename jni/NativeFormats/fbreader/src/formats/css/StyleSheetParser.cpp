/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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
#include <cstring>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLStringUtil.h>
#include <ZLLogger.h>

#include "StyleSheetParser.h"
#include "../util/MiscUtil.h"

StyleSheetParser::StyleSheetParser(const std::string &pathPrefix) : myPathPrefix(pathPrefix) {
	reset();
}

StyleSheetParser::~StyleSheetParser() {
}

void StyleSheetParser::reset() {
	myWord.erase();
	myAttributeName.erase();
	myReadState = WAITING_FOR_SELECTOR;
	myInsideComment = false;
	mySelectorString.erase();
	myMap.clear();
}

void StyleSheetParser::parse(const char *text, int len, bool final) {
	const char *start = text;
	const char *end = text + len;
	for (const char *ptr = start; ptr != end; ++ptr) {
		if (std::isspace(*ptr)) {
			if (start != ptr) {
				myWord.append(start, ptr - start);
			}
			processWord(myWord);
			myWord.erase();
			start = ptr + 1;
		} else if (isControlSymbol(*ptr)) {
			if (start != ptr) {
				myWord.append(start, ptr - start);
			}
			processWord(myWord);
			myWord.erase();
			processControl(*ptr);
			start = ptr + 1;
		}
	}
	if (start < end) {
		myWord.append(start, end - start);
		if (final) {
			processWord(myWord);
			myWord.erase();
		}
	}
}

bool StyleSheetParser::isControlSymbol(const char symbol) {
	switch (myReadState) {
		default:
		case WAITING_FOR_SELECTOR:
			return false;
		case SELECTOR:
			return symbol == '{' || symbol == ';';
		case WAITING_FOR_ATTRIBUTE:
			return symbol == '}' || symbol == ':';
		case ATTRIBUTE_NAME:
			return symbol == ':';
		case ATTRIBUTE_VALUE:
			return symbol == '}' || symbol == ';';
	}
}

void StyleSheetParser::storeData(const std::string&, const StyleSheetTable::AttributeMap&) {
}

void StyleSheetParser::processControl(const char control) {
	switch (myReadState) {
		case WAITING_FOR_SELECTOR:
			break;
		case SELECTOR:
			switch (control) {
				case '{':
					myReadState = WAITING_FOR_ATTRIBUTE;
					break;
				case ';':
					myReadState = WAITING_FOR_SELECTOR;
					mySelectorString.erase();
					break;
			}
			break;
		case WAITING_FOR_ATTRIBUTE:
			if (control == '}') {
				myReadState = WAITING_FOR_SELECTOR;
				storeData(mySelectorString, myMap);
				mySelectorString.erase();
				myMap.clear();
			}
			break;
		case ATTRIBUTE_NAME:
			if (control == ':') {
				myReadState = ATTRIBUTE_VALUE;
			}
			break;
		case ATTRIBUTE_VALUE:
			if (control == ';') {
				myReadState = WAITING_FOR_ATTRIBUTE;
			} else if (control == '}') {
				myReadState = WAITING_FOR_SELECTOR;
				storeData(mySelectorString, myMap);
				mySelectorString.erase();
				myMap.clear();
			}
			break;
	}
}

void StyleSheetParser::processWord(std::string &word) {
	while (!word.empty()) {
		int index = word.find(myInsideComment ? "*/" : "/*");
		if (!myInsideComment) {
			if (index == -1) {
				processWordWithoutComments(word);
			} else if (index > 0) {
				processWordWithoutComments(word.substr(0, index));
			}
		}
		if (index == -1) {
			break;
		}
		myInsideComment = !myInsideComment;
		word.erase(0, index + 2);
	}
}

void StyleSheetParser::processWordWithoutComments(const std::string &word) {
	switch (myReadState) {
		case WAITING_FOR_SELECTOR:
			myReadState = SELECTOR;
			mySelectorString = word;
			break;
		case SELECTOR:
			mySelectorString += ' ' + word;
			break;
		case WAITING_FOR_ATTRIBUTE:
			myReadState = ATTRIBUTE_NAME;
			// go through
		case ATTRIBUTE_NAME:
			myAttributeName = word;
			myMap[myAttributeName].clear();
			break;
		case ATTRIBUTE_VALUE:
		{
			const std::size_t l = word.length();
			if (l >= 2 && (word[0] == '"' || word[0] == '\'') && word[0] == word[l - 1]) {
				myMap[myAttributeName].push_back(word.substr(1, l - 2));
			} else {
				myMap[myAttributeName].push_back(word);
			}
			break;
		}
	}
}

StyleSheetSingleStyleParser::StyleSheetSingleStyleParser(const std::string &pathPrefix) : StyleSheetParser(pathPrefix) {
}

shared_ptr<ZLTextStyleEntry> StyleSheetSingleStyleParser::parseString(const char *text) {
	myReadState = WAITING_FOR_ATTRIBUTE;
	parse(text, std::strlen(text), true);
	shared_ptr<ZLTextStyleEntry> control = StyleSheetTable::createControl(myMap);
	reset();
	return control;
}

StyleSheetMultiStyleParser::StyleSheetMultiStyleParser(const std::string &pathPrefix) : StyleSheetParser(pathPrefix) {
}

void StyleSheetMultiStyleParser::storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map) {
	std::string s = selector;
	ZLStringUtil::stripWhiteSpaces(s);

	if (s.empty()) {
		return;
	}

	if (s[0] == '@') {
		processAtRule(s, map);
		return;
	}

	const std::vector<std::string> ids = ZLStringUtil::split(s, ",");
	for (std::vector<std::string>::const_iterator it = ids.begin(); it != ids.end(); ++it) {
		std::string id = *it;
		ZLStringUtil::stripWhiteSpaces(id);
		if (!id.empty()) {
			const std::size_t index = id.find('.');
			if (index == std::string::npos) {
				store(id, std::string(), map);
			} else {
				store(id.substr(0, index), id.substr(index + 1), map);
			}
		}
	}
}

static std::string firstValue(const StyleSheetTable::AttributeMap &map, const std::string &key) {
	const StyleSheetTable::AttributeMap::const_iterator it = map.find(key);
	if (it == map.end() || it->second.empty()) {
		return std::string();
	}
	return it->second[0];
}

void StyleSheetMultiStyleParser::processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &attributes) {
	ZLLogger::Instance().registerClass("FONT");
	if (name == "@font-face") {
		const std::string family = firstValue(attributes, "font-family");
		if (family.empty()) {
			ZLLogger::Instance().println("FONT", "Font family not specified in @font-face entry");
			return;
		}
		const StyleSheetTable::AttributeMap::const_iterator it = attributes.find("src");
		std::string url;
		if (it != attributes.end()) {
			for (std::vector<std::string>::const_iterator jt = it->second.begin(); jt != it->second.end(); ++jt) {
				if (ZLStringUtil::stringStartsWith(*jt, "url(") &&
						ZLStringUtil::stringEndsWith(*jt, ")")) {
					url = jt->substr(4, jt->size() - 5);
					if (url.size() > 2 && url[0] == url[url.size() - 1]) {
						if (url[0] == '\'' || url[0] == '"') {
							url = url.substr(1, url.size() - 2);
						}
					}
					break;
				}
			}
		}
		if (url.empty()) {
			ZLLogger::Instance().println("FONT", "Source not specified for " + family);
			return;
		}
		const ZLFile fontFile(myPathPrefix + MiscUtil::decodeHtmlURL(url));
		const bool bold = firstValue(attributes, "font-weight") == "bold";
		const bool italic = firstValue(attributes, "font-style") == "italic";
		ZLLogger::Instance().println("FONT", family + " => " + fontFile.path());
	}
}

void StyleSheetMultiStyleParser::parseStream(ZLInputStream &stream) {
	if (stream.open()) {
		char *buffer = new char[1024];
		while (true) {
			int len = stream.read(buffer, 1024);
			if (len == 0) {
				break;
			}
			parse(buffer, len);
		}
		delete[] buffer;
		stream.close();
	}
}

StyleSheetTableParser::StyleSheetTableParser(const std::string &pathPrefix, StyleSheetTable &table) : StyleSheetMultiStyleParser(pathPrefix), myTable(table) {
}

void StyleSheetTableParser::store(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map) {
	myTable.addMap(tag, aClass, map);
}

StyleSheetParserWithCache::StyleSheetParserWithCache(const std::string &pathPrefix) : StyleSheetMultiStyleParser(pathPrefix) {
}

void StyleSheetParserWithCache::store(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map) {
	myEntries.push_back(new Entry(tag, aClass, map));
}

void StyleSheetParserWithCache::applyToTable(StyleSheetTable &table) const {
	for (std::list<shared_ptr<Entry> >::const_iterator it = myEntries.begin(); it != myEntries.end(); ++it) {
		const Entry &entry = **it;
		table.addMap(entry.Tag, entry.Class, entry.Map);
	}
}
