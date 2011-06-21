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

#include "CoversWriter.h"

#include <AndroidLog.h>
#include <AndroidUtil.h>

#include <ZLFile.h>
#include <ZLOutputStream.h>
#include <ZLStringUtil.h>

#include <ZLFileImage.h>

#include "../fbreader/src/library/Library.h"


shared_ptr<CoversWriter> CoversWriter::ourInstance;

CoversWriter &CoversWriter::Instance() {
	if (ourInstance.isNull()) {
		ourInstance = new CoversWriter();
	}
	return *ourInstance;
}

CoversWriter::CoversWriter() :
		myDirectoryName(Library::Instance().cacheDirectory()),
		myFileExtension("ncover"),
		myCoversCounter(0) {
}

void CoversWriter::resetCounter() {
	myCoversCounter = 0;
}

jobject CoversWriter::writeCover(const ZLImage &image) {
	if (image.isSingle()) {
		return writeSingleCover((const ZLSingleImage&)image);
	} else {
		return 0;
	}
}

std::string CoversWriter::makeFileName(size_t index) const {
	std::string fileName(myDirectoryName);
	fileName.append("/").append("image");
	ZLStringUtil::appendNumber(fileName, index);
	return fileName.append(".").append(myFileExtension);
}

jobject CoversWriter::writeSingleCover(const ZLSingleImage &image) {
	AndroidLog log;
	log.wf("FBREADER", "CoversWriter: start");

	JNIEnv *env = AndroidUtil::getEnv();

	jstring javaPath;
	jint javaOffset, javaSize;

	switch (image.kind()) {
		case ZLSingleImage::BASE64_ENCODED_IMAGE:
		case ZLSingleImage::REGULAR_IMAGE:
		{
			log.wf("FBREADER", "CoversWriter: loading image data...");
			const shared_ptr<std::string> data = image.stringData();
			if (data.isNull() || data->empty()) {
				log.wf("FBREADER", "CoversWriter: data is NULL; return");
				return 0;
			}
			std::string fileName(makeFileName(myCoversCounter++));

			log.wf("FBREADER", "CoversWriter: writing to: %s", fileName.c_str());
			ZLFile file(fileName);
			shared_ptr<ZLOutputStream> stream = file.outputStream();
			stream->open();
			stream->write(data->data(), data->length());
			stream->close();
			log.wf("FBREADER", "CoversWriter: written.");

			javaPath = env->NewStringUTF(fileName.c_str());
			javaOffset = 0;
			javaSize = data->length();
			break;
		}
		case ZLSingleImage::FILE_IMAGE:
		{
			log.wf("FBREADER", "CoversWriter: need to write nothing.");
			const ZLFileImage &fileImage = (const ZLFileImage&)image;
			javaPath = env->NewStringUTF(fileImage.file().path().c_str());
			javaOffset = fileImage.offset();
			javaSize = fileImage.size();
			break;
		}
		default:
			return 0;
	}

	jstring javaMime = env->NewStringUTF(image.mimeType().c_str());

	log.wf("FBREADER", "CoversWriter: create java object");
	jclass cls = env->FindClass(AndroidUtil::Class_NativeFormatPlugin);
	jobject res = env->CallStaticObjectMethod(cls, AndroidUtil::SMID_NativeFormatPlugin_createImage,
			javaMime, javaPath, javaOffset, javaSize);
	env->DeleteLocalRef(javaMime);
	env->DeleteLocalRef(javaPath);
	env->DeleteLocalRef(cls);
	log.wf("FBREADER", "CoversWriter: finish");
	return res;
}
