/*
 * Copyright (C) 2011-2014 Geometer Plus <contact@geometerplus.com>
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

#include <AndroidUtil.h>
#include <JniEnvelope.h>
#include <ZLFileImage.h>
#include <FileEncryptionInfo.h>

#include "fbreader/src/bookmodel/BookModel.h"
#include "fbreader/src/formats/FormatPlugin.h"
#include "fbreader/src/library/Library.h"
#include "fbreader/src/library/Author.h"
#include "fbreader/src/library/Book.h"
#include "fbreader/src/library/Tag.h"
#include "fbreader/src/library/UID.h"

static shared_ptr<FormatPlugin> findCppPlugin(jobject base) {
	const std::string fileType = AndroidUtil::Method_NativeFormatPlugin_supportedFileType->callForCppString(base);
	shared_ptr<FormatPlugin> plugin = PluginCollection::Instance().pluginByType(fileType);
	if (plugin.isNull()) {
		AndroidUtil::throwRuntimeException("Native FormatPlugin instance is NULL for type " + fileType);
	}
	return plugin;
}

static void fillUids(JNIEnv* env, jobject javaBook, Book &book) {
	const UIDList &uids = book.uids();
	for (UIDList::const_iterator it = uids.begin(); it != uids.end(); ++it) {
		jstring type = AndroidUtil::createJavaString(env, (*it)->Type);
		jstring id = AndroidUtil::createJavaString(env, (*it)->Id);
		AndroidUtil::Method_Book_addUid->call(javaBook, type, id);
		env->DeleteLocalRef(id);
		env->DeleteLocalRef(type);
	}
}

static void fillMetaInfo(JNIEnv* env, jobject javaBook, Book &book) {
	jstring javaString;

	javaString = AndroidUtil::createJavaString(env, book.title());
	AndroidUtil::Method_Book_setTitle->call(javaBook, javaString);
	env->DeleteLocalRef(javaString);

	javaString = AndroidUtil::createJavaString(env, book.language());
	if (javaString != 0) {
		AndroidUtil::Method_Book_setLanguage->call(javaBook, javaString);
		env->DeleteLocalRef(javaString);
	}

	javaString = AndroidUtil::createJavaString(env, book.encoding());
	if (javaString != 0) {
		AndroidUtil::Method_Book_setEncoding->call(javaBook, javaString);
		env->DeleteLocalRef(javaString);
	}

	javaString = AndroidUtil::createJavaString(env, book.seriesTitle());
	if (javaString != 0) {
		jstring indexString = AndroidUtil::createJavaString(env, book.indexInSeries());
		AndroidUtil::Method_Book_setSeriesInfo->call(javaBook, javaString, indexString);
		if (indexString != 0) {
			env->DeleteLocalRef(indexString);
		}
		env->DeleteLocalRef(javaString);
	}

	const AuthorList &authors = book.authors();
	for (std::size_t i = 0; i < authors.size(); ++i) {
		const Author &author = *authors[i];
		javaString = env->NewStringUTF(author.name().c_str());
		jstring key = env->NewStringUTF(author.sortKey().c_str());
		AndroidUtil::Method_Book_addAuthor->call(javaBook, javaString, key);
		env->DeleteLocalRef(key);
		env->DeleteLocalRef(javaString);
	}

	const TagList &tags = book.tags();
	for (std::size_t i = 0; i < tags.size(); ++i) {
		const Tag &tag = *tags[i];
		AndroidUtil::Method_Book_addTag->call(javaBook, tag.javaTag(env));
	}

	fillUids(env, javaBook, book);
}

static void fillLanguageAndEncoding(JNIEnv* env, jobject javaBook, Book &book) {
	jstring javaString;

	javaString = AndroidUtil::createJavaString(env, book.language());
	if (javaString != 0) {
		AndroidUtil::Method_Book_setLanguage->call(javaBook, javaString);
		env->DeleteLocalRef(javaString);
	}

	javaString = AndroidUtil::createJavaString(env, book.encoding());
	if (javaString != 0) {
		AndroidUtil::Method_Book_setEncoding->call(javaBook, javaString);
		env->DeleteLocalRef(javaString);
	}
}

extern "C"
JNIEXPORT jint JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readMetaInfoNative(JNIEnv* env, jobject thiz, jobject javaBook) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return 1;
	}

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);

	if (!plugin->readMetaInfo(*book)) {
		return 2;
	}

	fillMetaInfo(env, javaBook, *book);
	return 0;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readEncryptionMethod(JNIEnv* env, jobject thiz, jobject javaBook) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return AndroidUtil::createJavaString(env, EncryptionMethod::UNSUPPORTED);
	}

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	return AndroidUtil::createJavaString(env, plugin->readEncryptionMethod(*book));
}

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readUidsNative(JNIEnv* env, jobject thiz, jobject javaBook) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return;
	}

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);

	plugin->readUids(*book);
	fillUids(env, javaBook, *book);
}

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_detectLanguageAndEncodingNative(JNIEnv* env, jobject thiz, jobject javaBook) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return;
	}

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	if (!plugin->readLanguageAndEncoding(*book)) {
		return;
	}

	fillLanguageAndEncoding(env, javaBook, *book);
}

static bool initInternalHyperlinks(JNIEnv *env, jobject javaModel, BookModel &model) {
	ZLCachedMemoryAllocator allocator(131072, Library::Instance().cacheDirectory(), "nlinks");

	ZLUnicodeUtil::Ucs2String ucs2id;
	ZLUnicodeUtil::Ucs2String ucs2modelId;

	const std::map<std::string,BookModel::Label> &links = model.internalHyperlinks();
	std::map<std::string,BookModel::Label>::const_iterator it = links.begin();
	for (; it != links.end(); ++it) {
		const std::string &id = it->first;
		const BookModel::Label &label = it->second;
		if (label.Model.isNull()) {
			continue;
		}
		ZLUnicodeUtil::utf8ToUcs2(ucs2id, id);
		ZLUnicodeUtil::utf8ToUcs2(ucs2modelId, label.Model->id());
		const std::size_t idLen = ucs2id.size() * 2;
		const std::size_t modelIdLen = ucs2modelId.size() * 2;

		char *ptr = allocator.allocate(idLen + modelIdLen + 8);
		ZLCachedMemoryAllocator::writeUInt16(ptr, ucs2id.size());
		ptr += 2;
		std::memcpy(ptr, &ucs2id.front(), idLen);
		ptr += idLen;
		ZLCachedMemoryAllocator::writeUInt16(ptr, ucs2modelId.size());
		ptr += 2;
		std::memcpy(ptr, &ucs2modelId.front(), modelIdLen);
		ptr += modelIdLen;
		ZLCachedMemoryAllocator::writeUInt32(ptr, label.ParagraphNumber);
	}
	allocator.flush();

	jstring linksDirectoryName = env->NewStringUTF(allocator.directoryName().c_str());
	jstring linksFileExtension = env->NewStringUTF(allocator.fileExtension().c_str());
	jint linksBlocksNumber = allocator.blocksNumber();
	AndroidUtil::Method_NativeBookModel_initInternalHyperlinks->call(javaModel, linksDirectoryName, linksFileExtension, linksBlocksNumber);
	env->DeleteLocalRef(linksDirectoryName);
	env->DeleteLocalRef(linksFileExtension);
	return !env->ExceptionCheck();
}

static jobject createTextModel(JNIEnv *env, jobject javaModel, ZLTextModel &model) {
	env->PushLocalFrame(16);

	jstring id = AndroidUtil::createJavaString(env, model.id());
	jstring language = AndroidUtil::createJavaString(env, model.language());
	jint paragraphsNumber = model.paragraphsNumber();

	const std::size_t arraysSize = model.startEntryIndices().size();
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

	jobject textModel = AndroidUtil::Method_NativeBookModel_createTextModel->call(
		javaModel,
		id, language,
		paragraphsNumber, entryIndices, entryOffsets,
		paragraphLenghts, textSizes, paragraphKinds,
		directoryName, fileExtension, blocksNumber
	);

	if (env->ExceptionCheck()) {
		textModel = 0;
	}
	return env->PopLocalFrame(textModel);
}

static void initTOC(JNIEnv *env, jobject javaModel, const ContentsTree &tree) {
	const std::vector<shared_ptr<ContentsTree> > &children = tree.children();
	for (std::vector<shared_ptr<ContentsTree> >::const_iterator it = children.begin(); it != children.end(); ++it) {
		const ContentsTree &child = **it;
		jstring text = AndroidUtil::createJavaString(env, child.text());
		const int ref = child.reference();
		AndroidUtil::Method_NativeBookModel_addTOCItem->call(javaModel, text, ref);
		env->DeleteLocalRef(text);

		initTOC(env, javaModel, child);

		AndroidUtil::Method_NativeBookModel_leaveTOCItem->call(javaModel);
	}
}

extern "C"
JNIEXPORT jint JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readModelNative(JNIEnv* env, jobject thiz, jobject javaModel) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return 1;
	}

	jobject javaBook = AndroidUtil::Field_NativeBookModel_Book->value(javaModel);

	shared_ptr<Book> book = Book::loadFromJavaBook(env, javaBook);
	shared_ptr<BookModel> model = new BookModel(book, javaModel);
	if (!plugin->readModel(*model)) {
		return 2;
	}
	if (!model->flush()) {
		AndroidUtil::throwCachedCharStorageException("Cannot write file from native code");
		return 3;
	}

	if (!initInternalHyperlinks(env, javaModel, *model)) {
		return 4;
	}

	initTOC(env, javaModel, *model->contentsTree());

	shared_ptr<ZLTextModel> textModel = model->bookTextModel();
	jobject javaTextModel = createTextModel(env, javaModel, *textModel);
	if (javaTextModel == 0) {
		return 5;
	}
	AndroidUtil::Method_NativeBookModel_setBookTextModel->call(javaModel, javaTextModel);
	if (env->ExceptionCheck()) {
		return 6;
	}
	env->DeleteLocalRef(javaTextModel);

	const std::map<std::string,shared_ptr<ZLTextModel> > &footnotes = model->footnotes();
	std::map<std::string,shared_ptr<ZLTextModel> >::const_iterator it = footnotes.begin();
	for (; it != footnotes.end(); ++it) {
		jobject javaFootnoteModel = createTextModel(env, javaModel, *it->second);
		if (javaFootnoteModel == 0) {
			return 7;
		}
		AndroidUtil::Method_NativeBookModel_setFootnoteModel->call(javaModel, javaFootnoteModel);
		if (env->ExceptionCheck()) {
			return 8;
		}
		env->DeleteLocalRef(javaFootnoteModel);
	}
	return 0;
}

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readCoverInternal(JNIEnv* env, jobject thiz, jobject file, jobjectArray box) {
	shared_ptr<FormatPlugin> plugin = findCppPlugin(thiz);
	if (plugin.isNull()) {
		return;
	}

	const std::string path = AndroidUtil::Method_ZLFile_getPath->callForCppString(file);

	shared_ptr<const ZLImage> image = plugin->coverImage(ZLFile(path));
	if (!image.isNull()) {
		jobject javaImage = AndroidUtil::createJavaImage(env, (const ZLFileImage&)*image);
		env->SetObjectArrayElement(box, 0, javaImage);
		env->DeleteLocalRef(javaImage);
	}
}
