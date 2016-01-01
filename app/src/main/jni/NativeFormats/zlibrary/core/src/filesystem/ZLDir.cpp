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

#include "ZLFSManager.h"
#include "ZLDir.h"

shared_ptr<ZLDir> ZLDir::root() {
	return ZLFSManager::Instance().rootDirectory();
}

ZLDir::ZLDir(const std::string &path) : myPath(path) {
	ZLFSManager::Instance().normalize(myPath);
}

ZLDir::~ZLDir() {
}

const std::string &ZLDir::path() const {
	return myPath;
}

std::string ZLDir::parentPath() const {
	return ZLFSManager::Instance().parentPath(myPath);
}

bool ZLDir::isRoot() const {
	return myPath == ZLFSManager::Instance().rootDirectoryPath();
}

std::string ZLDir::name() const {
	int index = ZLFSManager::Instance().findLastFileNameDelimiter(myPath);
	return myPath.substr(index + 1);
}

std::string ZLDir::itemPath(const std::string &itemName) const {
	if (itemName == "..") {
		return parentPath();
	} else {
		return isRoot() ? myPath + itemName : myPath + delimiter() + itemName;
	}
}
