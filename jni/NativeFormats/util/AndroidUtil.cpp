/*
 * Copyright (C) 2011 Geometer Plus <contact@geometerplus.com>
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


const char * const AndroidUtil::Class_java_io_InputStream = "java/io/InputStream";
const char * const AndroidUtil::Class_java_util_List = "java/util/List";
const char * const AndroidUtil::Class_java_util_Locale = "java/util/Locale";
const char * const AndroidUtil::Class_ZLibrary = "org/geometerplus/zlibrary/core/library/ZLibrary";
const char * const AndroidUtil::Class_ZLFile = "org/geometerplus/zlibrary/core/filesystem/ZLFile";
const char * const AndroidUtil::Class_NativeFormatPlugin = "org/geometerplus/fbreader/formats/NativeFormatPlugin";
const char * const AndroidUtil::Class_PluginCollection = "org/geometerplus/fbreader/formats/PluginCollection";
const char * const AndroidUtil::Class_Paths = "org/geometerplus/fbreader/Paths";
const char * const AndroidUtil::Class_Book = "org/geometerplus/fbreader/library/Book";
const char * const AndroidUtil::Class_Tag = "org/geometerplus/fbreader/library/Tag";
const char * const AndroidUtil::Class_BookModel = "org/geometerplus/fbreader/bookmodel/BookModel";
const char * const AndroidUtil::Class_NativeBookModel = "org/geometerplus/fbreader/bookmodel/NativeBookModel";

jmethodID AndroidUtil::SMID_ZLibrary_Instance;
jmethodID AndroidUtil::MID_ZLibrary_getVersionName;

jmethodID AndroidUtil::SMID_ZLFile_createFileByPath;
jmethodID AndroidUtil::MID_ZLFile_size;
jmethodID AndroidUtil::MID_ZLFile_exists;
jmethodID AndroidUtil::MID_ZLFile_isDirectory;
jmethodID AndroidUtil::MID_ZLFile_getInputStream;
jmethodID AndroidUtil::MID_ZLFile_children;
jmethodID AndroidUtil::MID_ZLFile_getPath;

jmethodID AndroidUtil::MID_java_io_InputStream_close;
jmethodID AndroidUtil::MID_java_io_InputStream_read;
jmethodID AndroidUtil::MID_java_io_InputStream_skip;

jmethodID AndroidUtil::MID_java_util_List_toArray;

jmethodID AndroidUtil::SMID_java_util_Locale_getDefault;
jmethodID AndroidUtil::MID_java_util_Locale_getLanguage;

jfieldID AndroidUtil::FID_NativeFormatPlugin_NativePointer;
jmethodID AndroidUtil::MID_NativeFormatPlugin_init;
jmethodID AndroidUtil::SMID_NativeFormatPlugin_createImage;

jmethodID AndroidUtil::SMID_PluginCollection_Instance;
jmethodID AndroidUtil::MID_PluginCollection_getDefaultLanguage;
jmethodID AndroidUtil::MID_PluginCollection_getDefaultEncoding;
jmethodID AndroidUtil::MID_PluginCollection_isLanguageAutoDetectEnabled;

jmethodID AndroidUtil::SMID_Paths_cacheDirectory;

jfieldID AndroidUtil::FID_Book_File;
jmethodID AndroidUtil::MID_Book_getTitle;
jmethodID AndroidUtil::MID_Book_getLanguage;
jmethodID AndroidUtil::MID_Book_getEncoding;
jmethodID AndroidUtil::MID_Book_setTitle;
jmethodID AndroidUtil::MID_Book_setSeriesInfo;
jmethodID AndroidUtil::MID_Book_setLanguage;
jmethodID AndroidUtil::MID_Book_setEncoding;
jmethodID AndroidUtil::MID_Book_addAuthor;
jmethodID AndroidUtil::MID_Book_addTag;

jmethodID AndroidUtil::SMID_Tag_getTag;

jfieldID AndroidUtil::FID_BookModel_Book;

jmethodID AndroidUtil::MID_NativeBookModel_initBookModel;
jmethodID AndroidUtil::MID_NativeBookModel_createTextModel;
jmethodID AndroidUtil::MID_NativeBookModel_setBookTextModel;
jmethodID AndroidUtil::MID_NativeBookModel_setFootnoteModel;


void AndroidUtil::init(JavaVM* jvm) {
	ourJavaVM = jvm;

	JNIEnv *env = getEnv();
	jclass cls;

	cls = env->FindClass(Class_ZLibrary);
	SMID_ZLibrary_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/zlibrary/core/library/ZLibrary;");
	MID_ZLibrary_getVersionName = env->GetMethodID(cls, "getVersionName", "()Ljava/lang/String;");

	cls = env->FindClass(Class_ZLFile);
	SMID_ZLFile_createFileByPath = env->GetStaticMethodID(cls, "createFileByPath", "(Ljava/lang/String;)Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;");
	MID_ZLFile_size = env->GetMethodID(cls, "size", "()J");
	MID_ZLFile_exists = env->GetMethodID(cls, "exists", "()Z");
	MID_ZLFile_isDirectory = env->GetMethodID(cls, "isDirectory", "()Z");
	MID_ZLFile_getInputStream = env->GetMethodID(cls, "getInputStream", "()Ljava/io/InputStream;");
	MID_ZLFile_children = env->GetMethodID(cls, "children", "()Ljava/util/List;");
	MID_ZLFile_getPath = env->GetMethodID(cls, "getPath", "()Ljava/lang/String;");

	cls = env->FindClass(Class_java_io_InputStream);
	MID_java_io_InputStream_close = env->GetMethodID(cls, "close", "()V");
	MID_java_io_InputStream_read = env->GetMethodID(cls, "read", "([BII)I");
	MID_java_io_InputStream_skip = env->GetMethodID(cls, "skip", "(J)J");

	cls = env->FindClass(Class_java_util_List);
	MID_java_util_List_toArray = env->GetMethodID(cls, "toArray", "()[Ljava/lang/Object;");

	cls = env->FindClass(Class_java_util_Locale);
	SMID_java_util_Locale_getDefault = env->GetStaticMethodID(cls, "getDefault", "()Ljava/util/Locale;");
	MID_java_util_Locale_getLanguage = env->GetMethodID(cls, "getLanguage", "()Ljava/lang/String;");

	cls = env->FindClass(Class_NativeFormatPlugin);
	FID_NativeFormatPlugin_NativePointer = env->GetFieldID(cls, "myNativePointer", "J");
	MID_NativeFormatPlugin_init = env->GetMethodID(cls, "<init>", "(J)V");
	SMID_NativeFormatPlugin_createImage = env->GetStaticMethodID(cls, "createImage", "(Ljava/lang/String;Ljava/lang/String;II)Lorg/geometerplus/zlibrary/core/image/ZLImage;");

	cls = env->FindClass(Class_PluginCollection);
	SMID_PluginCollection_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/fbreader/formats/PluginCollection;");
	MID_PluginCollection_getDefaultLanguage = env->GetMethodID(cls, "getDefaultLanguage", "()Ljava/lang/String;");
	MID_PluginCollection_getDefaultEncoding = env->GetMethodID(cls, "getDefaultEncoding", "()Ljava/lang/String;");
	MID_PluginCollection_isLanguageAutoDetectEnabled = env->GetMethodID(cls, "isLanguageAutoDetectEnabled", "()Z");

	cls = env->FindClass(Class_Paths);
	SMID_Paths_cacheDirectory = env->GetStaticMethodID(cls, "cacheDirectory", "()Ljava/lang/String;");

	cls = env->FindClass(Class_Book);
	FID_Book_File = env->GetFieldID(cls, "File", "Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;");
	MID_Book_getTitle = env->GetMethodID(cls, "getTitle", "()Ljava/lang/String;");
	MID_Book_getLanguage = env->GetMethodID(cls, "getLanguage", "()Ljava/lang/String;");
	MID_Book_getEncoding = env->GetMethodID(cls, "getEncoding", "()Ljava/lang/String;");
	MID_Book_setTitle = env->GetMethodID(cls, "setTitle", "(Ljava/lang/String;)V");
	MID_Book_setSeriesInfo = env->GetMethodID(cls, "setSeriesInfo", "(Ljava/lang/String;F)V");
	MID_Book_setLanguage = env->GetMethodID(cls, "setLanguage", "(Ljava/lang/String;)V");
	MID_Book_setEncoding = env->GetMethodID(cls, "setEncoding", "(Ljava/lang/String;)V");
	MID_Book_addAuthor = env->GetMethodID(cls, "addAuthor", "(Ljava/lang/String;Ljava/lang/String;)V");
	MID_Book_addTag = env->GetMethodID(cls, "addTag", "(Lorg/geometerplus/fbreader/library/Tag;)V");

	cls = env->FindClass(Class_Tag);
	SMID_Tag_getTag = env->GetStaticMethodID(cls, "getTag", "(Lorg/geometerplus/fbreader/library/Tag;Ljava/lang/String;)Lorg/geometerplus/fbreader/library/Tag;");

	cls = env->FindClass(Class_BookModel);
	FID_BookModel_Book = env->GetFieldID(cls, "Book", "Lorg/geometerplus/fbreader/library/Book;");

	cls = env->FindClass(Class_NativeBookModel);
	MID_NativeBookModel_initBookModel = env->GetMethodID(cls, "initBookModel", "([Ljava/lang/String;[I[ILjava/lang/String;Ljava/lang/String;I)V");
	MID_NativeBookModel_createTextModel = env->GetMethodID(cls, "createTextModel", "(Ljava/lang/String;Ljava/lang/String;I[I[I[I[I[BLjava/lang/String;Ljava/lang/String;I)Lorg/geometerplus/zlibrary/text/model/ZLTextModel;");
	MID_NativeBookModel_setBookTextModel = env->GetMethodID(cls, "setBookTextModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)V");
	MID_NativeBookModel_setFootnoteModel = env->GetMethodID(cls, "setFootnoteModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)V");
}


JNIEnv *AndroidUtil::getEnv() {
	JNIEnv *env;
	ourJavaVM->GetEnv((void **)&env, JNI_VERSION_1_2);
	return env;
}

jobject AndroidUtil::createZLFile(JNIEnv *env, const std::string &path) {
	jstring javaPath = env->NewStringUTF(path.c_str());
	jclass cls = env->FindClass(Class_ZLFile);
	jobject javaFile = env->CallStaticObjectMethod(cls, SMID_ZLFile_createFileByPath, javaPath);
	env->DeleteLocalRef(cls);
	env->DeleteLocalRef(javaPath);
	return javaFile;
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
