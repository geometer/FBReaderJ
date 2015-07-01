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

#ifndef __STYLESHEETTABLE_H__
#define __STYLESHEETTABLE_H__

#include <string>
#include <map>
#include <vector>

#include <shared_ptr.h>
#include <ZLBoolean3.h>

#include <ZLTextParagraph.h>
#include <ZLTextStyleEntry.h>

#include "CSSSelector.h"

class StyleSheetTable {

public:
	typedef std::map<std::string,std::string> AttributeMap;
	static shared_ptr<ZLTextStyleEntry> createOrUpdateControl(const AttributeMap &map, shared_ptr<ZLTextStyleEntry> entry = 0);

private:
	void addMap(shared_ptr<CSSSelector> selector, const AttributeMap &map);

	static void setLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Feature featureId, const AttributeMap &map, const std::string &attributeName);
	static const std::string &value(const AttributeMap &map, const std::string &name);

public:
	bool isEmpty() const;
	ZLBoolean3 doBreakBefore(const std::string &tag, const std::string &aClass) const;
	ZLBoolean3 doBreakAfter(const std::string &tag, const std::string &aClass) const;
	shared_ptr<ZLTextStyleEntry> control(const std::string &tag, const std::string &aClass) const;
	std::vector<std::pair<CSSSelector,shared_ptr<ZLTextStyleEntry> > > allControls(const std::string &tag, const std::string &aClass) const;

	void clear();

private:
	std::map<CSSSelector,shared_ptr<ZLTextStyleEntry> > myControlMap;
	std::map<CSSSelector,bool> myPageBreakBeforeMap;
	std::map<CSSSelector,bool> myPageBreakAfterMap;

friend class StyleSheetTableParser;
friend class StyleSheetParserWithCache;
};

#endif /* __STYLESHEETTABLE_H__ */
