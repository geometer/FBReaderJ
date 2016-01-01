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

#ifndef __ANDROIDUTIL_H__
#define __ANDROIDUTIL_H__

#include <jni.h>

#include <string>
#include <vector>

#include <shared_ptr.h>

class JavaClass;
class JavaArray;
class Constructor;
class ObjectField;
class VoidMethod;
class IntMethod;
class LongMethod;
class BooleanMethod;
class StringMethod;
class ObjectMethod;
class ObjectArrayMethod;
class StaticObjectMethod;

class ZLFile;
class FileEncryptionInfo;
class ZLFileImage;

class JString {

public:
	JString(JNIEnv* env, const std::string &str, bool emptyIsNull = true);
	jstring j();
	~JString();

private:
	JNIEnv *myEnv;
	jstring myJ;

private:
	JString(const JString&);
	const JString& operator = (const JString&);
};

class AndroidUtil {

private:
	static JavaVM *ourJavaVM;

public:
	static JavaClass Class_java_lang_Object;
	static JavaArray Array_java_lang_Object;
	static JavaClass Class_java_lang_RuntimeException;
	static JavaClass Class_java_lang_String;
	static JavaClass Class_java_util_Collection;
	static JavaClass Class_java_util_List;
	static JavaClass Class_java_util_Locale;
	static JavaClass Class_java_io_InputStream;
	static JavaClass Class_ZLibrary;
	static JavaClass Class_ZLFile;
	static JavaClass Class_FileInfo;
	static JavaClass Class_FileEncryptionInfo;
	static JavaClass Class_ZLFileImage;
	static JavaClass Class_ZLTextModel;
	static JavaClass Class_NativeFormatPlugin;
	static JavaClass Class_PluginCollection;
	static JavaClass Class_Encoding;
	static JavaClass Class_EncodingConverter;
	static JavaClass Class_JavaEncodingCollection;
	static JavaClass Class_Paths;
	static JavaClass Class_AbstractBook;
	static JavaClass Class_Book;
	static JavaClass Class_Tag;
	static JavaClass Class_BookModel;
	//static JavaClass Class_BookReadingException;

	static shared_ptr<StringMethod> Method_java_lang_String_toLowerCase;
	static shared_ptr<StringMethod> Method_java_lang_String_toUpperCase;

	static shared_ptr<ObjectArrayMethod> Method_java_util_Collection_toArray;

	static shared_ptr<StaticObjectMethod> StaticMethod_java_util_Locale_getDefault;
	static shared_ptr<StringMethod> Method_java_util_Locale_getLanguage;

	static shared_ptr<VoidMethod> Method_java_io_InputStream_close;
	static shared_ptr<IntMethod> Method_java_io_InputStream_read;
	static shared_ptr<LongMethod> Method_java_io_InputStream_skip;
	static shared_ptr<VoidMethod> Method_java_io_InputStream_mark;
	static shared_ptr<BooleanMethod> Method_java_io_InputStream_markSupported;
	static shared_ptr<VoidMethod> Method_java_io_InputStream_reset;

	static shared_ptr<StaticObjectMethod> StaticMethod_ZLibrary_Instance;
	static shared_ptr<StringMethod> Method_ZLibrary_getVersionName;

	static shared_ptr<StaticObjectMethod> StaticMethod_ZLFile_createFileByPath;
	static shared_ptr<ObjectMethod> Method_ZLFile_children;
	static shared_ptr<BooleanMethod> Method_ZLFile_exists;
	static shared_ptr<ObjectMethod> Method_ZLFile_getInputStream;
	static shared_ptr<StringMethod> Method_ZLFile_getPath;
	static shared_ptr<BooleanMethod> Method_ZLFile_isDirectory;
	static shared_ptr<LongMethod> Method_ZLFile_size;
	static shared_ptr<LongMethod> Method_ZLFile_lastModified;

	static shared_ptr<Constructor> Constructor_FileInfo;
	static shared_ptr<Constructor> Constructor_FileEncryptionInfo;

	static shared_ptr<Constructor> Constructor_ZLFileImage;

	static shared_ptr<StaticObjectMethod> StaticMethod_NativeFormatPlugin_create;
	static shared_ptr<StringMethod> Method_NativeFormatPlugin_supportedFileType;

	//static shared_ptr<StaticObjectMethod> StaticMethod_PluginCollection_Instance;

	static shared_ptr<ObjectMethod> Method_Encoding_createConverter;

	static shared_ptr<ObjectField> Field_EncodingConverter_Name;
	static shared_ptr<IntMethod> Method_EncodingConverter_convert;
	static shared_ptr<VoidMethod> Method_EncodingConverter_reset;

	static shared_ptr<StaticObjectMethod> StaticMethod_JavaEncodingCollection_Instance;
	static shared_ptr<ObjectMethod> Method_JavaEncodingCollection_getEncoding;
	//static shared_ptr<ObjectMethod> Method_JavaEncodingCollection_getEncoding_int;
	static shared_ptr<BooleanMethod> Method_JavaEncodingCollection_providesConverterFor;

	static shared_ptr<StringMethod> Method_Book_getPath;
	static shared_ptr<StringMethod> Method_Book_getTitle;
	static shared_ptr<StringMethod> Method_Book_getLanguage;
	static shared_ptr<StringMethod> Method_Book_getEncodingNoDetection;
	static shared_ptr<VoidMethod> Method_Book_setTitle;
	static shared_ptr<VoidMethod> Method_Book_setSeriesInfo;
	static shared_ptr<VoidMethod> Method_Book_setLanguage;
	static shared_ptr<VoidMethod> Method_Book_setEncoding;
	static shared_ptr<VoidMethod> Method_Book_addAuthor;
	static shared_ptr<VoidMethod> Method_Book_addTag;
	static shared_ptr<VoidMethod> Method_Book_addUid;

	static shared_ptr<StaticObjectMethod> StaticMethod_Tag_getTag;

	static shared_ptr<ObjectField> Field_BookModel_Book;
	static shared_ptr<VoidMethod> Method_BookModel_initInternalHyperlinks;
	static shared_ptr<VoidMethod> Method_BookModel_addTOCItem;
	static shared_ptr<VoidMethod> Method_BookModel_leaveTOCItem;
	static shared_ptr<ObjectMethod> Method_BookModel_createTextModel;
	static shared_ptr<VoidMethod> Method_BookModel_setBookTextModel;
	static shared_ptr<VoidMethod> Method_BookModel_setFootnoteModel;
	static shared_ptr<VoidMethod> Method_BookModel_addImage;
	static shared_ptr<VoidMethod> Method_BookModel_registerFontFamilyList;
	static shared_ptr<VoidMethod> Method_BookModel_registerFontEntry;

public:
	static bool init(JavaVM* jvm);
	static JNIEnv *getEnv();

	static std::string fromJavaString(JNIEnv *env, jstring from);
	static jstring createJavaString(JNIEnv* env, const std::string &str);
	static std::string convertNonUtfString(const std::string &str);

	static jobject createJavaFile(JNIEnv *env, const std::string &path);
	static jobject createJavaEncryptionInfo(JNIEnv *env, shared_ptr<FileEncryptionInfo> info);
	static jobject createJavaImage(JNIEnv *env, const ZLFileImage &image);

	static jintArray createJavaIntArray(JNIEnv *env, const std::vector<jint> &data);
	static jbyteArray createJavaByteArray(JNIEnv *env, const std::vector<jbyte> &data);
};

inline jstring JString::j() { return myJ; }

#endif /* __ANDROIDUTIL_H__ */
