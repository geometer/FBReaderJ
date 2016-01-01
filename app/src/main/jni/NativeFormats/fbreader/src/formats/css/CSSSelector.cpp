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

#include <cctype>

#include "CSSSelector.h"

CSSSelector::CSSSelector(const std::string &tag, const std::string &clazz) {
	Tag = tag;
	Class = clazz;
}

CSSSelector::CSSSelector(const std::string &simple) {
	const std::size_t index = simple.find('.');
	if (index == std::string::npos) {
		Tag = simple;
	} else {
		Tag = simple.substr(0, index);
		Class = simple.substr(index + 1);
	}
}

CSSSelector::Component::Component(Relation delimiter, shared_ptr<CSSSelector> selector) : Delimiter(delimiter), Selector(selector) {
}

void CSSSelector::update(shared_ptr<CSSSelector> &selector, const char *&start, const char *end, char delimiter) {
	shared_ptr<CSSSelector> newSelector = new CSSSelector(std::string(start, end - start));
	if (!selector.isNull()) {
		Relation rel = Ancestor;
		switch (delimiter) {
			case '+':
				rel = Previous;
				break;
			case '~':
				rel = Predecessor;
				break;
			case '>':
				rel = Parent;
				break;
		}
		newSelector->Next = new CSSSelector::Component(rel, selector);
	}
	selector = newSelector;
	start = 0;
}

shared_ptr<CSSSelector> CSSSelector::parse(const std::string &data) {
	shared_ptr<CSSSelector> selector;

	const char *start = data.data();
	const char *end = start + data.size();
	const char *wordStart = 0;
	char delimiter = '?';

	for (const char *ptr = start; ptr < end; ++ptr) {
		if (*ptr == '+' || *ptr == '>' || *ptr == '~') {
			if (wordStart != 0) {
				update(selector, wordStart, ptr, delimiter);
			}
			delimiter = *ptr;
		} else if (std::isspace(*ptr)) {
			if (wordStart != 0) {
				update(selector, wordStart, ptr, delimiter);
				delimiter = ' ';
			}
		} else if (wordStart == 0) {
			wordStart = ptr;
		}
	}
	if (wordStart != 0) {
		update(selector, wordStart, end, delimiter);
	}

	return selector;
}

bool CSSSelector::weakEquals(const CSSSelector &selector) const {
	return Tag == selector.Tag && Class == selector.Class;
}

bool CSSSelector::operator < (const CSSSelector &selector) const {
	int diff = Tag.compare(selector.Tag);
	if (diff != 0) {
		return diff < 0;
	}
	diff = Class.compare(selector.Class);
	if (diff != 0) {
		return diff < 0;
	}
	if (selector.Next.isNull()) {
		return false;
	}
	if (Next.isNull()) {
		return true;
	}
	diff = Next->Delimiter - selector.Next->Delimiter;
	if (diff != 0) {
		return diff < 0;
	}
	return *(Next->Selector) < *(selector.Next->Selector);
}
