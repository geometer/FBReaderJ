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

#ifndef __ZLSEARCHUTIL_H__
#define __ZLSEARCHUTIL_H__

#include <string>

class ZLSearchPattern {

public:
	ZLSearchPattern(const std::string &pattern, bool ignoreCase);
	~ZLSearchPattern();
	int length() const;

private:
	bool ignoreCase() const;
	const std::string &lowerCasePattern() const;
	const std::string &upperCasePattern() const;

private:
	bool myIgnoreCase;
	std::string myLowerCasePattern;
	std::string myUpperCasePattern;

friend class ZLSearchUtil;
};

class ZLSearchUtil {

private:
	ZLSearchUtil();

public:
	static int find(const char *text, size_t length, const ZLSearchPattern &pattern, int pos = 0);
};

inline ZLSearchPattern::~ZLSearchPattern() {}
inline int ZLSearchPattern::length() const { return myLowerCasePattern.length(); }
inline bool ZLSearchPattern::ignoreCase() const { return myIgnoreCase; }
inline const std::string &ZLSearchPattern::lowerCasePattern() const { return myLowerCasePattern; }
inline const std::string &ZLSearchPattern::upperCasePattern() const { return myUpperCasePattern; }

#endif /* __ZLSEARCHUTIL_H__ */
