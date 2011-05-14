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

#include <cstdlib>

#include <ZLStringUtil.h>

#include "StyleSheetTable.h"

bool StyleSheetTable::isEmpty() const {
	return myControlMap.empty() && myPageBreakBeforeMap.empty() && myPageBreakAfterMap.empty();
}

void StyleSheetTable::addMap(const std::string &tag, const std::string &aClass, const AttributeMap &map) {
	if ((!tag.empty() || !aClass.empty()) && !map.empty()) {
		Key key(tag, aClass);
		myControlMap[key] = createControl(map);
		const std::vector<std::string> &pbb = values(map, "page-break-before");
		if (!pbb.empty()) {
			if ((pbb[0] == "always") ||
					(pbb[0] == "left") ||
					(pbb[0] == "right")) {
				myPageBreakBeforeMap[key] = true;
			} else if (pbb[0] == "avoid") {
				myPageBreakBeforeMap[key] = false;
			}
		}
		const std::vector<std::string> &pba = values(map, "page-break-after");
		if (!pba.empty()) {
			if ((pba[0] == "always") ||
					(pba[0] == "left") ||
					(pba[0] == "right")) {
				myPageBreakAfterMap[key] = true;
			} else if (pba[0] == "avoid") {
				myPageBreakAfterMap[key] = false;
			}
		}
	}
}

static void parseLength(const std::string &toParse, short &size, ZLTextStyleEntry::SizeUnit &unit) {
	if (ZLStringUtil::stringEndsWith(toParse, "%")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_PERCENT;
		size = atoi(toParse.c_str());
	} else if (ZLStringUtil::stringEndsWith(toParse, "em")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_EM_100;
		size = (short)(100 * ZLStringUtil::stringToDouble(toParse, 0));
	} else if (ZLStringUtil::stringEndsWith(toParse, "ex")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_EX_100;
		size = (short)(100 * ZLStringUtil::stringToDouble(toParse, 0));
	} else {
		unit = ZLTextStyleEntry::SIZE_UNIT_PIXEL;
		size = atoi(toParse.c_str());
	}
}

void StyleSheetTable::setLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Length name, const AttributeMap &map, const std::string &attributeName) {
	StyleSheetTable::AttributeMap::const_iterator it = map.find(attributeName);
	if (it == map.end()) {
		return;
	}
	const std::vector<std::string> &values = it->second;
	if (!values.empty() && !values[0].empty()) {
		short size;
		ZLTextStyleEntry::SizeUnit unit;
		parseLength(values[0], size, unit);
		entry.setLength(name, size, unit);
	}
}

bool StyleSheetTable::doBreakBefore(const std::string &tag, const std::string &aClass) const {
	std::map<Key,bool>::const_iterator it = myPageBreakBeforeMap.find(Key(tag, aClass));
	if (it != myPageBreakBeforeMap.end()) {
		return it->second;
	}

	it = myPageBreakBeforeMap.find(Key("", aClass));
	if (it != myPageBreakBeforeMap.end()) {
		return it->second;
	}

	it = myPageBreakBeforeMap.find(Key(tag, ""));
	if (it != myPageBreakBeforeMap.end()) {
		return it->second;
	}

	return false;
}

bool StyleSheetTable::doBreakAfter(const std::string &tag, const std::string &aClass) const {
	std::map<Key,bool>::const_iterator it = myPageBreakAfterMap.find(Key(tag, aClass));
	if (it != myPageBreakAfterMap.end()) {
		return it->second;
	}

	it = myPageBreakAfterMap.find(Key("", aClass));
	if (it != myPageBreakAfterMap.end()) {
		return it->second;
	}

	it = myPageBreakAfterMap.find(Key(tag, ""));
	if (it != myPageBreakAfterMap.end()) {
		return it->second;
	}

	return false;
}

shared_ptr<ZLTextStyleEntry> StyleSheetTable::control(const std::string &tag, const std::string &aClass) const {
	std::map<Key,shared_ptr<ZLTextStyleEntry> >::const_iterator it =
		myControlMap.find(Key(tag, aClass));
	return (it != myControlMap.end()) ? it->second : 0;
}

const std::vector<std::string> &StyleSheetTable::values(const AttributeMap &map, const std::string &name) {
	const AttributeMap::const_iterator it = map.find(name);
	if (it != map.end()) {
		return it->second;
	}
	static const std::vector<std::string> emptyVector;
	return emptyVector;
}

shared_ptr<ZLTextStyleEntry> StyleSheetTable::createControl(const AttributeMap &styles) {
	shared_ptr<ZLTextStyleEntry> entry = new ZLTextStyleEntry();

	const std::vector<std::string> &alignment = values(styles, "text-align");
	if (!alignment.empty()) {
		if (alignment[0] == "justify") {
			entry->setAlignmentType(ALIGN_JUSTIFY);
		} else if (alignment[0] == "left") {
			entry->setAlignmentType(ALIGN_LEFT);
		} else if (alignment[0] == "right") {
			entry->setAlignmentType(ALIGN_RIGHT);
		} else if (alignment[0] == "center") {
			entry->setAlignmentType(ALIGN_CENTER);
		}
	}

	const std::vector<std::string> &bold = values(styles, "font-weight");
	if (!bold.empty()) {
		int num = -1;
		if (bold[0] == "bold") {
			num = 700;
		} else if (bold[0] == "normal") {
			num = 400;
		} else if ((bold[0].length() == 3) &&
							 (bold[0][1] == '0') &&
							 (bold[0][2] == '0') &&
							 (bold[0][0] >= '1') &&
							 (bold[0][0] <= '9')) {
			num = 100 * (bold[0][0] - '0');
		} else if (bold[0] == "bolder") {
		} else if (bold[0] == "lighter") {
		}
		if (num != -1) {
			entry->setFontModifier(FONT_MODIFIER_BOLD, num >= 600);
		}
	}

	const std::vector<std::string> &italic = values(styles, "font-style");
	if (!italic.empty()) {
		entry->setFontModifier(FONT_MODIFIER_ITALIC, italic[0] == "italic");
	}

	const std::vector<std::string> &variant = values(styles, "font-variant");
	if (!variant.empty()) {
		entry->setFontModifier(FONT_MODIFIER_SMALLCAPS, variant[0] == "small-caps");
	}

	const std::vector<std::string> &fontFamily = values(styles, "font-family");
	if (!fontFamily.empty() && !fontFamily[0].empty()) {
		entry->setFontFamily(fontFamily[0]);
	}

	const std::vector<std::string> &fontSize = values(styles, "font-size");
	if (!fontSize.empty()) {
		if (fontSize[0] == "xx-small") {
			entry->setFontSizeMag(-3);
		} else if (fontSize[0] == "x-small") {
			entry->setFontSizeMag(-2);
		} else if (fontSize[0] == "small") {
			entry->setFontSizeMag(-1);
		} else if (fontSize[0] == "medium") {
			entry->setFontSizeMag(0);
		} else if (fontSize[0] == "large") {
			entry->setFontSizeMag(1);
		} else if (fontSize[0] == "x-large") {
			entry->setFontSizeMag(2);
		} else if (fontSize[0] == "xx-large") {
			entry->setFontSizeMag(3);
		}
	}

	setLength(*entry, ZLTextStyleEntry::LENGTH_LEFT_INDENT, styles, "margin-left");
	setLength(*entry, ZLTextStyleEntry::LENGTH_RIGHT_INDENT, styles, "margin-right");
	setLength(*entry, ZLTextStyleEntry::LENGTH_FIRST_LINE_INDENT_DELTA, styles, "text-indent");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, styles, "margin-top");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, styles, "padding-top");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, styles, "margin-bottom");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, styles, "padding-bottom");

	return entry;
}
