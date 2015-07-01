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
#include <AndroidUtil.h>
#include <JniEnvelope.h>

#include "ZLAndroidFSManager.h"

#include "JavaInputStream.h"
#include "JavaFSDir.h"


std::string ZLAndroidFSManager::convertFilenameToUtf8(const std::string &name) const {
	return name;
}

std::string ZLAndroidFSManager::mimeType(const std::string &path) const {
	return std::string();
}


void ZLAndroidFSManager::normalizeRealPath(std::string &path) const {
	if (path.empty()) {
		return;
	} else if (path[0] == '~') {
		if (path.length() == 1 || path[1] == '/') {
			path.erase(0, 1);
		}
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


ZLFileInfo ZLAndroidFSManager::fileInfo(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::fileInfo(path);
	}

	ZLFileInfo info;

	JNIEnv *env = AndroidUtil::getEnv();
	jobject javaFile = AndroidUtil::createJavaFile(env, path);
	if (javaFile == 0) {
		return info;
	}

	info.IsDirectory = AndroidUtil::Method_ZLFile_isDirectory->call(javaFile);
	const jboolean exists = AndroidUtil::Method_ZLFile_exists->call(javaFile);
	if (exists) {
		info.Exists = true;
		info.Size = AndroidUtil::Method_ZLFile_size->call(javaFile);
		info.MTime = AndroidUtil::Method_ZLFile_lastModified->call(javaFile);
	}
	env->DeleteLocalRef(javaFile);

	return info;
}

std::string ZLAndroidFSManager::resolveSymlink(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::resolveSymlink(path);
	}
	return path;
}

ZLFSDir *ZLAndroidFSManager::createNewDirectory(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::createNewDirectory(path);
	}
	return 0;
}

ZLFSDir *ZLAndroidFSManager::createPlainDirectory(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::createPlainDirectory(path);
	}
	return new JavaFSDir(path);
}

ZLInputStream *ZLAndroidFSManager::createPlainInputStream(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::createPlainInputStream(path);
	}
	return new JavaInputStream(path);
}

/*ZLOutputStream *ZLAndroidFSManager::createOutputStream(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::createOutputStream(path);
	}
	return 0;
}*/

bool ZLAndroidFSManager::removeFile(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::removeFile(path);
	}
	return false;
}

bool ZLAndroidFSManager::canRemoveFile(const std::string &path) const {
	if (useNativeImplementation(path)) {
		return ZLUnixFSManager::canRemoveFile(path);
	}
	return false;
}
