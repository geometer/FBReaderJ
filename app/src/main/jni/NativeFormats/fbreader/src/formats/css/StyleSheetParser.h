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

#ifndef __STYLESHEETPARSER_H__
#define __STYLESHEETPARSER_H__

#include <list>
#include <set>

#include <shared_ptr.h>
#include <FileEncryptionInfo.h>

#include "StyleSheetTable.h"
#include "CSSSelector.h"
#include "FontMap.h"

class ZLFile;
class ZLInputStream;

class StyleSheetParser {

protected:
	StyleSheetParser(const std::string &pathPrefix);

public:
	virtual ~StyleSheetParser();
	void reset();
	void parseStream(shared_ptr<ZLInputStream> stream);
	void parseString(const char *data, std::size_t len);

protected:
	virtual void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);
	std::string url2FullPath(const std::string &url) const;
	virtual void importCSS(const std::string &path);

private:
	void parse(const char *text, int len, bool final = false);
	bool isControlSymbol(const char symbol);
	void processWord(const std::string &word);
	void processControl(const char control);

protected:
	const std::string myPathPrefix;

private:
	std::string myWord;
	std::string myAttributeName;
	enum {
		WAITING_FOR_SELECTOR,
		SELECTOR,
		IMPORT,
		WAITING_FOR_ATTRIBUTE,
		ATTRIBUTE_NAME,
		ATTRIBUTE_VALUE,
	} myReadState;
	std::string mySelectorString;
	StyleSheetTable::AttributeMap myMap;
	std::vector<std::string> myImportVector;
	bool myFirstRuleProcessed;

friend class StyleSheetSingleStyleParser;
};

class StyleSheetSingleStyleParser : public StyleSheetParser {

public:
	StyleSheetSingleStyleParser(const std::string &pathPrefix);
	shared_ptr<ZLTextStyleEntry> parseSingleEntry(const char *text);
};

class StyleSheetMultiStyleParser : public StyleSheetParser {

protected:
	StyleSheetMultiStyleParser(const std::string &pathPrefix, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap);

protected:
	virtual void store(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map) = 0;

private:
	void storeData(const std::string &selector, const StyleSheetTable::AttributeMap &map);
	void processAtRule(const std::string &name, const StyleSheetTable::AttributeMap &map);

protected:
	shared_ptr<FontMap> myFontMap;
	shared_ptr<EncryptionMap> myEncryptionMap;
};

class StyleSheetTableParser : public StyleSheetMultiStyleParser {

public:
	StyleSheetTableParser(const std::string &pathPrexix, StyleSheetTable &styleTable, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap);

private:
	void store(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map);

private:
	StyleSheetTable &myStyleTable;
};

class StyleSheetParserWithCache : public StyleSheetMultiStyleParser {

private:
	struct Entry {
		shared_ptr<CSSSelector> Selector;
		const StyleSheetTable::AttributeMap Map;

		Entry(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map);
	};

public:
	StyleSheetParserWithCache(const ZLFile &file, const std::string &pathPrefix, shared_ptr<FontMap> fontMap, shared_ptr<EncryptionMap> encryptionMap);
	void applyToTables(StyleSheetTable &table, FontMap &fontMap) const;

private:
	void store(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map);
	void importCSS(const std::string &path);

private:
	std::list<shared_ptr<Entry> > myEntries;
	std::set<std::string> myProcessedFiles;
};

inline StyleSheetParserWithCache::Entry::Entry(shared_ptr<CSSSelector> selector, const StyleSheetTable::AttributeMap &map) : Selector(selector), Map(map) {
}

#endif /* __STYLESHEETPARSER_H__ */
