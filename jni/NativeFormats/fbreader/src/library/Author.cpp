/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <ZLUnicodeUtil.h>

#include "Author.h"

std::set<shared_ptr<Author>,AuthorComparator> Author::ourAuthorSet;

shared_ptr<Author> Author::getAuthor(const std::string &name, const std::string &sortKey) {
	std::string strippedName = name;
	ZLUnicodeUtil::utf8Trim(strippedName);
	if (strippedName.empty()) {
		return 0;
	}
	std::string strippedKey = sortKey;
	ZLUnicodeUtil::utf8Trim(strippedKey);

	if (strippedKey.empty()) {
		const std::size_t index = strippedName.find(',');
		if (index != std::string::npos) {
			strippedKey = strippedName.substr(0, index);
			ZLUnicodeUtil::utf8Trim(strippedKey);
		}
	}

	if (strippedKey.empty()) {
		std::size_t index = strippedName.rfind(' ');
		if (index == std::string::npos) {
			strippedKey = strippedName;
		} else {
			strippedKey = strippedName.substr(index + 1);
			const std::size_t size = strippedName.size();
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
