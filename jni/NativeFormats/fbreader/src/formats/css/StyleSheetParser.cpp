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

#include <cctype>
#include <cstring>

#include <ZLStringUtil.h>
#include <ZLInputStream.h>

#include "StyleSheetParser.h"

StyleSheetTableParser::StyleSheetTableParser(StyleSheetTable &table) : myTable(table) {
}

void StyleSheetTableParser::storeData(const std::string &tagName, const std::string &className, const StyleSheetTable::AttributeMap &map) {
	myTable.addMap(tagName, className, map);
}

shared_ptr<ZLTextStyleEntry> StyleSheetSingleStyleParser::parseString(const char *text) {
	myReadState = ATTRIBUTE_NAME;
	parse(text, strlen(text), true);
	shared_ptr<ZLTextStyleEntry> control = StyleSheetTable::createControl(myMap);
	reset();
	return control;
}

StyleSheetParser::StyleSheetParser() : myReadState(TAG_NAME), myInsideComment(false) {
}

StyleSheetParser::~StyleSheetParser() {
}

void StyleSheetParser::reset() {
	myWord.erase();
	myAttributeName.erase();
	myReadState = TAG_NAME;
	myInsideComment = false;
	myTagName.erase();
	myClassName.erase();
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
	switch (symbol) {
		case '{':
		case '}':
		case ';':
		case ':':
			return true;
		default:
			return false;
	}
}

void StyleSheetParser::storeData(const std::string&, const std::string&, const StyleSheetTable::AttributeMap&) {
}

void StyleSheetParser::processControl(const char control) {
	switch (control) {
		case '{':
			myReadState = (myReadState == TAG_NAME) ? ATTRIBUTE_NAME : BROKEN;
			break;
		case '}':
			if (myReadState != BROKEN) {
				storeData(myTagName, myClassName, myMap);
			}
			myReadState = TAG_NAME;
			myTagName.erase();
			myClassName.erase();
			myMap.clear();
			break;
		case ';':
			myReadState =
				((myReadState == ATTRIBUTE_VALUE) ||
				 (myReadState == ATTRIBUTE_NAME)) ? ATTRIBUTE_NAME : BROKEN;
			break;
		case ':':
			myReadState = (myReadState == ATTRIBUTE_NAME) ? ATTRIBUTE_VALUE : BROKEN;
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
		case TAG_NAME:
		{
			int index = word.find('.');
			if (index == -1) {
				if (myTagName.empty()) {
					myTagName = word;
				} else {
					myTagName += ' ' + word;
				}
			} else {
				if (myTagName.empty()) {
					myTagName = word.substr(0, index);
					myClassName = word.substr(index + 1);
				} else {
					myTagName += ' ' + word.substr(0, index);
					myClassName += ' ' + word.substr(index + 1);
				}
			}
			myMap.clear();
			break;
		}
		case ATTRIBUTE_NAME:
			myAttributeName = word;
			myMap[myAttributeName].clear();
			break;
		case ATTRIBUTE_VALUE:
			myMap[myAttributeName].push_back(word);
			break;
		case BROKEN:
			break;
	}
}
