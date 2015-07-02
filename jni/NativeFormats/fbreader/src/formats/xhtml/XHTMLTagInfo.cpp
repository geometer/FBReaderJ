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

#include <algorithm>

#include "XHTMLTagInfo.h"
#include "../css/CSSSelector.h"

XHTMLTagInfo::XHTMLTagInfo(const std::string &tag, const std::vector<std::string> &classes) : Tag(tag), Classes(classes) {
}

bool XHTMLTagInfo::matches(const CSSSelector &selector) const {
	if (selector.Tag == "*") {
		return selector.Class.empty();
	}
	if (!selector.Tag.empty() && selector.Tag != Tag) {
		return false;
	}
	if (selector.Class.empty()) {
		return true;
	}
	return std::find(Classes.begin(), Classes.end(), selector.Class) != Classes.end();
}

int XHTMLTagInfoList::find(const CSSSelector &selector, int from, int to) const {
	if (from < 0) {
		from = std::max(from + (int)size(), 0);
	}
	if (to <= 0) {
		to += size();
	}
	for (int i = std::min(to, (int)size()) - 1; i >= from; --i) {
		if (at(i).matches(selector)) {
			return i;
		}
	}
	return -1;
}

bool XHTMLTagInfoList::matches(const CSSSelector &selector, int index) const {
	return find(selector, index, index + 1) != -1;
}
