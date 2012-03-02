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

#include <jni.h>

#include <AndroidUtil.h>

#include "fbreader/src/formats/FormatPlugin.h"
#include "fbreader/src/library/Author.h"
#include "fbreader/src/library/Book.h"
#include "fbreader/src/library/Tag.h"

static shared_ptr<FormatPlugin> findCppPlugin(JNIEnv *env, jobject base) {
	jstring fileTypeJava = (jstring)env->CallObjectMethod(base, AndroidUtil::MID_NativeFormatPlugin_supportedFileType);
	std::string fileTypeCpp;
	AndroidUtil::extractJavaString(env, fileTypeJava, fileTypeCpp);
	shared_ptr<FormatPlugin> plugin = PluginCollection::Instance().pluginByType(fileTypeCpp);
	if (plugin.isNull()) {
		AndroidUtil::throwRuntimeException(env, "Native FormatPlugin instance is NULL for type " + fileTypeCpp);
	}
	return plugin;
}

static void fillMetaInfo(JNIEnv* env, jobject javaBook, Book &book) {
	jstring javaString;

	javaString = AndroidUtil::createJavaString(env, book.title());
	env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setTitle, javaString);
	env->DeleteLocalRef(javaString);

	javaString = AndroidUtil::createJavaString(env, book.language());
	if (javaString != 0) {
		env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setLanguage, javaString);
		env->DeleteLocalRef(javaString);
	}

	javaString = AndroidUtil::createJavaString(env, book.encoding());
	if (javaString != 0) {
		env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setEncoding, javaString);
		env->DeleteLocalRef(javaString);
	}

	javaString = AndroidUtil::createJavaString(env, book.seriesTitle());
	if (javaString != 0) {
		env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setSeriesInfo, javaString, (jfloat)book.indexInSeries());
		env->DeleteLocalRef(javaString);
	}

	const AuthorList &authors = book.authors();
	for (size_t i = 0; i < authors.size(); ++i) {
		const Author &author = *authors[i];
		javaString = env->NewStringUTF(author.name().c_str());
		jstring key = env->NewStringUTF(author.sortKey().c_str());
		env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_addAuthor, javaString, key);
		env->DeleteLocalRef(key);
		env->DeleteLocalRef(javaString);
	}

	const TagList &tags = book.tags();
	for (size_t i = 0; i < tags.size(); ++i) {
		const Tag &tag = *tags[i];
		env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_addTag, tag.javaTag(env));
	}
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readMetaInfo(JNIEnv* env, jobject thiz, jobject javaBook) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(env, thiz);
	if (plugin.isNull()) {
		return JNI_FALSE;
	}

	return JNI_FALSE;

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	if (!plugin->readMetaInfo(*book)) {
		return JNI_FALSE;
	}

	fillMetaInfo(env, javaBook, *book);
	return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readLanguageAndEncoding(JNIEnv* env, jobject thiz, jobject javaBook) {
	return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readModel(JNIEnv* env, jobject thiz, jobject javaModel) {
	return JNI_FALSE;
}

extern "C"
JNIEXPORT jobject JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readCoverInternal(JNIEnv* env, jobject thiz, jobject file) {
	return 0;
}
