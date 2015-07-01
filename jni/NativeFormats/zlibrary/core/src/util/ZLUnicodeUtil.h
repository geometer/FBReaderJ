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

#ifndef __ZLUNICODEUTIL_H__
#define __ZLUNICODEUTIL_H__

#include <stdint.h>
#include <string>
#include <vector>

class ZLUnicodeUtil {

private:
	ZLUnicodeUtil();

public:
	typedef uint16_t Ucs2Char;
	typedef std::vector<Ucs2Char> Ucs2String;
	typedef uint32_t Ucs4Char;
	typedef std::vector<Ucs4Char> Ucs4String;

	enum Breakable {
		NO_BREAKABLE,
		BREAKABLE_BEFORE,
		BREAKABLE_AFTER
	};

	static bool isUtf8String(const char *str, int len);
	static bool isUtf8String(const std::string &str);
	static void cleanUtf8String(std::string &str);
	static int utf8Length(const char *str, int len);
	static int utf8Length(const std::string &str);
	static int length(const char *str, int utf8Length);
	static int length(const std::string &str, int utf8Length);
	static void utf8ToUcs4(Ucs4String &to, const char *from, int length, int toLength = -1);
	static void utf8ToUcs4(Ucs4String &to, const std::string &from, int toLength = -1);
	static void utf8ToUcs2(Ucs2String &to, const char *from, int length, int toLength = -1);
	static void utf8ToUcs2(Ucs2String &to, const std::string &from, int toLength = -1);
	static std::size_t firstChar(Ucs4Char &ch, const char *utf8String);
	static std::size_t firstChar(Ucs4Char &ch, const std::string &utf8String);
	static std::size_t lastChar(Ucs4Char &ch, const char *utf8String);
	static std::size_t lastChar(Ucs4Char &ch, const std::string &utf8String);
	static void ucs4ToUtf8(std::string &to, const Ucs4String &from, int toLength = -1);
	static int ucs4ToUtf8(char *to, Ucs4Char ch);
	static void ucs2ToUtf8(std::string &to, const Ucs2String &from, int toLength = -1);
	static int ucs2ToUtf8(char *to, Ucs2Char ch);
	//static bool isLetter(Ucs4Char ch);
	static bool isSpace(Ucs4Char ch);
	static bool isNBSpace(Ucs4Char ch);
	static Breakable isBreakable(Ucs4Char ch);

	//static Ucs4Char toLower(Ucs4Char ch);
	//static void toLower(Ucs4String &str);
	static std::string toLower(const std::string &utf8String);

	//static Ucs4Char toUpper(Ucs4Char ch);
	//static void toUpper(Ucs4String &str);
	static std::string toUpper(const std::string &utf8String);

	static void utf8Trim(std::string &utf8String);
};

inline bool ZLUnicodeUtil::isNBSpace(Ucs4Char ch) {
	return ch == 160;
}

#endif /* __ZLUNICODEUTIL_H__ */
