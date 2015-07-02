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

#include <cstdlib>

#include <ZLStringUtil.h>

#include "StyleSheetTable.h"
#include "StyleSheetUtil.h"
#include "CSSSelector.h"

bool StyleSheetTable::isEmpty() const {
	return myControlMap.empty() && myPageBreakBeforeMap.empty() && myPageBreakAfterMap.empty();
}

void StyleSheetTable::addMap(shared_ptr<CSSSelector> selectorPtr, const AttributeMap &map) {
	if (!selectorPtr.isNull() && !map.empty()) {
		const CSSSelector &selector = *selectorPtr;
		myControlMap[selector] = createOrUpdateControl(map, myControlMap[selector]);

		const std::string &pbb = value(map, "page-break-before");
		if (pbb == "always" || pbb == "left" || pbb == "right") {
			myPageBreakBeforeMap[selector] = true;
		} else if (pbb == "avoid") {
			myPageBreakBeforeMap[selector] = false;
		}

		const std::string &pba = value(map, "page-break-after");
		if (pba == "always" || pba == "left" || pba == "right") {
			myPageBreakAfterMap[selector] = true;
		} else if (pba == "avoid") {
			myPageBreakAfterMap[selector] = false;
		}
	}
}

static bool parseLength(const std::string &toParse, short &size, ZLTextStyleEntry::SizeUnit &unit) {
	if (toParse == "0") {
		unit = ZLTextStyleEntry::SIZE_UNIT_PIXEL;
		size = 0;
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "%")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_PERCENT;
		size = std::atoi(toParse.c_str());
		return true;
	} else if (ZLStringUtil::stringEndsWith(toParse, "rem")) {
		unit = ZLTextStyleEntry::SIZE_UNIT_REM_100;
		size = (short)(100 * ZLStringUtil::stringToDouble(toParse, 0));
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

static bool trySetLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Feature featureId, const std::string &value) {
	short size;
	ZLTextStyleEntry::SizeUnit unit;
	if (::parseLength(value, size, unit)) {
		entry.setLength(featureId, size, unit);
		return true;
	}
	return false;
}

void StyleSheetTable::setLength(ZLTextStyleEntry &entry, ZLTextStyleEntry::Feature featureId, const AttributeMap &map, const std::string &attributeName) {
	StyleSheetTable::AttributeMap::const_iterator it = map.find(attributeName);
	if (it != map.end()) {
		::trySetLength(entry, featureId, it->second);
		return;
	}
}

ZLBoolean3 StyleSheetTable::doBreakBefore(const std::string &tag, const std::string &aClass) const {
	std::map<CSSSelector,bool>::const_iterator it = myPageBreakBeforeMap.find(CSSSelector(tag, aClass));
	if (it != myPageBreakBeforeMap.end()) {
		return b3Value(it->second);
	}

	it = myPageBreakBeforeMap.find(CSSSelector("", aClass));
	if (it != myPageBreakBeforeMap.end()) {
		return b3Value(it->second);
	}

	it = myPageBreakBeforeMap.find(CSSSelector(tag, ""));
	if (it != myPageBreakBeforeMap.end()) {
		return b3Value(it->second);
	}

	return B3_UNDEFINED;
}

ZLBoolean3 StyleSheetTable::doBreakAfter(const std::string &tag, const std::string &aClass) const {
	std::map<CSSSelector,bool>::const_iterator it = myPageBreakAfterMap.find(CSSSelector(tag, aClass));
	if (it != myPageBreakAfterMap.end()) {
		return b3Value(it->second);
	}

	it = myPageBreakAfterMap.find(CSSSelector("", aClass));
	if (it != myPageBreakAfterMap.end()) {
		return b3Value(it->second);
	}

	it = myPageBreakAfterMap.find(CSSSelector(tag, ""));
	if (it != myPageBreakAfterMap.end()) {
		return b3Value(it->second);
	}

	return B3_UNDEFINED;
}

shared_ptr<ZLTextStyleEntry> StyleSheetTable::control(const std::string &tag, const std::string &aClass) const {
	std::map<CSSSelector,shared_ptr<ZLTextStyleEntry> >::const_iterator it =
		myControlMap.find(CSSSelector(tag, aClass));
	return it != myControlMap.end() ? it->second : 0;
}

std::vector<std::pair<CSSSelector,shared_ptr<ZLTextStyleEntry> > > StyleSheetTable::allControls(const std::string &tag, const std::string &aClass) const {
	const CSSSelector key(tag, aClass);
	std::vector<std::pair<CSSSelector,shared_ptr<ZLTextStyleEntry> > > pairs;

	std::map<CSSSelector,shared_ptr<ZLTextStyleEntry> >::const_iterator it =
		myControlMap.lower_bound(key);
	for (std::map<CSSSelector,shared_ptr<ZLTextStyleEntry> >::const_iterator jt = it; jt != myControlMap.end() && key.weakEquals(jt->first); ++jt) {
		pairs.push_back(*jt);
	}
	return pairs;
}

const std::string &StyleSheetTable::value(const AttributeMap &map, const std::string &name) {
	const AttributeMap::const_iterator it = map.find(name);
	if (it != map.end()) {
		return it->second;
	}
	static const std::string emptyString;
	return emptyString;
}

shared_ptr<ZLTextStyleEntry> StyleSheetTable::createOrUpdateControl(const AttributeMap &styles, shared_ptr<ZLTextStyleEntry> entry) {
	if (entry.isNull()) {
		entry = new ZLTextStyleEntry(ZLTextStyleEntry::STYLE_CSS_ENTRY);
	}

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
			num = ZLStringUtil::parseDecimal(bold, -1);
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
		entry->setFontFamilies(StyleSheetUtil::splitCommaSeparatedList(fontFamily));
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
		} else if (!::parseLength(fontSize, size, unit)) {
			doSetFontSize = false;
		}
		if (doSetFontSize) {
			entry->setLength(ZLTextStyleEntry::LENGTH_FONT_SIZE, size, unit);
		}
	}

	const std::string margin = value(styles, "margin");
	if (!margin.empty()) {
		std::vector<std::string> split = ZLStringUtil::split(margin, " ", true);
		if (split.size() > 0) {
			switch (split.size()) {
				case 1:
					split.push_back(split[0]);
					// go through
				case 2:
					split.push_back(split[0]);
					// go through
				case 3:
					split.push_back(split[1]);
					break;
			}
		}
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, split[0]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_MARGIN_RIGHT, split[1]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, split[2]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_MARGIN_LEFT, split[3]);
	}
	const std::string padding = value(styles, "padding");
	if (!padding.empty()) {
		std::vector<std::string> split = ZLStringUtil::split(padding, " ", true);
		if (split.size() > 0) {
			switch (split.size()) {
				case 1:
					split.push_back(split[0]);
					// go through
				case 2:
					split.push_back(split[0]);
					// go through
				case 3:
					split.push_back(split[1]);
					break;
			}
		}
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, split[0]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_PADDING_RIGHT, split[1]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, split[2]);
		::trySetLength(*entry, ZLTextStyleEntry::LENGTH_PADDING_LEFT, split[3]);
	}
	setLength(*entry, ZLTextStyleEntry::LENGTH_MARGIN_LEFT, styles, "margin-left");
	setLength(*entry, ZLTextStyleEntry::LENGTH_MARGIN_RIGHT, styles, "margin-right");
	setLength(*entry, ZLTextStyleEntry::LENGTH_PADDING_LEFT, styles, "padding-left");
	setLength(*entry, ZLTextStyleEntry::LENGTH_PADDING_RIGHT, styles, "padding-right");
	setLength(*entry, ZLTextStyleEntry::LENGTH_FIRST_LINE_INDENT, styles, "text-indent");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, styles, "margin-top");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_BEFORE, styles, "padding-top");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, styles, "margin-bottom");
	setLength(*entry, ZLTextStyleEntry::LENGTH_SPACE_AFTER, styles, "padding-bottom");

	const std::string verticalAlign = value(styles, "vertical-align");
	if (!verticalAlign.empty()) {
		static const char* values[] = { "sub", "super", "top", "text-top", "middle", "bottom", "text-bottom", "initial", "inherit" };
		int index = sizeof(values) / sizeof(const char*) - 1;
		for (; index >= 0; --index) {
			if (verticalAlign == values[index]) {
				break;
			}
		}
		if (index >= 0) {
			entry->setVerticalAlignCode((unsigned char)index);
		} else {
			::trySetLength(*entry, ZLTextStyleEntry::LENGTH_VERTICAL_ALIGN, verticalAlign);
		}
	}

	entry->setDisplayCode(StyleSheetUtil::displayCode(value(styles, "display")));

	return entry;
}

void StyleSheetTable::clear() {
	myControlMap.clear();
	myPageBreakBeforeMap.clear();
	myPageBreakAfterMap.clear();
}
