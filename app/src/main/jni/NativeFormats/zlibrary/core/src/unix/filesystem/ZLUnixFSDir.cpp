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
#include <dirent.h>
#include <stdio.h>

#include "ZLUnixFSDir.h"

/*
void ZLUnixFSDir::collectSubDirs(std::vector<std::string> &names, bool includeSymlinks) {
	DIR *dir = opendir(path().c_str());
	if (dir != 0) {
		const std::string namePrefix = path() + delimiter();
		const dirent *file;
		struct stat fileInfo;
		std::string shortName;
		while ((file = readdir(dir)) != 0) {
			shortName = file->d_name;
			if ((shortName == ".") || (shortName == "..")) {
				continue;
			}
			const std::string path = namePrefix + shortName;
			if (includeSymlinks) {
				stat(path.c_str(), &fileInfo);
			} else {
				lstat(path.c_str(), &fileInfo);
			}
			if (S_ISDIR(fileInfo.st_mode)) {
				names.push_back(shortName);
			}
		}
		closedir(dir);
	}
}
*/

void ZLUnixFSDir::collectFiles(std::vector<std::string> &names, bool includeSymlinks) {
	DIR *dir = opendir(path().c_str());
	if (dir != 0) {
		const std::string namePrefix = path() + delimiter();
		const dirent *file;
		struct stat fileInfo;
		std::string shortName;
		while ((file = readdir(dir)) != 0) {
			shortName = file->d_name;
			if ((shortName == ".") || (shortName == "..")) {
				continue;
			}
			const std::string path = namePrefix + shortName;
			if (includeSymlinks) {
				stat(path.c_str(), &fileInfo);
			} else {
				lstat(path.c_str(), &fileInfo);
			}
			if (S_ISREG(fileInfo.st_mode)) {
				names.push_back(shortName);
			}
		}
		closedir(dir);
	}
}
