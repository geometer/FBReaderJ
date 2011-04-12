/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

void ZLStringUtil::append(std::string &str, const std::vector<std::string> &text) {
	size_t len = str.length();
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		len += it->length();
	}
	str.reserve(len);
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		str += *it;
	}
}

void ZLStringUtil::stripWhiteSpaces(std::string &str) {
	size_t counter = 0;
	size_t length = str.length();
	while ((counter < length) && isspace((unsigned char)str[counter])) {
		counter++;
	}
	str.erase(0, counter);
	length -= counter;

	size_t r_counter = length;
	while ((r_counter > 0) && isspace((unsigned char)str[r_counter - 1])) {
		r_counter--;
	}
	str.erase(r_counter, length - r_counter);
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
	sprintf(buf, "%f", value);
	return buf;
}

double ZLStringUtil::stringToDouble(const std::string &value, double defaultValue) {
	if (!value.empty()) {
		setlocale(LC_NUMERIC, "C");
		return atof(value.c_str());
	} else {
		return defaultValue;
	}
}
