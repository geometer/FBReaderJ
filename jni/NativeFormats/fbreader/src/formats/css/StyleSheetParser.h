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

#ifndef __STYLESHEETPARSER_H__
#define __STYLESHEETPARSER_H__

#include <list>

#include "StyleSheetTable.h"

class ZLInputStream;

class StyleSheetParser {

protected:
	StyleSheetParser(const std::string &pathPrefix);

public:
	virtual ~StyleSheetParser();
	void reset();
	void parse(const char *text, int len, bool final = false);

protected:
	virtual void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);

private:
	bool isControlSymbol(const char symbol);
	void processWord(std::string &word);
	void processWordWithoutComments(const std::string &word);
	void processControl(const char control);

protected:
	const std::string myPathPrefix;

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

class StyleSheetSingleStyleParser : public StyleSheetParser {

public:
	StyleSheetSingleStyleParser(const std::string &pathPrefix);
	shared_ptr<ZLTextStyleEntry> parseString(const char *text);
};

class StyleSheetMultiStyleParser : public StyleSheetParser {

protected:
	StyleSheetMultiStyleParser(const std::string &pathPrefix);

public:
	void parseStream(ZLInputStream &stream);

protected:
	virtual void store(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map) = 0;

private:
	void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);
	void processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &map);
};

class StyleSheetTableParser : public StyleSheetMultiStyleParser {

public:
	StyleSheetTableParser(const std::string &pathPrexix, StyleSheetTable &table);

private:
	void store(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map);

private:
	StyleSheetTable &myTable;
};

class StyleSheetParserWithCache : public StyleSheetMultiStyleParser {

private:
	struct Entry {
		const std::string Tag;
		const std::string Class;
		const StyleSheetTable::AttributeMap Map;

		Entry(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map);
	};

public:
	StyleSheetParserWithCache(const std::string &pathPrefix);
	void applyToTable(StyleSheetTable &table) const;

private:
	void store(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map);

private:
	std::list<shared_ptr<Entry> > myEntries;
};

inline StyleSheetParserWithCache::Entry::Entry(const std::string &tag, const std::string &aClass, const StyleSheetTable::AttributeMap &map) : Tag(tag), Class(aClass), Map(map) {
}

#endif /* __STYLESHEETPARSER_H__ */
