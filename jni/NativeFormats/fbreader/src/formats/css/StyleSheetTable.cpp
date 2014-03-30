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

#include <cstdlib>

#include <ZLStringUtil.h>

#include "StyleSheetTable.h"

bool StyleSheetTable::isEmpty() const {
	return myControlMap.empty() && myPageBreakBeforeMap.empty() && myPageBreakAfterMap.empty();
}

void StyleSheetTable::addMap(const std::string &tag, const std::string &aClass, const AttributeMap &map) {
	if ((!tag.empty() || !aClass.empty()) && !map.empty()) {
		const Key key(tag, aClass);
		myControlMap[key] = createControl(map);

		const std::string &pbb = value(map, "page-break-before");
		if (pbb == "always" || pbb == "left" || pbb == "right") {
			myPageBreakBeforeMap[key] = true;
		} else if (pbb == "avoid") {
			myPageBreakBeforeMap[key] = false;
		}

		const std::string &pba = value(map, "page-break-after");
		if (pba == "always" || pba == "left" || pba == "right") {
			myPageBreakAfterMap[key] = true;
		} else if (pba == "avoid") {
			myPageBreakAfterMap[key] = false;
		}
	}
}

static bool parseLength(const std::string &toParse, short &size, ZLTextStyleEntry::SizeUnit &unit) {
	if (ZLStringUtil::stringEndsWith(toParse, "%")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_PERCENT;
		size = std::atoi(toParse.c_str());
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "em")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_EM_100;
		size = (short)(100 * ZLStringUtil::stringToDouble(toParse, 0));
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "ex")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_EX_100;
		size = (short)(100 * ZLStringUtil::stringToDouble(toParse, 0));
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "px")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_PIXEL;
		size = std::atoi(toParse.c_str());
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "pt")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_POINT;
		size = std::atoi(toParse.c_str());
		return true;
	}
	return false;
}

void StyleSheetTable::setLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Feature featureId, const AttributeMap &map, const std::string &attributeName) {
	StyleSheetTable::AttributeMap::const_iterator it = map.find(attributeName);
	if (it == map.end()) {
		return;
	}
	short size;
	ZLTextStyleEntry::SizeUnit unit;
	if (parseLength(it->second, size, unit)) {
		entry.setLength(featureId, size, unit);
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
	return it != myControlMap.end() ? it->second : 0;
}

const std::string &StyleSheetTable::value(const AttributeMap &map, const std::string &name) {
	const AttributeMap::const_iterator it = map.find(name);
	if (it != map.end()) {
		return it->second;
	}
	static const std::string emptyString;
	return emptyString;
}

static std::string strip(const std::string &data) {
	std::string res = data;
	ZLStringUtil::stripWhiteSpaces(res);
	if (res.size() > 1 && (res[0] == '"' || res[0] == '\'') && res[0] == res[res.size() - 1]) {
		return res.substr(1, res.size() - 2);
	} else {
		return res;
	}
}

static std::vector<std::string> splitCommaSeparatedList(const std::string &data) {
	std::vector<std::string> split;

	enum {
		S_QUOTED,
		D_QUOTED,
		NORMAL
	} state = NORMAL;

	std::size_t start = 0;
	for (std::size_t i = 0; i < data.size(); ++i) {
		const char ch = data[i];
		switch (state) {
			case NORMAL:
				if (ch == ',') {
					if (i > start) {
						split.push_back(strip(data.substr(start, i - start)));
					}
					start = i + 1;
				}
				break;
			case S_QUOTED:
				if (ch == '\'') {
					state = NORMAL;
				}
				break;
			case D_QUOTED:
				if (ch == '"') {
					state = NORMAL;
				}
				break;
		}
	}

	return split;
}

shared_ptr<ZLTextStyleEntry> StyleSheetTable::createControl(const AttributeMap &styles) {
	shared_ptr<ZLTextStyleEntry> entry = new ZLTextStyleEntry(ZLTextStyleEntry::STYLE_CSS_ENTRY);

	const std::string &alignment = value(styles, "text-align");
	if (alignment == "justify") {
		entry->setAlignmentType(ALIGN_JUSTIFY);
	} else if (alignment == "left") {
		entry->setAlignmentType(ALIGN_LEFT);
	} else if (alignment == "right") {
		entry->setAlignmentType(ALIGN_RIGHT);
	} else if (alignment == "center") {
		entry->setAlignmentType(ALIGN_CENTER);
	}

	const std::string &deco = value(styles, "text-decoration");
	if (deco == "underline") {
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_UNDERLINED, true);
	} else if (deco == "line-through") {
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_STRIKEDTHROUGH, true);
	} else if (deco == "none") {
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_UNDERLINED, false);
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_STRIKEDTHROUGH, false);
	}

	const std::string bold = value(styles, "font-weight");
	if (!bold.empty()) {
		int num = -1;
		if (bold == "bold") {
			num = 700;
		} else if (bold == "normal") {
			num = 400;
		} else if (bold == "bolder") {
			// TODO: implement
		} else if (bold == "lighter") {
			// TODO: implement
		} else {
			num = ZLStringUtil::stringToInteger(bold, -1);
		}
		if (num != -1) {
			entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_BOLD, num >= 600);
		}
	}

	const std::string &italic = value(styles, "font-style");
	if (!italic.empty()) {
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_ITALIC, italic == "italic" || italic == "oblique");
	}

	const std::string &variant = value(styles, "font-variant");
	if (!variant.empty()) {
		entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_SMALLCAPS, variant == "small-caps");
	}

	const std::string &fontFamily = value(styles, "font-family");
	if (!fontFamily.empty()) {
		std::vector<std::string> families = splitCommaSeparatedList(fontFamily);
		// TODO: use all families
		if (!families.empty()) {
			entry->setFontFamily(families[0]);
		}
	}

	const std::string &fontSize = value(styles, "font-size");
	if (!fontSize.empty()) {
		bool doSetFontSize = true; 
		short size = 100;
		ZLTextStyleEntry::SizeUnit unit = ZLTextStyleEntry::SIZE_UNIT_PERCENT;
		if (fontSize == "xx-small") {
			size = 58;
		} else if (fontSize == "x-small") {
			size = 69;
		} else if (fontSize == "small") {
			size = 83;
		} else if (fontSize == "medium") {
			size = 100;
		} else if (fontSize == "large") {
			size = 120;
		} else if (fontSize == "x-large") {
			size = 144;
		} else if (fontSize == "xx-large") {
			size = 173;
		} else if (fontSize == "inherit") {
			entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_INHERIT, true);
			doSetFontSize = false;
		} else if (fontSize == "smaller") {
			entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_SMALLER, true);
			doSetFontSize = false;
		} else if (fontSize == "larger") {
			entry->setFontModifier(ZLTextStyleEntry::FONT_MODIFIER_LARGER, true);
			doSetFontSize = false;
		} else if (!parseLength(fontSize, size, unit)) {
			doSetFontSize = false;
		}
		if (doSetFontSize) {
			entry->setLength(ZLTextStyleEntry::LENGTH_FONT_SIZE, size, unit);
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

void StyleSheetTable::clear() {
	myControlMap.clear();
	myPageBreakBeforeMap.clear();
	myPageBreakAfterMap.clear();
}
