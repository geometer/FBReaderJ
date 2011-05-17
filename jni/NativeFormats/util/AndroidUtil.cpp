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
const char * const AndroidUtil::Class_ZLFile = "org/geometerplus/zlibrary/core/filesystem/ZLFile";
const char * const AndroidUtil::Class_NativeFormatPlugin = "org/geometerplus/fbreader/formats/NativeFormatPlugin";
const char * const AndroidUtil::Class_PluginCollection = "org/geometerplus/fbreader/formats/PluginCollection";
const char * const AndroidUtil::Class_Paths = "org/geometerplus/fbreader/Paths";

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

jmethodID AndroidUtil::SMID_PluginCollection_Instance;
jmethodID AndroidUtil::MID_PluginCollection_getDefaultLanguage;
jmethodID AndroidUtil::MID_PluginCollection_getDefaultEncoding;
jmethodID AndroidUtil::MID_PluginCollection_isLanguageAutoDetectEnabled;

jmethodID AndroidUtil::SMID_Paths_cacheDirectory;


void AndroidUtil::init(JavaVM* jvm) {
	ourJavaVM = jvm;

	JNIEnv *env = getEnv();

	jclass cls = env->FindClass(Class_ZLFile);
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

	cls = env->FindClass(Class_PluginCollection);
	SMID_PluginCollection_Instance = env->GetStaticMethodID(cls, "Instance", "()Lorg/geometerplus/fbreader/formats/PluginCollection;");
	MID_PluginCollection_getDefaultLanguage = env->GetMethodID(cls, "getDefaultLanguage", "()Ljava/lang/String;");
	MID_PluginCollection_getDefaultEncoding = env->GetMethodID(cls, "getDefaultEncoding", "()Ljava/lang/String;");
	MID_PluginCollection_isLanguageAutoDetectEnabled = env->GetMethodID(cls, "isLanguageAutoDetectEnabled", "()Z");

	cls = env->FindClass(Class_Paths);
	SMID_Paths_cacheDirectory = env->GetStaticMethodID(cls, "cacheDirectory", "()Ljava/lang/String;");
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
