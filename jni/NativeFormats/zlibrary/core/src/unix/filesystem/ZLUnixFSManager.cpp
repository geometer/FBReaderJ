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

#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include <set>

#include <ZLStringUtil.h>

#include "ZLUnixFSManager.h"
#include "ZLUnixFSDir.h"
#include "ZLUnixFileInputStream.h"
#include "ZLUnixFileOutputStream.h"

static std::string getPwdDir() {
	char *pwd = getenv("PWD");
	return (pwd != 0) ? pwd : "";
}

static std::string getHomeDir() {
	char *home = getenv("HOME");
	return (home != 0) ? home : "";
}

ZLFileInfo ZLUnixFSManager::fileInfo(const std::string &path) const {
	ZLFileInfo info;
	struct stat fileStat;
	info.Exists = stat(path.c_str(), &fileStat) == 0;
	if (info.Exists) {
		info.Size = fileStat.st_size;
		info.MTime = fileStat.st_mtime;
		info.IsDirectory = S_ISDIR(fileStat.st_mode);
	}
	return info;
}

std::string ZLUnixFSManager::resolveSymlink(const std::string &path) const {
	std::set<std::string> names;
	std::string current = path;
	for (int i = 0; i < 256; ++i) {
		names.insert(current);

		std::string buffer(2048, '\0');
		int len = readlink(current.c_str(), (char*)buffer.data(), 2048);
		if ((len == 2048) || (len <= 0)) {
			return current;
		}
		buffer.erase(len);
		if (buffer[0] != '/') {
			buffer = parentPath(current) + '/' + buffer;
		}
		normalizeRealPath(buffer);
		if (names.find(buffer) != names.end()) {
			return buffer;
		}
		current = buffer;
	}
	return "";
}

void ZLUnixFSManager::normalizeRealPath(std::string &path) const {
	static std::string HomeDir = getHomeDir();
	static std::string PwdDir = getPwdDir();

	if (path.empty()) {
		path = PwdDir;
	} else if (path[0] == '~') {
		if ((path.length() == 1) || (path[1] == '/')) {
			path = HomeDir + path.substr(1);
		}
	} else if (path[0] != '/') {
		path = PwdDir + '/' + path;
	}
	int last = path.length() - 1;
	while ((last > 0) && (path[last] == '/')) {
		--last;
	}
	if (last < (int)path.length() - 1) {
		path = path.substr(0, last + 1);
	}

	int index;
	while ((index = path.find("/../")) != -1) {
		int prevIndex = std::max((int)path.rfind('/', index - 1), 0);
		path.erase(prevIndex, index + 3 - prevIndex);
	}
	int len = path.length();
	if ((len >= 3) && (path.substr(len - 3) == "/..")) {
		int prevIndex = std::max((int)path.rfind('/', len - 4), 0);
		path.erase(prevIndex);
	}
	while ((index = path.find("/./")) != -1) {
		path.erase(index, 2);
	}
	while (path.length() >= 2 &&
				 path.substr(path.length() - 2) == "/.") {
		path.erase(path.length() - 2);
	}
	while ((index = path.find("//")) != -1) {
		path.erase(index, 1);
	}
}

ZLFSDir *ZLUnixFSManager::createNewDirectory(const std::string &path) const {
	std::vector<std::string> subpaths;
	std::string current = path;

	while (current.length() > 1) {
		struct stat fileStat;
		if (stat(current.c_str(), &fileStat) == 0) {
			if (!S_ISDIR(fileStat.st_mode)) {
				return 0;
			}
			break;
		} else {
			subpaths.push_back(current);
			int index = current.rfind('/');
			if (index == -1) {
				return 0;
			}
			current.erase(index);
		}
	}

	for (int i = subpaths.size() - 1; i >= 0; --i) {
		if (mkdir(subpaths[i].c_str(), 0x1FF) != 0) {
			return 0;
		}
	}
	return createPlainDirectory(path);
}

ZLFSDir *ZLUnixFSManager::createPlainDirectory(const std::string &path) const {
	return new ZLUnixFSDir(path);
}

ZLInputStream *ZLUnixFSManager::createPlainInputStream(const std::string &path) const {
	return new ZLUnixFileInputStream(path);
}

ZLOutputStream *ZLUnixFSManager::createOutputStream(const std::string &path) const {
	return new ZLUnixFileOutputStream(path);
}

bool ZLUnixFSManager::removeFile(const std::string &path) const {
	return unlink(path.c_str()) == 0;
}

int ZLUnixFSManager::findArchiveFileNameDelimiter(const std::string &path) const {
	return path.rfind(':');
}

static const std::string RootPath = "/";

shared_ptr<ZLDir> ZLUnixFSManager::rootDirectory() const {
	return createPlainDirectory(RootPath);
}

const std::string &ZLUnixFSManager::rootDirectoryPath() const {
	return RootPath;
}

std::string ZLUnixFSManager::parentPath(const std::string &path) const {
	if (path == RootPath) {
		return path;
	}
	int index = findLastFileNameDelimiter(path);
	return (index <= 0) ? RootPath : path.substr(0, index);
}

bool ZLUnixFSManager::canRemoveFile(const std::string &path) const {
	return access(parentPath(path).c_str(), W_OK) == 0;
}

