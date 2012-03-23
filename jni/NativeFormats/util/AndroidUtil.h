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
#include <vector>

class ZLFile;

class AndroidUtil {

private:
	static JavaVM *ourJavaVM;

public:
	static const char * const Class_java_lang_String;
	static const char * const Class_java_util_Collection;
	static const char * const Class_java_util_Locale;
	static const char * const Class_java_io_InputStream;
	static const char * const Class_java_io_PrintStream;
	static const char * const Class_ZLibrary;
	static const char * const Class_ZLFile;
	static const char * const Class_NativeFormatPlugin;
	static const char * const Class_PluginCollection;
	static const char * const Class_Encoding;
	static const char * const Class_EncodingConverter;
	static const char * const Class_JavaEncodingCollection;
	static const char * const Class_Paths;
	static const char * const Class_Book;
	static const char * const Class_Tag;
	static const char * const Class_NativeBookModel;
	static const char * const Class_BookReadingException;

	static jmethodID MID_java_lang_String_toLowerCase;
	static jmethodID MID_java_lang_String_toUpperCase;

	static jmethodID MID_java_util_Collection_toArray;

	static jmethodID SMID_java_util_Locale_getDefault;
	static jmethodID MID_java_util_Locale_getLanguage;

	static jmethodID MID_java_io_InputStream_close;
	static jmethodID MID_java_io_InputStream_read;
	static jmethodID MID_java_io_InputStream_skip;

	static jmethodID MID_java_io_PrintStream_println;

	static jmethodID SMID_ZLibrary_Instance;
	static jmethodID MID_ZLibrary_getVersionName;

	static jmethodID SMID_ZLFile_createFileByPath;
	static jmethodID MID_ZLFile_children;
	static jmethodID MID_ZLFile_exists;
	static jmethodID MID_ZLFile_getInputStream;
	static jmethodID MID_ZLFile_getPath;
	static jmethodID MID_ZLFile_isDirectory;
	static jmethodID MID_ZLFile_size;

	static jmethodID MID_NativeFormatPlugin_init;
	static jmethodID MID_NativeFormatPlugin_supportedFileType;

	static jmethodID SMID_PluginCollection_Instance;

	static jmethodID MID_Encoding_createConverter;

	static jfieldID FID_EncodingConverter_Name;
	static jmethodID MID_EncodingConverter_convert;
	static jmethodID MID_EncodingConverter_reset;

	static jmethodID SMID_JavaEncodingCollection_Instance;
	static jmethodID MID_JavaEncodingCollection_getEncoding_String;
	static jmethodID MID_JavaEncodingCollection_getEncoding_int;
	static jmethodID MID_JavaEncodingCollection_providesConverterFor;

	static jmethodID SMID_Paths_cacheDirectory;

	static jfieldID FID_Book_File;
	static jmethodID MID_Book_getTitle;
	static jmethodID MID_Book_getLanguage;
	static jmethodID MID_Book_getEncodingNoDetection;
	static jmethodID MID_Book_setTitle;
	static jmethodID MID_Book_setSeriesInfo;
	static jmethodID MID_Book_setLanguage;
	static jmethodID MID_Book_setEncoding;
	static jmethodID MID_Book_addAuthor;
	static jmethodID MID_Book_addTag;
	static jmethodID MID_Book_save;

	static jmethodID SMID_Tag_getTag;

	static jfieldID FID_NativeBookModel_Book;
	static jmethodID MID_NativeBookModel_initImageMap;
	static jmethodID MID_NativeBookModel_initInternalHyperlinks;
	static jmethodID MID_NativeBookModel_initTOC;
	static jmethodID MID_NativeBookModel_createTextModel;
	static jmethodID MID_NativeBookModel_setBookTextModel;
	static jmethodID MID_NativeBookModel_setFootnoteModel;

	static jmethodID SMID_BookReadingException_throwForFile;

public:
	static bool init(JavaVM* jvm);
	static JNIEnv *getEnv();

	static jobject createZLFile(JNIEnv *env, const std::string &path);
	static std::string fromJavaString(JNIEnv *env, jstring from);
	static jstring createJavaString(JNIEnv* env, const std::string &str);
	static std::string convertNonUtfString(const std::string &str);

	static jintArray createIntArray(JNIEnv *env, const std::vector<jint> &data);
	static jbyteArray createByteArray(JNIEnv *env, const std::vector<jbyte> &data);
	static jobjectArray createStringArray(JNIEnv *env, const std::vector<std::string> &data);

	static void throwRuntimeException(JNIEnv *env, const std::string &message);
	static void throwBookReadingException(const std::string &resourceId, const ZLFile &file);
};

#endif /* __ANDROIDUTIL_H__ */
