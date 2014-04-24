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

#ifndef __STYLESHEETTABLE_H__
#define __STYLESHEETTABLE_H__

#include <string>
#include <map>
#include <vector>

#include <shared_ptr.h>

#include <ZLTextParagraph.h>
#include <ZLTextStyleEntry.h>

class StyleSheetTable {

public:
	typedef std::map<std::string,std::string> AttributeMap;
	static shared_ptr<ZLTextStyleEntry> createControl(const AttributeMap &map);

private:
	void addMap(const std::string &tag, const std::string &aClass, const AttributeMap &map);

	static void setLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Feature featureId, const AttributeMap &map, const std::string &attributeName);
	static const std::string &value(const AttributeMap &map, const std::string &name);

public:
	bool isEmpty() const;
	bool doBreakBefore(const std::string &tag, const std::string &aClass) const;
	bool doBreakAfter(const std::string &tag, const std::string &aClass) const;
	shared_ptr<ZLTextStyleEntry> control(const std::string &tag, const std::string &aClass) const;

	void clear();

private:
	struct Key {
		Key(const std::string &tag, const std::string &aClass);

		const std::string TagName;
		const std::string ClassName;

		bool operator < (const Key &key) const;
	};

	std::map<Key,shared_ptr<ZLTextStyleEntry> > myControlMap;
	std::map<Key,bool> myPageBreakBeforeMap;
	std::map<Key,bool> myPageBreakAfterMap;

friend class StyleSheetTableParser;
friend class StyleSheetParserWithCache;
};

inline StyleSheetTable::Key::Key(const std::string &tag, const std::string &aClass) : TagName(tag), ClassName(aClass) {
}

inline bool StyleSheetTable::Key::operator < (const StyleSheetTable::Key &key) const {
	return (TagName < key.TagName) || ((TagName == key.TagName) && (ClassName < key.ClassName));
}

#endif /* __STYLESHEETTABLE_H__ */
