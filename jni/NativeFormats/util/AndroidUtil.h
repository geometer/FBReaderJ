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

#ifndef __ANDROIDUTIL_H__
#define __ANDROIDUTIL_H__

#include <jni.h>

#include <string>

class AndroidUtil {

private:
	static JavaVM *ourJavaVM;

public:
	static const char * const Class_java_util_Collection;
	static const char * const Class_java_util_Locale;
	static const char * const Class_java_io_InputStream;
	static const char * const Class_ZLibrary;
	static const char * const Class_ZLFile;
	static const char * const Class_NativeFormatPlugin;
	static const char * const Class_PluginCollection;
	static const char * const Class_Book;
	static const char * const Class_Tag;

	static jmethodID MID_java_util_Collection_toArray;

	static jmethodID SMID_java_util_Locale_getDefault;
	static jmethodID MID_java_util_Locale_getLanguage;

	static jmethodID MID_java_io_InputStream_close;
	static jmethodID MID_java_io_InputStream_read;
	static jmethodID MID_java_io_InputStream_skip;

	static jmethodID SMID_ZLibrary_Instance;
	static jmethodID MID_ZLibrary_getVersionName;

	static jmethodID SMID_ZLFile_createFileByPath;
	static jmethodID MID_ZLFile_children;
	static jmethodID MID_ZLFile_exists;
	static jmethodID MID_ZLFile_getInputStream;
	static jmethodID MID_ZLFile_getPath;
	static jmethodID MID_ZLFile_isDirectory;
	static jmethodID MID_ZLFile_size;

	static jmethodID SMID_PluginCollection_Instance;

	static jfieldID FID_Book_File;
	static jmethodID MID_Book_getTitle;
	static jmethodID MID_Book_getLanguage;
	static jmethodID MID_Book_getEncoding;

	static jmethodID SMID_Tag_getTag;

public:
	static bool init(JavaVM* jvm);
	static JNIEnv *getEnv();

	static jobject createZLFile(JNIEnv *env, const std::string &path);
	static bool extractJavaString(JNIEnv *env, jstring from, std::string &to);
	static jstring createJavaString(JNIEnv* env, const std::string &str);
	static std::string convertNonUtfString(const std::string &str);

	static void throwRuntimeException(JNIEnv *env, const std::string &message);
};

#endif /* __ANDROIDUTIL_H__ */
