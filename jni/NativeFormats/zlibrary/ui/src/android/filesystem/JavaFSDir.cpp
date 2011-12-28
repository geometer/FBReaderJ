/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

#include <set>

#include <AndroidUtil.h>

#include "JavaFSDir.h"


JavaFSDir::JavaFSDir(const std::string &name) : ZLFSDir(name) {
	myJavaFile = 0;
}

JavaFSDir::~JavaFSDir() {
	JNIEnv *env = AndroidUtil::getEnv();
	env->DeleteGlobalRef(myJavaFile);
}

void JavaFSDir::initJavaFile(JNIEnv *env) {
	if (myJavaFile == 0) {
		jobject javaFile = AndroidUtil::createZLFile(env, path());
		myJavaFile = env->NewGlobalRef(javaFile);
		env->DeleteLocalRef(javaFile);
	}
}

jobjectArray JavaFSDir::getFileChildren(JNIEnv *env) {
	initJavaFile(env);
	if (myJavaFile == 0) {
		return 0;
	}

	jobject list = env->CallObjectMethod(myJavaFile, AndroidUtil::MID_ZLFile_children);
	if (list == 0) {
		return 0;
	}
	jobjectArray array = (jobjectArray)env->CallObjectMethod(list, AndroidUtil::MID_java_util_Collection_toArray);
	env->DeleteLocalRef(list);
	return array;
}

void JavaFSDir::collectChildren(std::vector<std::string> &names, bool filesNotDirs) {
	JNIEnv *env = AndroidUtil::getEnv();
	jobjectArray array = getFileChildren(env);
	if (array == 0) {
		return;
	}

	std::set<std::string> filesSet;

	std::string prefix(path());
	prefix.append("/");
	size_t prefixLength = prefix.length();

	const jsize size = env->GetArrayLength(array);
	for (jsize i = 0; i < size; ++i) {
		jobject file = env->GetObjectArrayElement(array, i);

		jstring javaPath = (jstring)env->CallObjectMethod(file, AndroidUtil::MID_ZLFile_getPath);
		const char *chars = env->GetStringUTFChars(javaPath, 0);
		std::string path(chars);
		env->ReleaseStringUTFChars(javaPath, chars);
		env->DeleteLocalRef(javaPath);

		if (path.length() > prefixLength) {
			size_t index = path.find('/', prefixLength);
			bool isdir = false;
			if (index != std::string::npos) {
				path.erase(index);
				isdir = true;
			} /*else {
				isdir = env->CallBooleanMethod(file, AndroidUtil::MID_ZLFile_isDirectory) != 0;
			}*/
			if (isdir ^ filesNotDirs) {
				names.push_back(path.substr(prefixLength));
			}
		}

		env->DeleteLocalRef(file);
	}
}

void JavaFSDir::collectSubDirs(std::vector<std::string> &names, bool includeSymlinks) {
	collectChildren(names, false);
}

void JavaFSDir::collectFiles(std::vector<std::string> &names, bool includeSymlinks) {
	collectChildren(names, true);
}
