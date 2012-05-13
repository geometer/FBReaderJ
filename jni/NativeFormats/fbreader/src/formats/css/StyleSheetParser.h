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

#ifndef __STYLESHEETPARSER_H__
#define __STYLESHEETPARSER_H__

#include "StyleSheetTable.h"

class ZLInputStream;

class StyleSheetParser {

protected:
	StyleSheetParser();

public:
	virtual ~StyleSheetParser();
	void reset();
	void parse(ZLInputStream &stream);
	void parse(const char *text, int len, bool final = false);

protected:
	virtual void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);
	virtual void processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &map);

private:
	bool isControlSymbol(const char symbol);
	void processWord(std::string &word);
	void processWordWithoutComments(const std::string &word);
	void processControl(const char control);

private:
	std::string myWord;
	std::string myAttributeName;
	enum {
		WAITING_FOR_SELECTOR,
		SELECTOR,
		WAITING_FOR_ATTRIBUTE,
		ATTRIBUTE_NAME,
		ATTRIBUTE_VALUE,
	} myReadState;
	bool myInsideComment;
	std::string mySelectorString;
	StyleSheetTable::AttributeMap myMap;

friend class StyleSheetSingleStyleParser;
};

class StyleSheetTableParser : public StyleSheetParser {

public:
	StyleSheetTableParser(StyleSheetTable &table);

private:
	void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);
	void processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &map);

private:
	StyleSheetTable &myTable;
};

class StyleSheetSingleStyleParser : public StyleSheetParser {

public:
	shared_ptr<ZLTextStyleEntry> parseString(const char *text);
};

#endif /* __STYLESHEETPARSER_H__ */
