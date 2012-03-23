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

#include <ZLFile.h>

#include "AndroidUtil.h"

JavaVM *AndroidUtil::ourJavaVM = 0;

const char * const AndroidUtil::Class_java_lang_String = "java/lang/String";
const char * const AndroidUtil::Class_java_util_Collection = "java/util/Collection";
const char * const AndroidUtil::Class_java_util_Locale = "java/util/Locale";
const char * const AndroidUtil::Class_java_io_InputStream = "java/io/InputStream";
const char * const AndroidUtil::Class_java_io_PrintStream = "java/io/PrintStream";
const char * const AndroidUtil::Class_ZLibrary = "org/geometerplus/zlibrary/core/library/ZLibrary";
const char * const AndroidUtil::Class_NativeFormatPlugin = "org/geometerplus/fbreader/formats/NativeFormatPlugin";
const char * const AndroidUtil::Class_PluginCollection = "org/geometerplus/fbreader/formats/PluginCollection";
const char * const AndroidUtil::Class_Encoding = "org/geometerplus/zlibrary/core/encodings/Encoding";
const char * const AndroidUtil::Class_EncodingConverter = "org/geometerplus/zlibrary/core/encodings/EncodingConverter";
const char * const AndroidUtil::Class_JavaEncodingCollection = "org/geometerplus/zlibrary/core/encodings/JavaEncodingCollection";
const char * const AndroidUtil::Class_Paths = "org/geometerplus/fbreader/Paths";
const char * const AndroidUtil::Class_ZLFile = "org/geometerplus/zlibrary/core/filesystem/ZLFile";
const char * const AndroidUtil::Class_Book = "org/geometerplus/fbreader/library/Book";
const char * const AndroidUtil::Class_Tag = "org/geometerplus/fbreader/library/Tag";
const char * const AndroidUtil::Class_NativeBookModel = "org/geometerplus/fbreader/bookmodel/NativeBookModel";
const char * const AndroidUtil::Class_BookReadingException = "org/geometerplus/fbreader/bookmodel/BookReadingException";

jmethodID AndroidUtil::MID_java_lang_String_toLowerCase;
jmethodID AndroidUtil::MID_java_lang_String_toUpperCase;

jmethodID AndroidUtil::MID_java_util_Collection_toArray;

jmethodID AndroidUtil::SMID_java_util_Locale_getDefault;
jmethodID AndroidUtil::MID_java_util_Locale_getLanguage;

jmethodID AndroidUtil::MID_java_io_InputStream_close;
jmethodID AndroidUtil::MID_java_io_InputStream_read;
jmethodID AndroidUtil::MID_java_io_InputStream_skip;

jmethodID AndroidUtil::MID_java_io_PrintStream_println;

jmethodID AndroidUtil::SMID_ZLibrary_Instance;
jmethodID AndroidUtil::MID_ZLibrary_getVersionName;

jmethodID AndroidUtil::MID_NativeFormatPlugin_init;
jmethodID AndroidUtil::MID_NativeFormatPlugin_supportedFileType;

jmethodID AndroidUtil::SMID_PluginCollection_Instance;

jmethodID AndroidUtil::MID_Encoding_createConverter;

jfieldID AndroidUtil::FID_EncodingConverter_Name;
jmethodID AndroidUtil::MID_EncodingConverter_convert;
jmethodID AndroidUtil::MID_EncodingConverter_reset;

jmethodID AndroidUtil::SMID_JavaEncodingCollection_Instance;
jmethodID AndroidUtil::MID_JavaEncodingCollection_getEncoding_int;
jmethodID AndroidUtil::MID_JavaEncodingCollection_getEncoding_String;
jmethodID AndroidUtil::MID_JavaEncodingCollection_providesConverterFor;

jmethodID AndroidUtil::SMID_ZLFile_createFileByPath;
jmethodID AndroidUtil::MID_ZLFile_children;
jmethodID AndroidUtil::MID_ZLFile_exists;
jmethodID AndroidUtil::MID_ZLFile_getInputStream;
jmethodID AndroidUtil::MID_ZLFile_getPath;
jmethodID AndroidUtil::MID_ZLFile_isDirectory;
jmethodID AndroidUtil::MID_ZLFile_size;

jmethodID AndroidUtil::SMID_Paths_cacheDirectory;

jfieldID AndroidUtil::FID_Book_File;
jmethodID AndroidUtil::MID_Book_getTitle;
jmethodID AndroidUtil::MID_Book_getLanguage;
jmethodID AndroidUtil::MID_Book_getEncodingNoDetection;
jmethodID AndroidUtil::MID_Book_setTitle;
jmethodID AndroidUtil::MID_Book_setSeriesInfo;
jmethodID AndroidUtil::MID_Book_setLanguage;
jmethodID AndroidUtil::MID_Book_setEncoding;
jmethodID AndroidUtil::MID_Book_addAuthor;
jmethodID AndroidUtil::MID_Book_addTag;
jmethodID AndroidUtil::MID_Book_save;

jmethodID AndroidUtil::SMID_Tag_getTag;

jfieldID AndroidUtil::FID_NativeBookModel_Book;
jmethodID AndroidUtil::MID_NativeBookModel_initImageMap;
jmethodID AndroidUtil::MID_NativeBookModel_initInternalHyperlinks;
jmethodID AndroidUtil::MID_NativeBookModel_initTOC;
jmethodID AndroidUtil::MID_NativeBookModel_createTextModel;
jmethodID AndroidUtil::MID_NativeBookModel_setBookTextModel;
jmethodID AndroidUtil::MID_NativeBookModel_setFootnoteModel;

jmethodID AndroidUtil::SMID_BookReadingException_throwForFile;

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

	CHECK_NULL( cls = env->FindClass(Class_java_lang_String) );
	CHECK_NULL( MID_java_lang_String_toLowerCase = env->GetMethodID(cls, "toLowerCase", "()Ljava/lang/String;") );
	CHECK_NULL( MID_java_lang_String_toUpperCase = env->GetMethodID(cls, "toUpperCase", "()Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_java_util_Collection) );
	CHECK_NULL( MID_java_util_Collection_toArray = env->GetMethodID(cls, "toArray", "()[Ljava/lang/Object;") );
	//CHECK_NULL( MID_java_util_Collection_add = env->GetMethodID(cls, "add", "(Ljava/lang/Object;)Z") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_java_util_Locale) );
	CHECK_NULL( SMID_java_util_Locale_getDefault = env->GetStaticMethodID(cls, "getDefault", "()Ljava/util/Locale;") );
	CHECK_NULL( MID_java_util_Locale_getLanguage = env->GetMethodID(cls, "getLanguage", "()Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_java_io_InputStream) );
	CHECK_NULL( MID_java_io_InputStream_close = env->GetMethodID(cls, "close", "()V") );
	CHECK_NULL( MID_java_io_InputStream_read = env->GetMethodID(cls, "read", "([BII)I") );
	CHECK_NULL( MID_java_io_InputStream_skip = env->GetMethodID(cls, "skip", "(J)J") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_java_io_PrintStream) );
	CHECK_NULL( MID_java_io_PrintStream_println = env->GetMethodID(cls, "println", "(Ljava/lang/String;)V") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_ZLibrary) );
	CHECK_NULL( SMID_ZLibrary_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/zlibrary/core/library/ZLibrary;") );
	CHECK_NULL( MID_ZLibrary_getVersionName = env->GetMethodID(cls, "getVersionName", "()Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_NativeFormatPlugin) );
	CHECK_NULL( MID_NativeFormatPlugin_init = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;)V") );
	CHECK_NULL( MID_NativeFormatPlugin_supportedFileType = env->GetMethodID(cls, "supportedFileType", "()Ljava/lang/String;") );
	//CHECK_NULL( SMID_NativeFormatPlugin_createImage = env->GetStaticMethodID(cls, "createImage", "(Ljava/lang/String;Ljava/lang/String;II)Lorg/geometerplus/zlibrary/core/image/ZLImage;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_PluginCollection) );
	CHECK_NULL( SMID_PluginCollection_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/fbreader/formats/PluginCollection;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Encoding) );
	CHECK_NULL( MID_Encoding_createConverter = env->GetMethodID(cls, "createConverter", "()Lorg/geometerplus/zlibrary/core/encodings/EncodingConverter;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_EncodingConverter) );
	CHECK_NULL( FID_EncodingConverter_Name = env->GetFieldID(cls, "Name", "Ljava/lang/String;") );
	CHECK_NULL( MID_EncodingConverter_convert = env->GetMethodID(cls, "convert", "([BII[BI)I") );
	CHECK_NULL( MID_EncodingConverter_reset = env->GetMethodID(cls, "reset", "()V") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_JavaEncodingCollection) );
	CHECK_NULL( SMID_JavaEncodingCollection_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/zlibrary/core/encodings/JavaEncodingCollection;") );
	CHECK_NULL( MID_JavaEncodingCollection_getEncoding_String = env->GetMethodID(cls, "getEncoding", "(Ljava/lang/String;)Lorg/geometerplus/zlibrary/core/encodings/Encoding;") );
	CHECK_NULL( MID_JavaEncodingCollection_getEncoding_int = env->GetMethodID(cls, "getEncoding", "(I)Lorg/geometerplus/zlibrary/core/encodings/Encoding;") );
	CHECK_NULL( MID_JavaEncodingCollection_providesConverterFor = env->GetMethodID(cls, "providesConverterFor", "(Ljava/lang/String;)Z") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_ZLFile) );
	CHECK_NULL( SMID_ZLFile_createFileByPath = env->GetStaticMethodID(cls, "createFileByPath", "(Ljava/lang/String;)Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;") );
	CHECK_NULL( MID_ZLFile_children = env->GetMethodID(cls, "children", "()Ljava/util/List;") );
	CHECK_NULL( MID_ZLFile_exists = env->GetMethodID(cls, "exists", "()Z") );
	CHECK_NULL( MID_ZLFile_isDirectory = env->GetMethodID(cls, "isDirectory", "()Z") );
	CHECK_NULL( MID_ZLFile_getInputStream = env->GetMethodID(cls, "getInputStream", "()Ljava/io/InputStream;") );
	CHECK_NULL( MID_ZLFile_getPath = env->GetMethodID(cls, "getPath", "()Ljava/lang/String;") );
	CHECK_NULL( MID_ZLFile_size = env->GetMethodID(cls, "size", "()J") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Paths) );
	CHECK_NULL( SMID_Paths_cacheDirectory = env->GetStaticMethodID(cls, "cacheDirectory", "()Ljava/lang/String;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Book) );
	CHECK_NULL( FID_Book_File = env->GetFieldID(cls, "File", "Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;") );
	CHECK_NULL( MID_Book_getTitle = env->GetMethodID(cls, "getTitle", "()Ljava/lang/String;") );
	CHECK_NULL( MID_Book_getLanguage = env->GetMethodID(cls, "getLanguage", "()Ljava/lang/String;") );
	CHECK_NULL( MID_Book_getEncodingNoDetection = env->GetMethodID(cls, "getEncodingNoDetection", "()Ljava/lang/String;") );
	CHECK_NULL( MID_Book_setTitle = env->GetMethodID(cls, "setTitle", "(Ljava/lang/String;)V") );
	CHECK_NULL( MID_Book_setSeriesInfo = env->GetMethodID(cls, "setSeriesInfo", "(Ljava/lang/String;F)V") );
	CHECK_NULL( MID_Book_setLanguage = env->GetMethodID(cls, "setLanguage", "(Ljava/lang/String;)V") );
	CHECK_NULL( MID_Book_setEncoding = env->GetMethodID(cls, "setEncoding", "(Ljava/lang/String;)V") );
	CHECK_NULL( MID_Book_addAuthor = env->GetMethodID(cls, "addAuthor", "(Ljava/lang/String;Ljava/lang/String;)V") );
	CHECK_NULL( MID_Book_addTag = env->GetMethodID(cls, "addTag", "(Lorg/geometerplus/fbreader/library/Tag;)V") );
	CHECK_NULL( MID_Book_save = env->GetMethodID(cls, "save", "()Z") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_Tag) );
	CHECK_NULL( SMID_Tag_getTag = env->GetStaticMethodID(cls, "getTag", "(Lorg/geometerplus/fbreader/library/Tag;Ljava/lang/String;)Lorg/geometerplus/fbreader/library/Tag;") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_NativeBookModel) );
	CHECK_NULL( FID_NativeBookModel_Book = env->GetFieldID(cls, "Book", "Lorg/geometerplus/fbreader/library/Book;") );
	CHECK_NULL( MID_NativeBookModel_initImageMap = env->GetMethodID(cls, "initImageMap", "([Ljava/lang/String;[I[ILjava/lang/String;Ljava/lang/String;I)V") );
	CHECK_NULL( MID_NativeBookModel_initInternalHyperlinks = env->GetMethodID(cls, "initInternalHyperlinks", "(Ljava/lang/String;Ljava/lang/String;I)V") );
	CHECK_NULL( MID_NativeBookModel_initTOC = env->GetMethodID(cls, "initTOC", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;[I[I)V") );
	CHECK_NULL( MID_NativeBookModel_createTextModel = env->GetMethodID(cls, "createTextModel", "(Ljava/lang/String;Ljava/lang/String;I[I[I[I[I[BLjava/lang/String;Ljava/lang/String;I)Lorg/geometerplus/zlibrary/text/model/ZLTextModel;") );
	CHECK_NULL( MID_NativeBookModel_setBookTextModel = env->GetMethodID(cls, "setBookTextModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)V") );
	CHECK_NULL( MID_NativeBookModel_setFootnoteModel = env->GetMethodID(cls, "setFootnoteModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)V") );
	env->DeleteLocalRef(cls);

	CHECK_NULL( cls = env->FindClass(Class_BookReadingException) );
	CHECK_NULL( SMID_BookReadingException_throwForFile = env->GetStaticMethodID(cls, "throwForFile", "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;)V") );
	env->DeleteLocalRef(cls);

	return true;
}

jobject AndroidUtil::createZLFile(JNIEnv *env, const std::string &path) {
	jstring javaPath = env->NewStringUTF(path.c_str());
	jclass cls = env->FindClass(Class_ZLFile);
	jobject javaFile = env->CallStaticObjectMethod(cls, SMID_ZLFile_createFileByPath, javaPath);
	env->DeleteLocalRef(cls);
	env->DeleteLocalRef(javaPath);
	return javaFile;
}

std::string AndroidUtil::fromJavaString(JNIEnv *env, jstring from) {
	if (from == 0) {
		return std::string();
	}
	const char *data = env->GetStringUTFChars(from, 0);
	const std::string result(data);
	env->ReleaseStringUTFChars(from, data);
	return result;
}

jstring AndroidUtil::createJavaString(JNIEnv* env, const std::string &str) {
	if (str.empty()) {
		return 0;
	}
	return env->NewStringUTF(str.c_str());
}

std::string AndroidUtil::convertNonUtfString(const std::string &str) {
	const int len = str.length();
	if (len == 0) {
		return str;
	}

	JNIEnv *env = getEnv();

	jchar *chars = new jchar[len];
	for (int i = 0; i < len; ++i) {
		chars[i] = str[i];
	}
	jstring javaString = env->NewString(chars, len);
	const std::string result = fromJavaString(env, javaString);
	env->DeleteLocalRef(javaString);
	delete[] chars;

	return result;
}

jintArray AndroidUtil::createIntArray(JNIEnv *env, const std::vector<jint> &data) {
	size_t size = data.size();
	jintArray array = env->NewIntArray(size);
	env->SetIntArrayRegion(array, 0, size, &data.front());
	return array;
}

jbyteArray AndroidUtil::createByteArray(JNIEnv *env, const std::vector<jbyte> &data) {
	size_t size = data.size();
	jbyteArray array = env->NewByteArray(size);
	env->SetByteArrayRegion(array, 0, size, &data.front());
	return array;
}

jobjectArray AndroidUtil::createStringArray(JNIEnv *env, const std::vector<std::string> &data) {
	size_t size = data.size();
	jclass cls = env->FindClass("java/lang/String");
	jobjectArray array = env->NewObjectArray(size, cls, 0);
	for (size_t i = 0; i < size; ++i) {
		const std::string &str = data[i];
		if (str.length() > 0) {
			jstring javaStr = env->NewStringUTF(str.c_str());
			env->SetObjectArrayElement(array, i, javaStr);
			env->DeleteLocalRef(javaStr);
		}
	}
	return array;
}

void AndroidUtil::throwRuntimeException(JNIEnv *env, const std::string &message) {
	jclass cls = env->FindClass("java/lang/RuntimeException");
	env->ThrowNew(cls, message.c_str());
}

void AndroidUtil::throwBookReadingException(const std::string &resourceId, const ZLFile &file) {
	JNIEnv *env = getEnv();
	jclass cls = env->FindClass("org/geometerplus/fbreader/bookmodel/BookReadingException");
	env->CallStaticVoidMethod(
		cls,
		SMID_BookReadingException_throwForFile,
		AndroidUtil::createJavaString(env, resourceId),
		AndroidUtil::createZLFile(env, file.path())
	);
	// TODO: clear cls & ZLFile object references
}
