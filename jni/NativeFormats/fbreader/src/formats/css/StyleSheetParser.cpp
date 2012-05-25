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
#include <cstring>

#include <ZLStringUtil.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>

#include "StyleSheetParser.h"

StyleSheetTableParser::StyleSheetTableParser(StyleSheetTable &table) : myTable(table) {
	//ZLLogger::Instance().registerClass("CSS");
}

void StyleSheetTableParser::storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map) {
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
			const size_t index = id.find('.');
			if (index == std::string::npos) {
				myTable.addMap(id, std::string(), map);
			} else {
				myTable.addMap(id.substr(0, index), id.substr(index + 1), map);
			}
		}
	}
}

void StyleSheetTableParser::processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &map) {
	if (name == "@font-face") {
	}
}

shared_ptr<ZLTextStyleEntry> StyleSheetSingleStyleParser::parseString(const char *text) {
	myReadState = WAITING_FOR_ATTRIBUTE;
	parse(text, strlen(text), true);
	shared_ptr<ZLTextStyleEntry> control = StyleSheetTable::createControl(myMap);
	reset();
	return control;
}

StyleSheetParser::StyleSheetParser() {
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

void StyleSheetParser::parse(ZLInputStream &stream) {
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

void StyleSheetParser::parse(const char *text, int len, bool final) {
	const char *start = text;
	const char *end = text + len;
	for (const char *ptr = start; ptr != end; ++ptr) {
		if (isspace(*ptr)) {
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

void StyleSheetParser::processAtRule(const std::string&, const StyleSheetTable::AttributeMap&) {
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
			const size_t l = word.length();
			if (l >= 2 && (word[0] == '"' || word[0] == '\'') && word[0] == word[l - 1]) {
				myMap[myAttributeName].push_back(word.substr(1, l - 2));
			} else {
				myMap[myAttributeName].push_back(word);
			}
			break;
		}
	}
}
