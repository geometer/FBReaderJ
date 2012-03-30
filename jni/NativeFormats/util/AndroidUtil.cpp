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
#include <ZLFileImage.h>

#include "AndroidUtil.h"
#include "JniEnvelope.h"

JavaVM *AndroidUtil::ourJavaVM = 0;

shared_ptr<JavaClass> AndroidUtil::Class_java_lang_RuntimeException;
shared_ptr<JavaClass> AndroidUtil::Class_java_lang_String;
shared_ptr<JavaClass> AndroidUtil::Class_java_util_Collection;
shared_ptr<JavaClass> AndroidUtil::Class_java_util_Locale;
shared_ptr<JavaClass> AndroidUtil::Class_java_io_InputStream;
shared_ptr<JavaClass> AndroidUtil::Class_ZLibrary;
shared_ptr<JavaClass> AndroidUtil::Class_NativeFormatPlugin;
shared_ptr<JavaClass> AndroidUtil::Class_PluginCollection;
shared_ptr<JavaClass> AndroidUtil::Class_Encoding;
shared_ptr<JavaClass> AndroidUtil::Class_EncodingConverter;
shared_ptr<JavaClass> AndroidUtil::Class_JavaEncodingCollection;
shared_ptr<JavaClass> AndroidUtil::Class_Paths;
shared_ptr<JavaClass> AndroidUtil::Class_ZLFile;
shared_ptr<JavaClass> AndroidUtil::Class_ZLFileImage;
shared_ptr<JavaClass> AndroidUtil::Class_Book;
shared_ptr<JavaClass> AndroidUtil::Class_Tag;
shared_ptr<JavaClass> AndroidUtil::Class_NativeBookModel;
//shared_ptr<JavaClass> AndroidUtil::Class_BookReadingException;

shared_ptr<StringMethod> AndroidUtil::Method_java_lang_String_toLowerCase;
shared_ptr<StringMethod> AndroidUtil::Method_java_lang_String_toUpperCase;

jmethodID AndroidUtil::MID_java_util_Collection_toArray;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_java_util_Locale_getDefault;
shared_ptr<StringMethod> AndroidUtil::Method_java_util_Locale_getLanguage;

shared_ptr<VoidMethod> AndroidUtil::Method_java_io_InputStream_close;
shared_ptr<IntMethod> AndroidUtil::Method_java_io_InputStream_read;
shared_ptr<LongMethod> AndroidUtil::Method_java_io_InputStream_skip;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_ZLibrary_Instance;
shared_ptr<StringMethod> AndroidUtil::Method_ZLibrary_getVersionName;

shared_ptr<Constructor> AndroidUtil::Constructor_NativeFormatPlugin;
shared_ptr<StringMethod> AndroidUtil::Method_NativeFormatPlugin_supportedFileType;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_PluginCollection_Instance;

shared_ptr<ObjectMethod> AndroidUtil::Method_Encoding_createConverter;

jfieldID AndroidUtil::FID_EncodingConverter_Name;
shared_ptr<IntMethod> AndroidUtil::Method_EncodingConverter_convert;
shared_ptr<VoidMethod> AndroidUtil::Method_EncodingConverter_reset;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_JavaEncodingCollection_Instance;
shared_ptr<ObjectMethod> AndroidUtil::Method_JavaEncodingCollection_getEncoding_int;
shared_ptr<ObjectMethod> AndroidUtil::Method_JavaEncodingCollection_getEncoding_String;
shared_ptr<BooleanMethod> AndroidUtil::Method_JavaEncodingCollection_providesConverterFor;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_ZLFile_createFileByPath;
shared_ptr<ObjectMethod> AndroidUtil::Method_ZLFile_children;
shared_ptr<BooleanMethod> AndroidUtil::Method_ZLFile_exists;
shared_ptr<ObjectMethod> AndroidUtil::Method_ZLFile_getInputStream;
shared_ptr<StringMethod> AndroidUtil::Method_ZLFile_getPath;
shared_ptr<BooleanMethod> AndroidUtil::Method_ZLFile_isDirectory;
shared_ptr<LongMethod> AndroidUtil::Method_ZLFile_size;

shared_ptr<Constructor> AndroidUtil::Constructor_ZLFileImage;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_Paths_cacheDirectory;

jfieldID AndroidUtil::FID_Book_File;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getTitle;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getLanguage;
shared_ptr<StringMethod> AndroidUtil::Method_Book_getEncodingNoDetection;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setTitle;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setSeriesInfo;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setLanguage;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_setEncoding;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_addAuthor;
shared_ptr<VoidMethod> AndroidUtil::Method_Book_addTag;
shared_ptr<BooleanMethod> AndroidUtil::Method_Book_save;

shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_Tag_getTag;

jfieldID AndroidUtil::FID_NativeBookModel_Book;
shared_ptr<VoidMethod> AndroidUtil::Method_NativeBookModel_initInternalHyperlinks;
shared_ptr<VoidMethod> AndroidUtil::Method_NativeBookModel_initTOC;
shared_ptr<ObjectMethod> AndroidUtil::Method_NativeBookModel_createTextModel;
shared_ptr<VoidMethod> AndroidUtil::Method_NativeBookModel_setBookTextModel;
shared_ptr<VoidMethod> AndroidUtil::Method_NativeBookModel_setFootnoteModel;
shared_ptr<VoidMethod> AndroidUtil::Method_NativeBookModel_addImage;

//shared_ptr<StaticObjectMethod> AndroidUtil::StaticMethod_BookReadingException_throwForFile;

JNIEnv *AndroidUtil::getEnv() {
	JNIEnv *env;
	ourJavaVM->GetEnv((void **)&env, JNI_VERSION_1_2);
	return env;
}

#define CHECK_NULL(value) if ((value) == 0) { return false; }

bool AndroidUtil::init(JavaVM* jvm) {
	ourJavaVM = jvm;

	JNIEnv *env = getEnv();

	Class_java_lang_RuntimeException = new JavaClass(env, "java/lang/RuntimeException");

	Class_java_lang_String = new JavaClass(env, "java/lang/String");
	Method_java_lang_String_toLowerCase = new StringMethod(*Class_java_lang_String, "toLowerCase", "()");
	Method_java_lang_String_toUpperCase = new StringMethod(*Class_java_lang_String, "toUpperCase", "()");

	Class_java_util_Collection = new JavaClass(env, "java/util/Collection");
	CHECK_NULL( MID_java_util_Collection_toArray = env->GetMethodID(Class_java_util_Collection->j(), "toArray", "()[Ljava/lang/Object;") );

	Class_java_util_Locale = new JavaClass(env, "java/util/Locale");
	StaticMethod_java_util_Locale_getDefault = new StaticObjectMethod(env, Class_java_util_Locale->j(), "getDefault", "java/util/Locale", "()");
	Method_java_util_Locale_getLanguage = new StringMethod(*Class_java_util_Locale, "getLanguage", "()");

	Class_java_io_InputStream = new JavaClass(env, "java/io/InputStream");
	Method_java_io_InputStream_close = new VoidMethod(env, Class_java_io_InputStream->j(), "close", "()");
	Method_java_io_InputStream_read = new IntMethod(env, Class_java_io_InputStream->j(), "read", "([BII)");
	Method_java_io_InputStream_skip = new LongMethod(env, Class_java_io_InputStream->j(), "skip", "(J)");

	Class_ZLibrary = new JavaClass(env, "org/geometerplus/zlibrary/core/library/ZLibrary");
	StaticMethod_ZLibrary_Instance = new StaticObjectMethod(env, Class_ZLibrary->j(), "Instance", "org/geometerplus/zlibrary/core/library/ZLibrary", "()");
	Method_ZLibrary_getVersionName = new StringMethod(*Class_ZLibrary, "getVersionName", "()");

	Class_NativeFormatPlugin = new JavaClass(env, "org/geometerplus/fbreader/formats/NativeFormatPlugin");
	Constructor_NativeFormatPlugin = new Constructor(*Class_NativeFormatPlugin, "(Ljava/lang/String;)V");
	Method_NativeFormatPlugin_supportedFileType = new StringMethod(*Class_NativeFormatPlugin, "supportedFileType", "()");

	Class_PluginCollection = new JavaClass(env, "org/geometerplus/fbreader/formats/PluginCollection");
	StaticMethod_PluginCollection_Instance = new StaticObjectMethod(env, Class_PluginCollection->j(), "Instance", "org/geometerplus/fbreader/formats/PluginCollection", "()");

	Class_Encoding = new JavaClass(env, "org/geometerplus/zlibrary/core/encodings/Encoding");
	Method_Encoding_createConverter = new ObjectMethod(env, Class_Encoding->j(), "createConverter", "org/geometerplus/zlibrary/core/encodings/EncodingConverter", "()");

	Class_EncodingConverter = new JavaClass(env, "org/geometerplus/zlibrary/core/encodings/EncodingConverter");
	CHECK_NULL( FID_EncodingConverter_Name = env->GetFieldID(Class_EncodingConverter->j(), "Name", "Ljava/lang/String;") );
	Method_EncodingConverter_convert = new IntMethod(env, Class_EncodingConverter->j(), "convert", "([BII[BI)");
	Method_EncodingConverter_reset = new VoidMethod(env, Class_EncodingConverter->j(), "reset", "()");

	Class_JavaEncodingCollection = new JavaClass(env, "org/geometerplus/zlibrary/core/encodings/JavaEncodingCollection");
	StaticMethod_JavaEncodingCollection_Instance = new StaticObjectMethod(env, Class_JavaEncodingCollection->j(), "Instance", "org/geometerplus/zlibrary/core/encodings/JavaEncodingCollection", "()");
	Method_JavaEncodingCollection_getEncoding_String = new ObjectMethod(env, Class_JavaEncodingCollection->j(), "getEncoding", "org/geometerplus/zlibrary/core/encodings/Encoding", "(Ljava/lang/String;)");
	Method_JavaEncodingCollection_getEncoding_int = new ObjectMethod(env, Class_JavaEncodingCollection->j(), "getEncoding", "org/geometerplus/zlibrary/core/encodings/Encoding", "(I)");
	Method_JavaEncodingCollection_providesConverterFor = new BooleanMethod(env, Class_JavaEncodingCollection->j(), "providesConverterFor", "(Ljava/lang/String;)");

	Class_ZLFile = new JavaClass(env, "org/geometerplus/zlibrary/core/filesystem/ZLFile");
	StaticMethod_ZLFile_createFileByPath = new StaticObjectMethod(env, Class_ZLFile->j(), "createFileByPath", "org/geometerplus/zlibrary/core/filesystem/ZLFile", "(Ljava/lang/String;)");
	Method_ZLFile_children = new ObjectMethod(env, Class_ZLFile->j(), "children", "java/util/List", "()");
	Method_ZLFile_exists = new BooleanMethod(env, Class_ZLFile->j(), "exists", "()");
	Method_ZLFile_isDirectory = new BooleanMethod(env, Class_ZLFile->j(), "isDirectory", "()");
	Method_ZLFile_getInputStream = new ObjectMethod(env, Class_ZLFile->j(), "getInputStream", "java/io/InputStream", "()");
	Method_ZLFile_getPath = new StringMethod(*Class_ZLFile, "getPath", "()");
	Method_ZLFile_size = new LongMethod(env, Class_ZLFile->j(), "size", "()");

 	Class_ZLFileImage = new JavaClass(env, "org/geometerplus/zlibrary/core/image/ZLFileImage");
	Constructor_ZLFileImage = new Constructor(*Class_ZLFileImage, "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;Ljava/lang/String;II)V");

	Class_Paths = new JavaClass(env, "org/geometerplus/fbreader/Paths");
	StaticMethod_Paths_cacheDirectory = new StaticObjectMethod(env, Class_Paths->j(), "cacheDirectory", "java/lang/String", "()");

	Class_Book = new JavaClass(env, "org/geometerplus/fbreader/library/Book");
	CHECK_NULL( FID_Book_File = env->GetFieldID(Class_Book->j(), "File", "Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;") );
	Method_Book_getTitle = new StringMethod(*Class_Book, "getTitle", "()");
	Method_Book_getLanguage = new StringMethod(*Class_Book, "getLanguage", "()");
	Method_Book_getEncodingNoDetection = new StringMethod(*Class_Book, "getEncodingNoDetection", "()");
	Method_Book_setTitle = new VoidMethod(env, Class_Book->j(), "setTitle", "(Ljava/lang/String;)");
	Method_Book_setSeriesInfo = new VoidMethod(env, Class_Book->j(), "setSeriesInfo", "(Ljava/lang/String;F)");
	Method_Book_setLanguage = new VoidMethod(env, Class_Book->j(), "setLanguage", "(Ljava/lang/String;)");
	Method_Book_setEncoding = new VoidMethod(env, Class_Book->j(), "setEncoding", "(Ljava/lang/String;)");
	Method_Book_addAuthor = new VoidMethod(env, Class_Book->j(), "addAuthor", "(Ljava/lang/String;Ljava/lang/String;)");
	Method_Book_addTag = new VoidMethod(env, Class_Book->j(), "addTag", "(Lorg/geometerplus/fbreader/library/Tag;)");
	Method_Book_save = new BooleanMethod(env, Class_Book->j(), "save", "()");

	Class_Tag = new JavaClass(env, "org/geometerplus/fbreader/library/Tag");
	StaticMethod_Tag_getTag = new StaticObjectMethod(env, Class_Tag->j(), "getTag", "org/geometerplus/fbreader/library/Tag", "(Lorg/geometerplus/fbreader/library/Tag;Ljava/lang/String;)");

	Class_NativeBookModel = new JavaClass(env, "org/geometerplus/fbreader/bookmodel/NativeBookModel");
	CHECK_NULL( FID_NativeBookModel_Book = env->GetFieldID(Class_NativeBookModel->j(), "Book", "Lorg/geometerplus/fbreader/library/Book;") );
	Method_NativeBookModel_initInternalHyperlinks = new VoidMethod(env, Class_NativeBookModel->j(), "initInternalHyperlinks", "(Ljava/lang/String;Ljava/lang/String;I)");
	Method_NativeBookModel_initTOC = new VoidMethod(env, Class_NativeBookModel->j(), "initTOC", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;[I[I)");
	Method_NativeBookModel_createTextModel = new ObjectMethod(env, Class_NativeBookModel->j(), "createTextModel", "org/geometerplus/zlibrary/text/model/ZLTextModel", "(Ljava/lang/String;Ljava/lang/String;I[I[I[I[I[BLjava/lang/String;Ljava/lang/String;I)");
	Method_NativeBookModel_setBookTextModel = new VoidMethod(env, Class_NativeBookModel->j(), "setBookTextModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)");
	Method_NativeBookModel_setFootnoteModel = new VoidMethod(env, Class_NativeBookModel->j(), "setFootnoteModel", "(Lorg/geometerplus/zlibrary/text/model/ZLTextModel;)");
	Method_NativeBookModel_addImage = new VoidMethod(env, Class_NativeBookModel->j(), "addImage", "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/image/ZLImage;)");

/*
	Class_BookReadingException = new JavaClass(env, "org/geometerplus/fbreader/bookmodel/BookReadingException");
	StaticMethod_BookReadingException_throwForFile = new StaticVoidMethod(*Class_BookReadingException, "throwForFile", "(Ljava/lang/String;Lorg/geometerplus/zlibrary/core/filesystem/ZLFile;)V") );
*/

	return true;
}

jobject AndroidUtil::createJavaFile(JNIEnv *env, const std::string &path) {
	jstring javaPath = env->NewStringUTF(path.c_str());
	jobject javaFile = StaticMethod_ZLFile_createFileByPath->call(Class_ZLFile->j(), javaPath);
	env->DeleteLocalRef(javaPath);
	return javaFile;
}

jobject AndroidUtil::createJavaImage(JNIEnv *env, const ZLFileImage &image) {
	jstring javaMimeType = createJavaString(env, image.mimeType());
	jobject javaFile = createJavaFile(env, image.file().path());
	jstring javaEncoding = createJavaString(env, image.encoding());

	jobject javaImage = Constructor_ZLFileImage->call(
		javaMimeType, javaFile, javaEncoding,
		image.offset(), image.size()
	);

	env->DeleteLocalRef(javaEncoding);
	env->DeleteLocalRef(javaFile);
	env->DeleteLocalRef(javaMimeType);

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

jintArray AndroidUtil::createJavaIntArray(JNIEnv *env, const std::vector<jint> &data) {
	size_t size = data.size();
	jintArray array = env->NewIntArray(size);
	env->SetIntArrayRegion(array, 0, size, &data.front());
	return array;
}

jbyteArray AndroidUtil::createJavaByteArray(JNIEnv *env, const std::vector<jbyte> &data) {
	size_t size = data.size();
	jbyteArray array = env->NewByteArray(size);
	env->SetByteArrayRegion(array, 0, size, &data.front());
	return array;
}

void AndroidUtil::throwRuntimeException(JNIEnv *env, const std::string &message) {
	env->ThrowNew(Class_java_lang_RuntimeException->j(), message.c_str());
}

/*
void AndroidUtil::throwBookReadingException(const std::string &resourceId, const ZLFile &file) {
	JNIEnv *env = getEnv();
	jclass cls = env->FindClass("org/geometerplus/fbreader/bookmodel/BookReadingException");
	env->CallStaticVoidMethod(
		cls,
		StaticMethod_BookReadingException_throwForFile,
		AndroidUtil::createJavaString(env, resourceId),
		AndroidUtil::createJavaFile(env, file.path())
	);
	// TODO: possible memory leak
	// TODO: clear cls & ZLFile object references
}
*/
