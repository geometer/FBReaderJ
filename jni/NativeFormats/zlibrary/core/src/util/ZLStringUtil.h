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

#ifndef __ZLSTRINGUTIL_H__
#define __ZLSTRINGUTIL_H__

#include <vector>
#include <string>

class ZLStringUtil {

private:
	ZLStringUtil();

public:
	static bool stringStartsWith(const std::string &str, const std::string &start);
	static bool stringEndsWith(const std::string &str, const std::string &end);

	static void appendNumber(std::string &str, unsigned int n);
	static std::string numberToString(unsigned int n);
	static void append(std::string &str, const std::vector<std::string> &buffer);
	static void stripWhiteSpaces(std::string &str);

	static std::vector<std::string> split(const std::string &str, const std::string &delimiter, bool skipEmpty);
	static std::string join(const std::vector<std::string> &data, const std::string &delimiter);

	static std::string printf(const std::string &format, const std::string &arg0);

	static std::string doubleToString(double value);
	static double stringToDouble(const std::string &value, double defaultValue);
	static int parseDecimal(const std::string &str, int defaultValue);
	static unsigned long parseHex(const std::string &str, int defaultValue);

	static void asciiToLowerInline(std::string &asciiString);
};

#endif /* __ZLSTRINGUTIL_H__ */
