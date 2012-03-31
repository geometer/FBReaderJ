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
#include <JniEnvelope.h>

#include <ZLibrary.h>

#include "../../../../core/src/unix/library/ZLibraryImplementation.h"

#include "../filesystem/ZLAndroidFSManager.h"

class ZLAndroidLibraryImplementation : public ZLibraryImplementation {

private:
	void init(int &argc, char **&argv);
};

void initLibrary() {
	new ZLAndroidLibraryImplementation();
}

void ZLAndroidLibraryImplementation::init(int &argc, char **&argv) {
	ZLibrary::parseArguments(argc, argv);

	ZLAndroidFSManager::createInstance();
}

std::string ZLibrary::Language() {
	JNIEnv *env = AndroidUtil::getEnv();
	jobject locale = AndroidUtil::StaticMethod_java_util_Locale_getDefault->call();
	jstring javaLang = (jstring)AndroidUtil::Method_java_util_Locale_getLanguage->call(locale);
	const char *langData = env->GetStringUTFChars(javaLang, 0);
	std::string lang(langData);
	env->ReleaseStringUTFChars(javaLang, langData);
	env->DeleteLocalRef(javaLang);
	env->DeleteLocalRef(locale);
	return lang;
}

std::string ZLibrary::Version() {
	JNIEnv *env = AndroidUtil::getEnv();
	jobject zlibrary = AndroidUtil::StaticMethod_ZLibrary_Instance->call();
	jstring javaVersion = (jstring)AndroidUtil::Method_ZLibrary_getVersionName->call(zlibrary);
	const char *versionData = env->GetStringUTFChars(javaVersion, 0);
	std::string version(versionData);
	env->ReleaseStringUTFChars(javaVersion, versionData);
	env->DeleteLocalRef(javaVersion);
	env->DeleteLocalRef(zlibrary);
	return version;
}
