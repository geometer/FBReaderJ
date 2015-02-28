/*
 * Copyright (C) 2011-2015 FBReader.ORG Limited <contact@fbreader.org>
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
#include <JniEnvelope.h>

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
		jobject javaFile = AndroidUtil::createJavaFile(env, path());
		myJavaFile = env->NewGlobalRef(javaFile);
		env->DeleteLocalRef(javaFile);
	}
}

jobjectArray JavaFSDir::getFileChildren(JNIEnv *env) {
	initJavaFile(env);
	if (myJavaFile == 0) {
		return 0;
	}

	jobject list = AndroidUtil::Method_ZLFile_children->call(myJavaFile);
	if (list == 0) {
		return 0;
	}
	jobjectArray array = AndroidUtil::Method_java_util_Collection_toArray->call(list);
	env->DeleteLocalRef(list);
	return array;
}

void JavaFSDir::collectFiles(std::vector<std::string> &names, bool includeSymlinks) {
	JNIEnv *env = AndroidUtil::getEnv();
	jobjectArray array = getFileChildren(env);
	if (array == 0) {
		return;
	}

	const jsize size = env->GetArrayLength(array);
	for (jsize i = 0; i < size; ++i) {
		jobject file = env->GetObjectArrayElement(array, i);
		std::string path = AndroidUtil::Method_ZLFile_getPath->callForCppString(file);
		env->DeleteLocalRef(file);

		std::size_t index = path.rfind('/');
		if (index != std::string::npos) {
			path = path.substr(index + 1);
		}
		names.push_back(path);
	}
}
