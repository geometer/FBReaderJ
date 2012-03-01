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
	static const char * const Class_ZLFile;
	static const char * const Class_NativeFormatPlugin;
	static const char * const Class_PluginCollection;
	static const char * const Class_Book;
	static const char * const Class_Tag;

	static jmethodID MID_ZLFile_getPath;

	static jmethodID SMID_PluginCollection_Instance;

	static jfieldID FID_Book_File;
	static jfieldID FID_Book_Title;
	static jfieldID FID_Book_Language;
	static jfieldID FID_Book_Encoding;

	static jmethodID SMID_Tag_getTag;

public:
	static bool init(JavaVM* jvm);
	static JNIEnv *getEnv();

	static bool extractJavaString(JNIEnv *env, jstring from, std::string &to);
	static jstring createJavaString(JNIEnv* env, const std::string &str);
};

#endif /* __ANDROIDUTIL_H__ */
