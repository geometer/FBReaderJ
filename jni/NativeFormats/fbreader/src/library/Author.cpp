/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>

#include "Author.h"

std::set<shared_ptr<Author>,AuthorComparator> Author::ourAuthorSet;

shared_ptr<Author> Author::getAuthor(const std::string &name, const std::string &sortKey) {
	std::string strippedName = name;
	ZLStringUtil::stripWhiteSpaces(strippedName);
	if (strippedName.empty()) {
		return 0;
	}
	std::string strippedKey = sortKey;
	ZLStringUtil::stripWhiteSpaces(strippedKey);

	if (strippedKey.empty()) {
		const size_t index = strippedName.find(',');
		if (index != std::string::npos) {
			strippedKey = strippedName.substr(0, index);
			ZLStringUtil::stripWhiteSpaces(strippedKey);
		}
	}

	if (strippedKey.empty()) {
		size_t index = strippedName.rfind(' ');
		if (index == std::string::npos) {
			strippedKey = strippedName;
		} else {
			strippedKey = strippedName.substr(index + 1);
			const size_t size = strippedName.size();
			while (index < size && strippedName[index] == ' ') {
				--index;
			}
			strippedName = strippedName.substr(0, index + 1) + ' ' + strippedKey;
		}
	}

	shared_ptr<Author> author =
		new Author(strippedName, ZLUnicodeUtil::toLower(strippedKey));
	std::set<shared_ptr<Author>,AuthorComparator>::const_iterator it =
		ourAuthorSet.find(author);
	if (it != ourAuthorSet.end()) {
		return *it;
	} else {
		ourAuthorSet.insert(author);
		return author;
	}
}
