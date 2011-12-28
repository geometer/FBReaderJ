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

#include "ZLSearchUtil.h"
#include "ZLUnicodeUtil.h"

ZLSearchPattern::ZLSearchPattern(const std::string &pattern, bool ignoreCase) {
	myIgnoreCase = ignoreCase;
	if (myIgnoreCase) {
		myLowerCasePattern = ZLUnicodeUtil::toLower(pattern);
		myUpperCasePattern = ZLUnicodeUtil::toUpper(pattern);
	} else {
		myLowerCasePattern = pattern;
	}
}

int ZLSearchUtil::find(const char *text, size_t length, const ZLSearchPattern &pattern, int pos) {
	if (pattern.ignoreCase()) {
		if (pos < 0) {
			pos = 0;
		}
		const std::string &lower = pattern.lowerCasePattern();
		const std::string &upper = pattern.upperCasePattern();
		const char *last = text + length - pattern.lowerCasePattern().length();
		const char *patternLast = lower.data() + lower.length() - 1;
		for (const char *i = text + pos; i <= last; ++i) {
			const char *j0 = lower.data();
			const char *j1 = upper.data();
			const char *k = i;
			for (; j0 <= patternLast; ++j0, ++j1, ++k) {
				if ((*j0 != *k) && (*j1 != *k)) {
					break;
				}
			}
			if (j0 > patternLast) {
				return i - text;
			}
		}
		return -1;
	} else {
		if (pos < 0) {
			pos = 0;
		}
		const std::string &lower = pattern.lowerCasePattern();
		const char *last = text + length - pattern.lowerCasePattern().length();
		const char *patternLast = lower.data() + lower.length() - 1;
		for (const char *i = text + pos; i <= last; ++i) {
			const char *j0 = lower.data();
			const char *k = i;
			for (; j0 <= patternLast; ++j0, ++k) {
				if (*j0 != *k) {
					break;
				}
			}
			if (j0 > patternLast) {
				return i - text;
			}
		}
		return -1;
	}
}
