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

#include <jni.h>

// This code is temporary: it makes eclipse not complain
#ifndef _JNI_H
#define JNIIMPORT
#define JNIEXPORT
#define JNICALL
#endif /* _JNI_H */


#include <AndroidLog.h>
#include <AndroidUtil.h>
#include <CoversWriter.h>

#include <ZLFile.h>
#include <ZLTextModel.h>

#include <ZLImageMapWriter.h>

#include "fbreader/src/bookmodel/BookModel.h"
#include "fbreader/src/formats/FormatPlugin.h"
#include "fbreader/src/library/Library.h"
#include "fbreader/src/library/Book.h"
#include "fbreader/src/library/Author.h"
#include "fbreader/src/library/Tag.h"


static inline FormatPlugin *extractPointer(JNIEnv *env, jobject base) {
	jlong ptr = env->GetLongField(base, AndroidUtil::FID_NativeFormatPlugin_NativePointer);
	if (ptr == 0) {
		jclass cls = env->FindClass("org/geometerplus/fbreader/formats/NativeFormatPluginException");
		env->ThrowNew(cls, "Native FormatPlugin instance is NULL.");
	}
	return (FormatPlugin *)ptr;
}


extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_acceptsFile(JNIEnv* env, jobject thiz, jobject file) {
	FormatPlugin *plugin = extractPointer(env, thiz);
	if (plugin == 0) {
		return JNI_FALSE;
	}
	std::string path;
	jstring javaPath = (jstring) env->CallObjectMethod(file, AndroidUtil::MID_ZLFile_getPath);
	AndroidUtil::extractJavaString(env, javaPath, path);
	env->DeleteLocalRef(javaPath);
	return plugin->acceptsFile(ZLFile(path)) ? JNI_TRUE : JNI_FALSE;
}


void fillMetaInfo(JNIEnv* env, jobject javaBook, Book &book) {
	jstring javaString;

	javaString = AndroidUtil::createJavaString(env, book.title());
	env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setTitle, javaString);
	env->DeleteLocalRef(javaString);

	javaString = AndroidUtil::createJavaString(env, book.language());
	env->CallVoidMethod(javaBook, AndroidUtil::MID_Book_setLanguage, javaString);
	env->DeleteLocalRef(javaString);

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
	FormatPlugin *plugin = extractPointer(env, thiz);
	if (plugin == 0) {
		return JNI_FALSE;
	}

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	if (!plugin->readMetaInfo(*book)) {
		return JNI_FALSE;
	}

	fillMetaInfo(env, javaBook, *book);
	return JNI_TRUE;
}

static void initBookModel(JNIEnv *env, jobject javaModel, BookModel &model) {
	shared_ptr<ZLImageMapWriter> imageMapWriter = model.imageMapWriter();

	env->PushLocalFrame(16);

	jobjectArray ids = AndroidUtil::createStringArray(env, imageMapWriter->identifiers());
	jintArray indices = AndroidUtil::createIntArray(env, imageMapWriter->indices());
	jintArray offsets = AndroidUtil::createIntArray(env, imageMapWriter->offsets());
	jstring imageDirectoryName = env->NewStringUTF(imageMapWriter->allocator().directoryName().c_str());
	jstring imageFileExtension = env->NewStringUTF(imageMapWriter->allocator().fileExtension().c_str());
	jint imageBlocksNumber = imageMapWriter->allocator().blocksNumber();
	env->CallVoidMethod(javaModel, AndroidUtil::MID_NativeBookModel_initBookModel,
			ids, indices, offsets, imageDirectoryName, imageFileExtension, imageBlocksNumber);

	env->PopLocalFrame(0);
}

static jobject createTextModel(JNIEnv *env, jobject javaModel, ZLTextModel &model) {
	env->PushLocalFrame(16);

	jstring id = AndroidUtil::createJavaString(env, model.id());
	jstring language = AndroidUtil::createJavaString(env, model.language());
	jint paragraphsNumber = model.paragraphsNumber();

	const size_t arraysSize = model.startEntryIndices().size();
	jintArray entryIndices = env->NewIntArray(arraysSize);
	jintArray entryOffsets = env->NewIntArray(arraysSize);
	jintArray paragraphLenghts = env->NewIntArray(arraysSize);
	jintArray textSizes = env->NewIntArray(arraysSize);
	jbyteArray paragraphKinds = env->NewByteArray(arraysSize);
	env->SetIntArrayRegion(entryIndices, 0, arraysSize, &model.startEntryIndices().front());
	env->SetIntArrayRegion(entryOffsets, 0, arraysSize, &model.startEntryOffsets().front());
	env->SetIntArrayRegion(paragraphLenghts, 0, arraysSize, &model.paragraphLengths().front());
	env->SetIntArrayRegion(textSizes, 0, arraysSize, &model.textSizes().front());
	env->SetByteArrayRegion(paragraphKinds, 0, arraysSize, &model.paragraphKinds().front());

	jstring directoryName = env->NewStringUTF(model.allocator().directoryName().c_str());
	jstring fileExtension = env->NewStringUTF(model.allocator().fileExtension().c_str());
	jint blocksNumber = (jint) model.allocator().blocksNumber();

	jobject textModel = env->CallObjectMethod(javaModel, AndroidUtil::MID_NativeBookModel_createTextModel,
			id, language,
			paragraphsNumber, entryIndices, entryOffsets,
			paragraphLenghts, textSizes, paragraphKinds,
			directoryName, fileExtension, blocksNumber);

	return env->PopLocalFrame(textModel);
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readModel(JNIEnv* env, jobject thiz, jobject javaModel) {
	FormatPlugin *plugin = extractPointer(env, thiz);
	if (plugin == 0) {
		return JNI_FALSE;
	}

	jobject javaBook = env->GetObjectField(javaModel, AndroidUtil::FID_BookModel_Book);

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	shared_ptr<BookModel> model = new BookModel(book);
	if (!plugin->readModel(*model)) {
		return JNI_FALSE;
	}
	model->flush();

	initBookModel(env, javaModel, *model);

	shared_ptr<ZLTextModel> textModel = model->bookTextModel();
	jobject javaTextModel = createTextModel(env, javaModel, *textModel);

	env->CallVoidMethod(javaModel, AndroidUtil::MID_NativeBookModel_setBookTextModel, javaTextModel);
	return JNI_TRUE;
}

extern "C"
JNIEXPORT jobject JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readCoverInternal(JNIEnv* env, jobject thiz, jobject file) {
	AndroidLog log;
	log.wf("FBREADER", "LOAD COVER...");

	FormatPlugin *plugin = extractPointer(env, thiz);
	if (plugin == 0) {
		return 0;
	}
	std::string path;
	jstring javaPath = (jstring) env->CallObjectMethod(file, AndroidUtil::MID_ZLFile_getPath);
	AndroidUtil::extractJavaString(env, javaPath, path);
	env->DeleteLocalRef(javaPath);

	log.wf("FBREADER", "... FOR PATH: %s", path.c_str());

	shared_ptr<ZLImage> cover = plugin->coverImage(ZLFile(path));
	if (cover.isNull()) {
		log.wf("FBREADER", "... cover is NULL; return NULL");
		return 0;
	}
	log.wf("FBREADER", "... cover is ready");

	jobject res = CoversWriter::Instance().writeCover(*cover);
	log.wf("FBREADER", "... cover is converted to java; return");
	return res;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readAnnotation(JNIEnv* env, jobject thiz, jobject file) {
	FormatPlugin *plugin = extractPointer(env, thiz);
	if (plugin == 0) {
		return 0;
	}
	return 0;
}
