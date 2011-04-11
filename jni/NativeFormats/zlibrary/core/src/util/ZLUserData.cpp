/*
 * Copyright (C) 2008-2010 Geometer Plus <contact@geometerplus.com>
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

#include "ZLUserData.h"

ZLUserData::~ZLUserData() {
}

ZLUserDataHolder::~ZLUserDataHolder() {
}

void ZLUserDataHolder::addUserData(const std::string &key, shared_ptr<ZLUserData> data) {
	myDataMap[key] = data;
}

void ZLUserDataHolder::removeUserData(const std::string &key) {
	myDataMap.erase(key);
}

shared_ptr<ZLUserData> ZLUserDataHolder::getUserData(const std::string &key) const {
	std::map<std::string,shared_ptr<ZLUserData> >::const_iterator it = myDataMap.find(key);
	return (it != myDataMap.end()) ? it->second : 0;
}
