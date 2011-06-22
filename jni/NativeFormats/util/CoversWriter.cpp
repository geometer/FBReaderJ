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
		myFileExtension("ncover"),
		myCoversCounter(0) {
}

void CoversWriter::resetCounter() {
	myCoversCounter = 0;
	myImageCache.clear();
}

jobject CoversWriter::writeCover(const std::string &bookPath, const ZLImage &image) {
	if (image.isSingle()) {
		return writeSingleCover(bookPath, (const ZLSingleImage&)image);
	} else {
		return 0;
	}
}

std::string CoversWriter::makeFileName(size_t index) const {
	std::string fileName(Library::Instance().cacheDirectory());
	fileName.append("/").append("image");
	ZLStringUtil::appendNumber(fileName, index);
	return fileName.append(".").append(myFileExtension);
}

jobject CoversWriter::writeSingleCover(const std::string &bookPath, const ZLSingleImage &image) {
	AndroidLog log;
	log.wf("FBREADER", "CoversWriter: start");

	JNIEnv *env = AndroidUtil::getEnv();

	ImageData &data = myImageCache[bookPath];
	if ((data.Path.empty() || !ZLFile(data.Path).exists())
			&& !fillSingleImageData(data, image)) {
		return 0;
	}

	log.wf("FBREADER", "CoversWriter: create java object");
	jstring javaPath = env->NewStringUTF(data.Path.c_str());
	jstring javaMime = env->NewStringUTF(image.mimeType().c_str());
	jclass cls = env->FindClass(AndroidUtil::Class_NativeFormatPlugin);
	jobject res = env->CallStaticObjectMethod(cls, AndroidUtil::SMID_NativeFormatPlugin_createImage,
			javaMime, javaPath, (jint)data.Offset, (jint)data.Length);
	env->DeleteLocalRef(javaMime);
	env->DeleteLocalRef(javaPath);
	env->DeleteLocalRef(cls);
	log.wf("FBREADER", "CoversWriter: finish");
	return res;
}

bool CoversWriter::fillSingleImageData(ImageData &imageData, const ZLSingleImage &image) {
	AndroidLog log;
	log.wf("FBREADER", "CoversWriter: NO CACHE DATA...");
	switch (image.kind()) {
		case ZLSingleImage::BASE64_ENCODED_IMAGE:
		case ZLSingleImage::REGULAR_IMAGE:
		{
			log.wf("FBREADER", "CoversWriter: loading image data...");
			const shared_ptr<std::string> data = image.stringData();
			if (data.isNull() || data->empty()) {
				log.wf("FBREADER", "CoversWriter: data is NULL; return");
				return false;
			}
			std::string fileName(makeFileName(myCoversCounter++));

			log.wf("FBREADER", "CoversWriter: writing to: %s", fileName.c_str());
			ZLFile file(fileName);
			shared_ptr<ZLOutputStream> stream = file.outputStream();
			stream->open();
			stream->write(data->data(), data->length());
			stream->close();
			log.wf("FBREADER", "CoversWriter: written.");

			imageData.Path = fileName;
			imageData.Offset = 0;
			imageData.Length = data->length();
			break;
		}
		case ZLSingleImage::FILE_IMAGE:
		{
			log.wf("FBREADER", "CoversWriter: need to write nothing.");
			const ZLFileImage &fileImage = (const ZLFileImage&)image;
			imageData.Path = fileImage.file().path();
			imageData.Offset = fileImage.offset();
			imageData.Length = fileImage.size();
			break;
		}
		default:
			log.wf("FBREADER", "CoversWriter: unknown image; return");
			return false;
	}
	log.wf("FBREADER", "CoversWriter: CACHE FILLED");
	return true;
}
