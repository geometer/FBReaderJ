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

#include <ZLFile.h>
#include <FileEncryptionInfo.h>
#include <ZLFileImage.h>
#include <ZLUnicodeUtil.h>

#include "AndroidUtil.h"
#include "JniEnvelope.h"

JavaVM *AndroidUtil::ourJavaVM = 0;

JavaClass AndroidUtil::Class_java_lang_Object("java/lang/Object");
JavaArray AndroidUtil::Array_java_lang_Object(Class_java_lang_Object);
JavaClass AndroidUtil::Class_java_lang_RuntimeException("java/lang/RuntimeException");
JavaClass AndroidUtil::Class_java_lang_String("java/lang/String");
JavaClass AndroidUtil::Class_java_util_Collection("java/util/Collection");
JavaClass AndroidUtil::Class_java_util_List("java/util/List");
JavaClass AndroidUtil::Class_java_util_Locale("java/util/Locale");
JavaClass AndroidUtil::Class_java_io_InputStream("java/io/InputStream");

JavaClass AndroidUtil::Class_ZLibrary("org/geometerplus/zlibrary/core/library/ZLibrary");
JavaClass AndroidUtil::Class_ZLFile("org/geometerplus/zlibrary/core/filesystem/ZLFile");
JavaClass AndroidUtil::Class_FileInfo("org/geometerplus/zlibrary/core/fonts/FileInfo");
JavaClass AndroidUtil::Class_FileEncryptionInfo("org/geometerplus/zlibrary/core/drm/FileEncryptionInfo");
JavaClass AndroidUtil::Class_ZLFileImage("org/geometerplus/zlibrary/core/image/ZLFileImage");
JavaClass AndroidUtil::Class_ZLTextModel("org/geometerplus/zlibrary/text/model/ZLTextModel");

JavaClass AndroidUtil::Class_Encoding("org/geometerplus/zlibrary/core/encodings/Encoding");
JavaClass AndroidUtil::Class_EncodingConverter("org/geometerplus/zlibrary/core/encodings/EncodingConverter");
JavaClass AndroidUtil::Class_JavaEncodingCollection("org/geometerplus/zlibrary/core/encodings/JavaEncodingCollection");

JavaClass AndroidUtil::Class_NativeFormatPlugin("org/geometerplus/fbreader/formats/NativeFormatPlugin");
JavaClass AndroidUtil::Class_PluginCollection("org/geometerplus/fbreader/formats/PluginCollection");
JavaClass AndroidUtil::Class_Paths("org/geometerplus/fbreader/Paths");
JavaClass AndroidUtil::Class_AbstractBook("org/geometerplus/fbreader/book/AbstractBook");
JavaClass AndroidUtil::Class_Book("org/geometerplus/fbreader/book/Book");
JavaClass AndroidUtil::Class_Tag("org/geometerplus/fbreader/book/Tag");
JavaClass AndroidUtil::Class_BookModel("org/geometerplus/fbreader/bookmodel/BookModel");

shared_ptr<StringMethod> AndroidUtil::Method_java_lang_String_toLowerCase;
shared_ptr<StringMethod> AndroidUtil::Method_java_lang_String_toUpperCase;

shared_ptr<ObjectArrayMethod> AndroidUtil::Method_java_util_Collection_toArray;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_java_util_Locale_getDefault;
shared_ptr<StringMethod> AndroidUtil::Method_java_util_Locale_getLanguage;

shared_ptr<VoidMethod> AndroidUtil::Method_java_io_InputStream_close;
shared_ptr<IntMethod> AndroidUtil::Method_java_io_InputStream_read;
shared_ptr<LongMethod> AndroidUtil::Method_java_io_InputStream_skip;
shared_ptr<VoidMethod> AndroidUtil::Method_java_io_InputStream_mark;
shared_ptr<BooleanMethod> AndroidUtil::Method_java_io_InputStream_markSupported;
shared_ptr<VoidMethod> AndroidUtil::Method_java_io_InputStream_reset;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_ZLibrary_Instance;
shared_ptr<StringMethod> AndroidUtil::Method_ZLibrary_getVersionName;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_NativeFormatPlugin_create;
shared_ptr<StringMethod> AndroidUtil::Method_NativeFormatPlugin_supportedFileType;

//shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_PluginCollection_Instance;

shared_ptr<ObjectMethod> AndroidUtil::Method_Encoding_createConverter;

shared_ptr<ObjectField> AndroidUtil::Field_EncodingConverter_Name;
shared_ptr<IntMethod> AndroidUtil::Method_EncodingConverter_convert;
shared_ptr<VoidMethod> AndroidUtil::Method_EncodingConverter_reset;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_JavaEncodingCollection_Instance;
//shared_ptr<ObjectMethod> AndroidUtil::Method_JavaEncodingCollection_getEncoding_int;
shared_ptr<ObjectMethod> AndroidUtil::Method_JavaEncodingCollection_getEncoding;
shared_ptr<BooleanMethod> AndroidUtil::Method_JavaEncodingCollection_providesConverterFor;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_ZLFile_createFileByPath;
shared_ptr<ObjectMethod> AndroidUtil::Method_ZLFile_children;
shared_ptr<BooleanMethod> AndroidUtil::Method_ZLFile_exists;
shared_ptr<ObjectMethod> AndroidUtil::Method_ZLFile_getInputStream;
shared_ptr<StringMethod> AndroidUtil::Method_ZLFile_getPath;
shared_ptr<BooleanMethod> AndroidUtil::Method_ZLFile_isDirectory;
shared_ptr<LongMethod> AndroidUtil::Method_ZLFile_size;
shared_ptr<LongMethod> AndroidUtil::Method_ZLFile_lastModified;

shared_ptr<Constructor> AndroidUtil::Constructor_FileInfo;
shared_ptr<Constructor> AndroidUtil::Constructor_FileEncryptionInfo;

shared_ptr<Constructor> AndroidUtil::Constructor_ZLFileImage;

shared_ptr<StringMethod> AndroidUtil::Method_Book_getPath;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getTitle;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getLanguage;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getEncodingNoDetection;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setTitle;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setSeriesInfo;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setLanguage;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setEncoding;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_addAuthor;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_addTag;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_addUid;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_Tag_getTag;

shared_ptr<ObjectField> AndroidUtil::Field_BookModel_Book;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_initInternalHyperlinks;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_addTOCItem;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_leaveTOCItem;
shared_ptr<ObjectMethod> AndroidUtil::Method_BookModel_createTextModel;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_setBookTextModel;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_setFootnoteModel;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_addImage;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_registerFontFamilyList;
shared_ptr<VoidMethod> AndroidUtil::Method_BookModel_registerFontEntry;

JNIEnv *AndroidUtil::getEnv() {
	JNIEnv *env;
	ourJavaVM->GetEnv((void **)&env, JNI_VERSION_1_2);
	return env;
}

bool AndroidUtil::init(JavaVM* jvm) {
	ourJavaVM = jvm;

	Method_java_lang_String_toLowerCase = new StringMethod(Class_java_lang_String, "toLowerCase", "()");
	Method_java_lang_String_toUpperCase = new StringMethod(Class_java_lang_String, "toUpperCase", "()");

	Method_java_util_Collection_toArray = new ObjectArrayMethod(Class_java_util_Collection, "toArray", Array_java_lang_Object, "()");

	StaticMethod_java_util_Locale_getDefault = new StaticObjectMethod(Class_java_util_Locale, "getDefault", Class_java_util_Locale, "()");
	Method_java_util_Locale_getLanguage = new StringMethod(Class_java_util_Locale, "getLanguage", "()");

	Method_java_io_InputStream_close = new VoidMethod(Class_java_io_InputStream, "close", "()");
	Method_java_io_InputStream_read = new IntMethod(Class_java_io_InputStream, "read", "([BII)");
	Method_java_io_InputStream_skip = new LongMethod(Class_java_io_InputStream, "skip", "(J)");
	Method_java_io_InputStream_mark = new VoidMethod(Class_java_io_InputStream, "mark", "(I)");
	Method_java_io_InputStream_markSupported = new BooleanMethod(Class_java_io_InputStream, "markSupported", "()");
	Method_java_io_InputStream_reset = new VoidMethod(Class_java_io_InputStream, "reset", "()");

	StaticMethod_ZLibrary_Instance = new StaticObjectMethod(Class_ZLibrary, "Instance", Class_ZLibrary, "()");
	Method_ZLibrary_getVersionName = new StringMethod(Class_ZLibrary, "getVersionName", "()");

	StaticMethod_NativeFormatPlugin_create = new StaticObjectMethod(Class_NativeFormatPlugin, "create", Class_NativeFormatPlugin, "(Lorg/geometerplus/zlibrary/core/util/SystemInfo;Ljava/lang/String;)");
	Method_NativeFormatPlugin_supportedFileType = new StringMethod(Class_NativeFormatPlugin, "supportedFileType", "()");

	//StaticMethod_PluginCollection_Instance = new StaticObjectMethod(Class_PluginCollection, "Instance", Class_PluginCollection, "()");

	Method_Encoding_createConverter = new ObjectMethod(Class_Encoding, "createConverter", Class_EncodingConverter, "()");
	Field_EncodingConverter_Name = new ObjectField(Class_EncodingConverter, "Name", Class_java_lang_String);
	Method_EncodingConverter_convert = new IntMethod(Class_EncodingConverter, "convert", "([BII[C)");
	Method_EncodingConverter_reset = new VoidMethod(Class_EncodingConverter, "reset", "()");

	StaticMethod_JavaEncodingCollection_Instance = new StaticObjectMethod(Class_JavaEncodingCollection, "Instance", Class_JavaEncodingCollection, "()");
	Method_JavaEncodingCollection_getEncoding = new ObjectMethod(Class_JavaEncodingCollection, "getEncoding", Class_Encoding, "(Ljava/lang/String;)");
	//Method_JavaEncodingCollection_getEncoding_int = new ObjectMethod(Class_JavaEncodingCollection, "getEncoding", Class_Encoding, "(I)");
	Method_JavaEncodingCollection_providesConverterFor = new BooleanMethod(Class_JavaEncodingCollection, "providesConverterFor", "(Ljava/lang/String;)");

	StaticMethod_ZLFile_createFileByPath = new StaticObjectMethod(Class_ZLFile, "createFileByPath", Class_ZLFile, "(Ljava/lang/String;)");
	Method_ZLFile_children = new ObjectMethod(Class_ZLFile, "children", Class_java_util_List, "()");
	Method_ZLFile_exists = new BooleanMethod(Class_ZLFile, "exists", "()");
	Method_ZLFile_isDirectory = new BooleanMethod(Class_ZLFile, "isDirectory", "()");
	Method_ZLFile_getInputStream = new ObjectMethod(Class_ZLFile, "getInputStream", Class_java_io_InputStream, "()");
	Method_ZLFile_getPath = new StringMethod(Class_ZLFile, "getPath", "()");
	Method_ZLFile_size = new LongMethod(Class_ZLFile, "size", "()");
	Method_ZLFile_lastModified = new LongMethod(Class_ZLFile, "lastModified", "()");

	Constructor_FileInfo = new Constructor(Class_FileInfo, "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/drm/FileEncryptionInfo;)V");
	Constructor_FileEncryptionInfo = new Constructor(Class_FileEncryptionInfo, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

	Constructor_ZLFileImage = new Constructor(Class_ZLFileImage, "(Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;Ljava/lang/String;[I[ILorg/geometerplus/zlibrary/core/drm/FileEncryptionInfo;)V");

	Method_Book_getPath = new StringMethod(Class_AbstractBook, "getPath", "()");
	Method_Book_getTitle = new StringMethod(Class_AbstractBook, "getTitle", "()");
	Method_Book_getLanguage = new StringMethod(Class_AbstractBook, "getLanguage", "()");
	Method_Book_getEncodingNoDetection = new StringMethod(Class_AbstractBook, "getEncodingNoDetection", "()");
	Method_Book_setTitle = new VoidMethod(Class_AbstractBook, "setTitle", "(Ljava/lang/String;)");
	Method_Book_setSeriesInfo = new VoidMethod(Class_AbstractBook, "setSeriesInfo", "(Ljava/lang/String;Ljava/lang/String;)");
	Method_Book_setLanguage = new VoidMethod(Class_AbstractBook, "setLanguage", "(Ljava/lang/String;)");
	Method_Book_setEncoding = new VoidMethod(Class_AbstractBook, "setEncoding", "(Ljava/lang/String;)");
	Method_Book_addAuthor = new VoidMethod(Class_AbstractBook, "addAuthor", "(Ljava/lang/String;Ljava/lang/String;)");
	Method_Book_addTag = new VoidMethod(Class_AbstractBook, "addTag", "(Lorg/geometerplus/fbreader/book/Tag;)");
	Method_Book_addUid = new VoidMethod(Class_AbstractBook, "addUid", "(Ljava/lang/String;Ljava/lang/String;)");

	StaticMethod_Tag_getTag = new StaticObjectMethod(Class_Tag, "getTag", Class_Tag, "(Lorg/geometerplus/fbreader/book/Tag;Ljava/lang/String;)");

	Field_BookModel_Book = new ObjectField(Class_BookModel, "Book", Class_Book);
	Method_BookModel_initInternalHyperlinks = new VoidMethod(Class_BookModel, "initInternalHyperlinks", "(Ljava/lang/String;Ljava/lang/String;I)");
	Method_BookModel_addTOCItem = new VoidMethod(Class_BookModel, "addTOCItem", "(Ljava/lang/String;I)");
	Method_BookModel_leaveTOCItem = new VoidMethod(Class_BookModel, "leaveTOCItem", "()");
	Method_BookModel_createTextModel = new ObjectMethod(Class_BookModel, "createTextModel", Class_ZLTextModel, "(Ljava/lang/String;Ljava/lang/String;I[I[I[I[I[BLjava/lang/String;Ljava/lang/String;I)");
	Method_BookModel_setBookTextModel = new VoidMethod(Class_BookModel, "setBookTextModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)");
	Method_BookModel_setFootnoteModel = new VoidMethod(Class_BookModel, "setFootnoteModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)");
	Method_BookModel_addImage = new VoidMethod(Class_BookModel, "addImage", "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/image/ZLImage;)");
	Method_BookModel_registerFontFamilyList = new VoidMethod(Class_BookModel, "registerFontFamilyList", "([Ljava/lang/String;)");
	Method_BookModel_registerFontEntry = new VoidMethod(Class_BookModel, "registerFontEntry", "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/fonts/FileInfo;Lorg/geometerplus/zlibrary/core/fonts/FileInfo;Lorg/geometerplus/zlibrary/core/fonts/FileInfo;Lorg/geometerplus/zlibrary/core/fonts/FileInfo;)");

	return true;
}

jobject AndroidUtil::createJavaFile(JNIEnv *env, const std::string &path) {
	JString javaPath(env, path, false);
	return StaticMethod_ZLFile_createFileByPath->call(javaPath.j());
}

jobject AndroidUtil::createJavaEncryptionInfo(JNIEnv *env, shared_ptr<FileEncryptionInfo> info) {
	if (info.isNull()) {
		return 0;
	}

	JString uri(env, info->Uri, false);
	JString method(env, info->Method, false);
	JString algorithm(env, info->Algorithm, false);
	JString contentId(env, info->ContentId, false);

	return Constructor_FileEncryptionInfo->call(uri.j(), method.j(), algorithm.j(), contentId.j());
}

jobject AndroidUtil::createJavaImage(JNIEnv *env, const ZLFileImage &image) {
	jobject javaFile = createJavaFile(env, image.file().path());
	JString javaEncoding(env, image.encoding());

	std::vector<jint> offsets, sizes;
	const ZLFileImage::Blocks &blocks = image.blocks();
	for (std::size_t i = 0; i < blocks.size(); ++i) {
		offsets.push_back((jint)blocks.at(i).offset);
		sizes.push_back((jint)blocks.at(i).size);
	}
	jintArray javaOffsets = createJavaIntArray(env, offsets);
	jintArray javaSizes = createJavaIntArray(env, sizes);

	jobject javaEncryptionInfo = createJavaEncryptionInfo(env, image.encryptionInfo());

	jobject javaImage = Constructor_ZLFileImage->call(
		javaFile, javaEncoding.j(),
		javaOffsets, javaSizes, javaEncryptionInfo
	);

	if (javaEncryptionInfo != 0) {
		env->DeleteLocalRef(javaEncryptionInfo);
	}

	env->DeleteLocalRef(javaFile);
	env->DeleteLocalRef(javaOffsets);
	env->DeleteLocalRef(javaSizes);

	return javaImage;
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

JString::JString(JNIEnv* env, const std::string &str, bool emptyIsNull) : myEnv(env) {
	myJ = (emptyIsNull && str.empty()) ? 0 : env->NewStringUTF(str.c_str());
}

JString::~JString() {
	if (myJ != 0) {
		myEnv->DeleteLocalRef(myJ);
	}
}

jstring AndroidUtil::createJavaString(JNIEnv* env, const std::string &str) {
	if (str.empty()) {
		return 0;
	}
	return env->NewStringUTF(str.c_str());
}

std::string AndroidUtil::convertNonUtfString(const std::string &str) {
	if (ZLUnicodeUtil::isUtf8String(str)) {
		return str;
	}

	JNIEnv *env = getEnv();

	const int len = str.length();
	jchar *chars = new jchar[len];
	for (int i = 0; i < len; ++i) {
		chars[i] = (unsigned char)str[i];
	}
	jstring javaString = env->NewString(chars, len);
	const std::string result = fromJavaString(env, javaString);
	env->DeleteLocalRef(javaString);
	delete[] chars;

	return result;
}

jintArray AndroidUtil::createJavaIntArray(JNIEnv *env, const std::vector<jint> &data) {
	std::size_t size = data.size();
	jintArray array = env->NewIntArray(size);
	env->SetIntArrayRegion(array, 0, size, &data.front());
	return array;
}

jbyteArray AndroidUtil::createJavaByteArray(JNIEnv *env, const std::vector<jbyte> &data) {
	std::size_t size = data.size();
	jbyteArray array = env->NewByteArray(size);
	env->SetByteArrayRegion(array, 0, size, &data.front());
	return array;
}
