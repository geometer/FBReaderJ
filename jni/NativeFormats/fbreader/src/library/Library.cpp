/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#include <AndroidUtil.h>

#include "Library.h"


shared_ptr<Library> Library::ourInstance;

Library &Library::Instance() {
	if (ourInstance.isNull()) {
		ourInstance = new Library();
	}
	return *ourInstance;
}

Library::Library() {
	JNIEnv *env = AndroidUtil::getEnv();
	jclass paths = env->FindClass(AndroidUtil::Class_Paths);
	myPathsClass = (jclass)env->NewGlobalRef(paths);
	env->DeleteLocalRef(paths);
}

Library::~Library() {
	JNIEnv *env = AndroidUtil::getEnv();
	env->DeleteGlobalRef(myPathsClass);
}

std::string Library::cacheDirectory() const {
	JNIEnv *env = AndroidUtil::getEnv();
	jstring res = (jstring)env->CallStaticObjectMethod(myPathsClass, AndroidUtil::SMID_Paths_cacheDirectory);
	const char *data = env->GetStringUTFChars(res, 0);
	std::string str(data);
	env->ReleaseStringUTFChars(res, data);
	env->DeleteLocalRef(res);
	return str;
}
