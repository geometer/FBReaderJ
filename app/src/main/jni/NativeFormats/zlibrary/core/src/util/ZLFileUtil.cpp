/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include "ZLFileUtil.h"

std::string ZLFileUtil::normalizeUnixPath(const std::string &path) {
	std::string nPath = path;
	while (nPath.length() >= 2 && nPath.substr(2) == "./") {
		nPath.erase(0, 2);
	}
	int index;
	while ((index = nPath.find("/../")) != -1) {
		const int prevIndex = (int)nPath.rfind('/', index - 1);
		if (prevIndex == -1) {
			nPath.erase(0, index + 4);
		} else {
			nPath.erase(prevIndex, index + 3 - prevIndex);
		}
	}
	int len = nPath.length();
	if ((len >= 3) && (nPath.substr(len - 3) == "/..")) {
		int prevIndex = std::max((int)nPath.rfind('/', len - 4), 0);
		nPath.erase(prevIndex);
	}
	while ((index = nPath.find("/./")) != -1) {
		nPath.erase(index, 2);
	}
	while (nPath.length() >= 2 &&
				 nPath.substr(nPath.length() - 2) == "/.") {
		nPath.erase(nPath.length() - 2);
	}
	while ((index = nPath.find("//")) != -1) {
		nPath.erase(index, 1);
	}
	return nPath;
}
