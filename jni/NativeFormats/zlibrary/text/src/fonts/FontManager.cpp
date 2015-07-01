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

#include <ZLStringUtil.h>

#include "FontManager.h"

std::string FontManager::put(const std::string &family, shared_ptr<FontEntry> entry) {
	shared_ptr<FontEntry> existing = myEntries[family];
	if (existing.isNull() || *existing == *entry) {
		myEntries[family] = entry;
		return family;
	}

	for (std::map<std::string,shared_ptr<FontEntry> >::const_iterator it = myEntries.begin(); it != myEntries.end(); ++it) {
		if (*it->second == *entry) {
			return it->first;
		}
	}

	for (int i = 1; i < 1000; ++i) {
		std::string indexed = family + "#";
		ZLStringUtil::appendNumber(indexed, i);
		if (myEntries[indexed].isNull()) {
			myEntries[indexed] = entry;
			return indexed;
		}
	}

	return std::string();
}

int FontManager::familyListIndex(const std::vector<std::string> &familyList) {
	std::vector<std::vector<std::string> >::const_iterator it =
		std::find(myFamilyLists.begin(), myFamilyLists.end(), familyList);
	if (it == myFamilyLists.end()) {
		myFamilyLists.push_back(familyList);
		return myFamilyLists.size() - 1;
	} else {
		return it - myFamilyLists.begin();
	}
}

const std::map<std::string,shared_ptr<FontEntry> > &FontManager::entries() const {
	return myEntries;
}

const std::vector<std::vector<std::string> > &FontManager::familyLists() const {
	return myFamilyLists;
}
