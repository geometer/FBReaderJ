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
#include <cstdio>
#include <cstdlib>
#include <locale.h>

#include "ZLStringUtil.h"

bool ZLStringUtil::stringEndsWith(const std::string &str, const std::string &end) {
	return
		(end.length() <= str.length()) &&
#if __GNUC__ == 2
		(str.compare(end, str.length() - end.length(), end.length()) == 0);
#else
		(str.compare(str.length() - end.length(), end.length(), end) == 0);
#endif
}

bool ZLStringUtil::stringStartsWith(const std::string &str, const std::string &start) {
	return
		(start.length() <= str.length()) &&
#if __GNUC__ == 2
		(str.compare(start, 0, start.length()) == 0);
#else
		(str.compare(0, start.length(), start) == 0);
#endif
}

void ZLStringUtil::appendNumber(std::string &str, unsigned int n) {
	int len;
	if (n > 0) {
		len = 0;
		for (unsigned int copy = n; copy > 0; copy /= 10) {
			len++;
		}
	} else {
		len = 1;
	}

	str.append(len, '\0');
	char *ptr = (char*)str.data() + str.length() - 1;
	for (int i = 0; i < len; ++i) {
		*ptr-- = '0' + n % 10;
		n /= 10;
	}
}

std::string ZLStringUtil::numberToString(unsigned int n) {
	std::string str;
	appendNumber(str, n);
	return str;
}

void ZLStringUtil::append(std::string &str, const std::vector<std::string> &text) {
	std::size_t len = str.length();
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		len += it->length();
	}
	str.reserve(len);
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		str += *it;
	}
}

void ZLStringUtil::stripWhiteSpaces(std::string &str) {
	std::size_t counter = 0;
	std::size_t length = str.length();
	while (counter < length && std::isspace((unsigned char)str[counter])) {
		counter++;
	}
	str.erase(0, counter);
	length -= counter;

	std::size_t r_counter = length;
	while (r_counter > 0 && std::isspace((unsigned char)str[r_counter - 1])) {
		r_counter--;
	}
	str.erase(r_counter, length - r_counter);
}

std::vector<std::string> ZLStringUtil::split(const std::string &str, const std::string &delimiter, bool skipEmpty) {
	std::vector<std::string> result;
	std::size_t start = 0;
	std::size_t index = str.find(delimiter);
	while (index != std::string::npos) {
		const std::string sub = str.substr(start, index - start);
		if (!skipEmpty || sub.size() > 0) {
			result.push_back(sub);
		}
		start = index + delimiter.length();
		index = str.find(delimiter, start);
	}
	const std::string sub = str.substr(start, index - start);
	if (!skipEmpty || sub.size() > 0) {
		result.push_back(sub);
	}
	return result;
}

std::string ZLStringUtil::join(const std::vector<std::string> &data, const std::string &delimiter) {
	std::string joined;
	bool addDelimiter = false;
	for (std::vector<std::string>::const_iterator it = data.begin(); it != data.end(); ++it) {
		if (addDelimiter) {
			joined += delimiter;
		}
		joined += *it;
		addDelimiter = true;
	}
	return joined;
}

std::string ZLStringUtil::printf(const std::string &format, const std::string &arg0) {
	int index = format.find("%s");
	if (index == -1) {
		return format;
	}
	return format.substr(0, index) + arg0 + format.substr(index + 2);
}

std::string ZLStringUtil::doubleToString(double value) {
	char buf[100];
	setlocale(LC_NUMERIC, "C");
	std::sprintf(buf, "%f", value);
	return buf;
}

double ZLStringUtil::stringToDouble(const std::string &str, double defaultValue) {
	if (!str.empty()) {
		setlocale(LC_NUMERIC, "C");
		return std::atof(str.c_str());
	} else {
		return defaultValue;
	}
}

int ZLStringUtil::parseDecimal(const std::string &str, int defaultValue) {
	if (str.empty()) {
		return defaultValue;
	}
	if (!std::isdigit(str[0]) && (str.length() == 1 || str[0] != '-' || !std::isdigit(str[1]))) {
		return defaultValue;
	}

	for (std::size_t i = 1; i < str.length(); ++i) {
		if (!std::isdigit(str[i])) {
			return defaultValue;
		}
	}

	return std::atoi(str.c_str());
}

unsigned long ZLStringUtil::parseHex(const std::string &str, int defaultValue) {
	if (str.empty()) {
		return defaultValue;
	}

	for (std::size_t i = 0; i < str.length(); ++i) {
		if (!std::isxdigit(str[i])) {
			return defaultValue;
		}
	}

	char *ptr;
	return std::strtol(str.c_str(), &ptr, 16);
}

void ZLStringUtil::asciiToLowerInline(std::string &asciiString) {
	for (int i = asciiString.size() - 1; i >= 0; --i) {
		asciiString[i] = std::tolower(asciiString[i]);
	}
}
