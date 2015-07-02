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

#include <ZLibrary.h>
#include <ZLFileUtil.h>

#include "ZLFSManager.h"
#include "ZLFSDir.h"

ZLFSManager *ZLFSManager::ourInstance = 0;

void ZLFSManager::deleteInstance() {
	if (ourInstance != 0) {
		delete ourInstance;
		ourInstance = 0;
	}
}

int ZLFSManager::findLastFileNameDelimiter(const std::string &path) const {
	int index = findArchiveFileNameDelimiter(path);
	if (index == -1) {
		index = path.rfind(ZLibrary::FileNameDelimiter);
	}
	return index;
}

std::string ZLFSDir::delimiter() const {
	return ZLibrary::FileNameDelimiter;
}

void ZLFSManager::normalize(std::string &path) const {
	int index = findArchiveFileNameDelimiter(path);
	if (index == -1) {
		normalizeRealPath(path);
	} else {
		std::string realPath = path.substr(0, index);
		normalizeRealPath(realPath);
		path = realPath + ':' + ZLFileUtil::normalizeUnixPath(path.substr(index + 1));
	}
}
