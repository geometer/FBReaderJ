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
#include <cstring>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLStringUtil.h>
#include <ZLLogger.h>

#include "StyleSheetParser.h"
#include "StyleSheetUtil.h"
#include "StringInputStream.h"
#include "CSSInputStream.h"
#include "../util/MiscUtil.h"

StyleSheetParser::StyleSheetParser(const std::string &pathPrefix) : myPathPrefix(pathPrefix) {
	//ZLLogger::Instance().registerClass("CSS-IMPORT");
	ZLLogger::Instance().registerClass("CSS-SELECTOR");
	reset();
}

StyleSheetParser::~StyleSheetParser() {
}

void StyleSheetParser::reset() {
	myWord.erase();
	myAttributeName.erase();
	myReadState = WAITING_FOR_SELECTOR;
	mySelectorString.erase();
	myMap.clear();
	myImportVector.clear();
	myFirstRuleProcessed = false;
}

void StyleSheetParser::parseString(const char *data, std::size_t len) {
	parseStream(new StringInputStream(data, len));
}

void StyleSheetParser::parseStream(shared_ptr<ZLInputStream> stream) {
	stream = new CSSInputStream(stream);
	if (stream->open()) {
		char *buffer = new char[1024];
		while (true) {
			int len = stream->read(buffer, 1024);
			if (len == 0) {
				break;
			}
			parse(buffer, len);
		}
		delete[] buffer;
		stream->close();
	}
}

void StyleSheetParser::parse(const char *text, int len, bool final) {
	const char *start = text;
	const char *end = text + len;
	for (const char *ptr = start; ptr != end; ++ptr) {
		if (myReadState != ATTRIBUTE_VALUE && std::isspace(*ptr)) {
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
		case IMPORT:
			return symbol == ';';
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

std::string StyleSheetParser::url2FullPath(const std::string &url) const {
	std::string path = url;
	if (ZLStringUtil::stringStartsWith(path, "url(") &&
			ZLStringUtil::stringEndsWith(path, ")")) {
		path = path.substr(4, path.size() - 5);
	}
	if (path.size() > 1 && (path[0] == '"' || path[0] == '\'') && path[0] == path[path.size() - 1]) {
		path = path.substr(1, path.size() - 2);
	}
	return myPathPrefix + MiscUtil::decodeHtmlURL(path);
}

void StyleSheetParser::importCSS(const std::string &path) {
}

void StyleSheetParser::processControl(const char control) {
	switch (myReadState) {
		case WAITING_FOR_SELECTOR:
			break;
		case SELECTOR:
			switch (control) {
				case '{':
					myReadState = WAITING_FOR_ATTRIBUTE;
					myFirstRuleProcessed = true;
					break;
				case ';':
					myReadState = WAITING_FOR_SELECTOR;
					mySelectorString.erase();
					break;
			}
			break;
		case IMPORT:
			if (control == ';') {
				if (!myImportVector.empty()) {
					if (myFirstRuleProcessed) {
						ZLLogger::Instance().println(
							"CSS-IMPORT", "Ignore import after style rule " + myImportVector[0]
						);
					} else {
						importCSS(url2FullPath(myImportVector[0]));
					}
					myImportVector.clear();
				}
				myReadState = WAITING_FOR_SELECTOR;
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

void StyleSheetParser::processWord(const std::string &word) {
	if (word.empty()) {
		return;
	}

	switch (myReadState) {
		case WAITING_FOR_SELECTOR:
			mySelectorString = word;
			if (word == "@import") {
				myReadState = IMPORT;
			} else {
				myReadState = SELECTOR;
			}
			break;
		case SELECTOR:
			mySelectorString += ' ' + word;
			break;
		case IMPORT:
			myImportVector.push_back(word);
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
			std::string stripped = word;
			ZLStringUtil::stripWhiteSpaces(stripped);
			std::string &current = myMap[myAttributeName];
			if (current.size() == 0) {
				current = stripped;
			} else {
				current += ' ' + stripped;
			}
			break;
		}
	}
}

StyleSheetSingleStyleParser::StyleSheetSingleStyleParser(const std::string &pathPrefix) : StyleSheetParser(pathPrefix) {
}

shared_ptr<ZLTextStyleEntry> StyleSheetSingleStyleParser::parseSingleEntry(const char *text) {
	myReadState = WAITING_FOR_ATTRIBUTE;
	parse(text, std::strlen(text), true);
	shared_ptr<ZLTextStyleEntry> control = StyleSheetTable::createOrUpdateControl(myMap);
	reset();
	return control;
}

StyleSheetMultiStyleParser::StyleSheetMultiStyleParser(const std::string &pathPrefix, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap) : StyleSheetParser(pathPrefix), myFontMap(fontMap.isNull() ? new FontMap() : fontMap), myEncryptionMap(encryptionMap) {
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

	const std::vector<std::string> ids = ZLStringUtil::split(s, ",", true);
	for (std::vector<std::string>::const_iterator it = ids.begin(); it != ids.end(); ++it) {
		shared_ptr<CSSSelector> selector = CSSSelector::parse(*it);
		if (!selector.isNull()) {
			store(selector, map);
		}
	}
}

static std::string value(const StyleSheetTable::AttributeMap &map, const std::string &key) {
	const StyleSheetTable::AttributeMap::const_iterator it = map.find(key);
	if (it == map.end() || it->second.empty()) {
		return std::string();
	}
	return it->second;
}

void StyleSheetMultiStyleParser::processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &attributes) {
	//ZLLogger::Instance().registerClass("FONT");
	if (name == "@font-face") {
		std::string family = value(attributes, "font-family");
		if (family.empty()) {
			ZLLogger::Instance().println("FONT", "Font family not specified in @font-face entry");
			return;
		}
		family = StyleSheetUtil::strip(family);

		const StyleSheetTable::AttributeMap::const_iterator it = attributes.find("src");
		std::string path;
		if (it != attributes.end()) {
			// TODO: better split
			const std::vector<std::string> ids = ZLStringUtil::split(it->second, " ", true);
			for (std::vector<std::string>::const_iterator jt = ids.begin(); jt != ids.end(); ++jt) {
				if (ZLStringUtil::stringStartsWith(*jt, "url(") &&
						ZLStringUtil::stringEndsWith(*jt, ")")) {
					path = ZLFile(url2FullPath(*jt)).path();
					break;
				}
			}
		}
		if (path.empty()) {
			ZLLogger::Instance().println("FONT", "Source not specified for " + family);
			return;
		}

		const std::string weight = value(attributes, "font-weight");
		const std::string style = value(attributes, "font-style");

		myFontMap->append(
			family,
			weight == "bold",
			style == "italic" || style == "oblique",
			path,
			myEncryptionMap.isNull() ? 0 : myEncryptionMap->info(path)
		);
	}
}

StyleSheetTableParser::StyleSheetTableParser(const std::string &pathPrefix, StyleSheetTable &styleTable, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap) : StyleSheetMultiStyleParser(pathPrefix, fontMap, encryptionMap), myStyleTable(styleTable) {
}

void StyleSheetTableParser::store(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map) {
	myStyleTable.addMap(selector, map);
}

StyleSheetParserWithCache::StyleSheetParserWithCache(const ZLFile &file, const std::string &pathPrefix, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap) : StyleSheetMultiStyleParser(pathPrefix, fontMap, encryptionMap) {
	myProcessedFiles.insert(file.path());
}

void StyleSheetParserWithCache::store(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map) {
	myEntries.push_back(new Entry(selector, map));
}

void StyleSheetParserWithCache::importCSS(const std::string &path) {
	const ZLFile fileToImport(path);
	if (myProcessedFiles.find(fileToImport.path()) != myProcessedFiles.end()) {
		ZLLogger::Instance().println(
			"CSS-IMPORT", "File " + fileToImport.path() + " is already processed, do skip"
		);
		return;
	}
	ZLLogger::Instance().println("CSS-IMPORT", "Go to process imported file " + fileToImport.path());
	shared_ptr<ZLInputStream> stream = fileToImport.inputStream(myEncryptionMap);
	if (!stream.isNull()) {
		StyleSheetParserWithCache importParser(fileToImport, myPathPrefix, myFontMap, myEncryptionMap);
		importParser.myProcessedFiles.insert(myProcessedFiles.begin(), myProcessedFiles.end());
		importParser.parseStream(stream);
		myEntries.insert(myEntries.end(), importParser.myEntries.begin(), importParser.myEntries.end());
	}
	myProcessedFiles.insert(fileToImport.path());
}

void StyleSheetParserWithCache::applyToTables(StyleSheetTable &table, FontMap &fontMap) const {
	for (std::list<shared_ptr<Entry> >::const_iterator it = myEntries.begin(); it != myEntries.end(); ++it) {
		const Entry &entry = **it;
		table.addMap(entry.Selector, entry.Map);
	}
	fontMap.merge(*myFontMap);
}
