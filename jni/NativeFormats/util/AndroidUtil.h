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

#include <shared_ptr.h>

class VoidMethod;
class IntMethod;
class LongMethod;
class BooleanMethod;
class StringMethod;
class ObjectMethod;

class ZLFile;
class ZLFileImage;

class AndroidUtil {

private:
	static JavaVM *ourJavaVM;

public:
	static const char * const Class_java_lang_String;
	static const char * const Class_java_util_Collection;
	static const char * const Class_java_util_Locale;
	static const char * const Class_java_io_InputStream;
	static const char * const Class_ZLibrary;
	static const char * const Class_ZLFile;
	static const char * const Class_ZLFileImage;
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

	static shared_ptr<StringMethod> Method_java_lang_String_toLowerCase;
	static shared_ptr<StringMethod> Method_java_lang_String_toUpperCase;

	static jmethodID MID_java_util_Collection_toArray;

	static jmethodID SMID_java_util_Locale_getDefault;
	static shared_ptr<StringMethod> Method_java_util_Locale_getLanguage;

	static shared_ptr<VoidMethod> Method_java_io_InputStream_close;
	static shared_ptr<IntMethod> Method_java_io_InputStream_read;
	static shared_ptr<LongMethod> Method_java_io_InputStream_skip;

	static jmethodID SMID_ZLibrary_Instance;
	static shared_ptr<StringMethod> Method_ZLibrary_getVersionName;

	static jmethodID SMID_ZLFile_createFileByPath;
	static shared_ptr<ObjectMethod> Method_ZLFile_children;
	static shared_ptr<BooleanMethod> Method_ZLFile_exists;
	static shared_ptr<ObjectMethod> Method_ZLFile_getInputStream;
	static shared_ptr<StringMethod> Method_ZLFile_getPath;
	static shared_ptr<BooleanMethod> Method_ZLFile_isDirectory;
	static shared_ptr<LongMethod> Method_ZLFile_size;

	static jmethodID MID_ZLFileImage_init;

	static jmethodID MID_NativeFormatPlugin_init;
	static shared_ptr<StringMethod> Method_NativeFormatPlugin_supportedFileType;

	static jmethodID SMID_PluginCollection_Instance;

	static shared_ptr<ObjectMethod> Method_Encoding_createConverter;

	static jfieldID FID_EncodingConverter_Name;
	static shared_ptr<IntMethod> Method_EncodingConverter_convert;
	static shared_ptr<VoidMethod> Method_EncodingConverter_reset;

	static jmethodID SMID_JavaEncodingCollection_Instance;
	static shared_ptr<ObjectMethod> Method_JavaEncodingCollection_getEncoding_String;
	static shared_ptr<ObjectMethod> Method_JavaEncodingCollection_getEncoding_int;
	static shared_ptr<BooleanMethod> Method_JavaEncodingCollection_providesConverterFor;

	static jmethodID SMID_Paths_cacheDirectory;

	static jfieldID FID_Book_File;
	static shared_ptr<StringMethod> Method_Book_getTitle;
	static shared_ptr<StringMethod> Method_Book_getLanguage;
	static shared_ptr<StringMethod> Method_Book_getEncodingNoDetection;
	static shared_ptr<VoidMethod> Method_Book_setTitle;
	static shared_ptr<VoidMethod> Method_Book_setSeriesInfo;
	static shared_ptr<VoidMethod> Method_Book_setLanguage;
	static shared_ptr<VoidMethod> Method_Book_setEncoding;
	static shared_ptr<VoidMethod> Method_Book_addAuthor;
	static shared_ptr<VoidMethod> Method_Book_addTag;
	static shared_ptr<BooleanMethod> Method_Book_save;

	static jmethodID SMID_Tag_getTag;

	static jfieldID FID_NativeBookModel_Book;
	static shared_ptr<VoidMethod> Method_NativeBookModel_initInternalHyperlinks;
	static shared_ptr<VoidMethod> Method_NativeBookModel_initTOC;
	static shared_ptr<ObjectMethod> Method_NativeBookModel_createTextModel;
	static shared_ptr<VoidMethod> Method_NativeBookModel_setBookTextModel;
	static shared_ptr<VoidMethod> Method_NativeBookModel_setFootnoteModel;
	static shared_ptr<VoidMethod> Method_NativeBookModel_addImage;

	static jmethodID SMID_BookReadingException_throwForFile;

public:
	static bool init(JavaVM* jvm);
	static JNIEnv *getEnv();

	static std::string fromJavaString(JNIEnv *env, jstring from);
	static jstring createJavaString(JNIEnv* env, const std::string &str);
	static std::string convertNonUtfString(const std::string &str);

	static jobject createJavaFile(JNIEnv *env, const std::string &path);
	static jobject createJavaImage(JNIEnv *env, const ZLFileImage &image);

	static jintArray createJavaIntArray(JNIEnv *env, const std::vector<jint> &data);
	static jbyteArray createJavaByteArray(JNIEnv *env, const std::vector<jbyte> &data);
	static jobjectArray createJavaStringArray(JNIEnv *env, const std::vector<std::string> &data);

	static void throwRuntimeException(JNIEnv *env, const std::string &message);
	static void throwBookReadingException(const std::string &resourceId, const ZLFile &file);
};

#endif /* __ANDROIDUTIL_H__ */
