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

#include "AndroidUtil.h"

JavaVM *AndroidUtil::ourJavaVM = 0;

const char * const AndroidUtil::Class_NativeFormatPlugin = "org/geometerplus/fbreader/formats/NativeFormatPlugin";
const char * const AndroidUtil::Class_PluginCollection = "org/geometerplus/fbreader/formats/PluginCollection";
const char * const AndroidUtil::Class_ZLFile = "org/geometerplus/zlibrary/core/filesystem/ZLFile";
const char * const AndroidUtil::Class_Book = "org/geometerplus/fbreader/library/Book";
const char * const AndroidUtil::Class_Tag = "org/geometerplus/fbreader/library/Tag";

jmethodID AndroidUtil::SMID_PluginCollection_Instance;

jmethodID AndroidUtil::MID_ZLFile_getPath;

jfieldID AndroidUtil::FID_Book_File;
jfieldID AndroidUtil::FID_Book_Title;
jfieldID AndroidUtil::FID_Book_Language;
jfieldID AndroidUtil::FID_Book_Encoding;

jmethodID AndroidUtil::SMID_Tag_getTag;

JNIEnv *AndroidUtil::getEnv() {
	JNIEnv *env;
	ourJavaVM->GetEnv((void **)&env, JNI_VERSION_1_2);
	return env;
}

#define CHECK_NULL(value) if ((value) == 0) { return false; }

bool AndroidUtil::init(JavaVM* jvm) {
	ourJavaVM = jvm;

	JNIEnv *env = getEnv();
	jclass cls;

	CHECK_NULL( cls = env->FindClass(Class_ZLFile) );
	CHECK_NULL( MID_ZLFile_getPath = env->GetMethodID(cls, "getPath", "()Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Book) );
	CHECK_NULL( FID_Book_File = env->GetFieldID(cls, "File", "Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;") );
	CHECK_NULL( FID_Book_Title = env->GetFieldID(cls, "myTitle", "Ljava/lang/String;") );
	CHECK_NULL( FID_Book_Language = env->GetFieldID(cls, "myLanguage", "Ljava/lang/String;") );
	CHECK_NULL( FID_Book_Encoding = env->GetFieldID(cls, "myEncoding", "Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Tag) );
	CHECK_NULL( SMID_Tag_getTag = env->GetStaticMethodID(cls, "getTag", "(Lorg/geometerplus/fbreader/library/Tag;Ljava/lang/String;)Lorg/geometerplus/fbreader/library/Tag;") );
	env->DeleteLocalRef(cls);
}

bool AndroidUtil::extractJavaString(JNIEnv *env, jstring from, std::string &to) {
	if (from == 0) {
		return false;
	}
	const char *data = env->GetStringUTFChars(from, 0);
	to.assign(data);
	env->ReleaseStringUTFChars(from, data);
	return true;
}

jstring AndroidUtil::createJavaString(JNIEnv* env, const std::string &str) {
	if (str.empty()) {
		return 0;
	}
	return env->NewStringUTF(str.c_str());
}
