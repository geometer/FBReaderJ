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

#include "JavaInputStream.h"

#include <AndroidUtil.h>
#include <JniEnvelope.h>

JavaInputStream::JavaInputStream(const std::string &name, shared_ptr<FileEncryptionInfo> encryptionInfo) : myName(name), myEncryptionInfo(encryptionInfo), myNeedRepositionToStart(false), myMarkSupported(false) {
	myJavaFile = 0;

	myJavaInputStream = 0;
	myOffset = 0;

	myJavaBuffer = 0;
	myJavaBufferSize = 0;
}

JavaInputStream::~JavaInputStream() {
	JNIEnv *env = AndroidUtil::getEnv();
	if (myJavaInputStream != 0) {
		closeStream(env);
	}
	env->DeleteGlobalRef(myJavaFile);
	env->DeleteGlobalRef(myJavaBuffer);
}


void JavaInputStream::initStream(JNIEnv *env) {
	if (myJavaFile == 0) {
		jobject javaFile = AndroidUtil::createJavaFile(env, myName);
		if (javaFile == 0) {
			return;
		}
		myJavaFile = env->NewGlobalRef(javaFile);
		env->DeleteLocalRef(javaFile);
	}

	jobject stream;
	if (myEncryptionInfo.isNull()) {
		stream = AndroidUtil::Method_ZLFile_getInputStream->call(myJavaFile);
	} else {
		stream = 0;
	}

	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		return;
	}

	if (stream == 0) {
		return;
	}

	myJavaInputStream = env->NewGlobalRef(stream);
	myOffset = 0;
	myMarkSupported = AndroidUtil::Method_java_io_InputStream_markSupported->call(stream);
	if (myMarkSupported) {
		AndroidUtil::Method_java_io_InputStream_mark->call(stream, sizeOfOpened());
	}
	env->DeleteLocalRef(stream);
}

void JavaInputStream::closeStream(JNIEnv *env) {
	AndroidUtil::Method_java_io_InputStream_close->call(myJavaInputStream);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
	}
	env->DeleteGlobalRef(myJavaInputStream);
	myJavaInputStream = 0;
	myOffset = 0;
}

void JavaInputStream::rewind(JNIEnv *env) {
	if (myOffset > 0) {
		if (myMarkSupported) {
			AndroidUtil::Method_java_io_InputStream_reset->call(myJavaInputStream);
			AndroidUtil::Method_java_io_InputStream_mark->call(myJavaInputStream, sizeOfOpened());
			myOffset = 0;
		} else {
			closeStream(env);
			initStream(env);
		}
	}
}


void JavaInputStream::ensureBufferCapacity(JNIEnv *env, std::size_t maxSize) {
	if (myJavaBuffer != 0 && myJavaBufferSize >= maxSize) {
		return;
	}
	env->DeleteGlobalRef(myJavaBuffer);
	jbyteArray array = env->NewByteArray(maxSize);
	myJavaBuffer = (jbyteArray)env->NewGlobalRef(array);
	env->DeleteLocalRef(array);
	myJavaBufferSize = maxSize;
}

std::size_t JavaInputStream::readToBuffer(JNIEnv *env, char *buffer, std::size_t maxSize) {
	ensureBufferCapacity(env, maxSize);

	jint result =
		AndroidUtil::Method_java_io_InputStream_read->call(myJavaInputStream, myJavaBuffer, (jint)0, (jint)maxSize);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		return 0;
	}
	if (result > 0) {
		std::size_t bytesRead = (std::size_t)result;
		myOffset += bytesRead;

		jbyte *data = env->GetByteArrayElements(myJavaBuffer, 0);
		std::memcpy(buffer, data, bytesRead);
		env->ReleaseByteArrayElements(myJavaBuffer, data, JNI_ABORT);

		return bytesRead;
	}
	return 0;
}

std::size_t JavaInputStream::skip(JNIEnv *env, std::size_t offset) {
	std::size_t result =
		(std::size_t)AndroidUtil::Method_java_io_InputStream_skip->call(myJavaInputStream, (jlong)offset);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		return 0;
	}
	myOffset += result;
	return result;
}


bool JavaInputStream::open() {
	if (myJavaInputStream == 0) {
		JNIEnv *env = AndroidUtil::getEnv();
		initStream(env);
	} else {
		myNeedRepositionToStart = true;
	}
	return myJavaInputStream != 0;
}

std::size_t JavaInputStream::read(char *buffer, std::size_t maxSize) {
	JNIEnv *env = AndroidUtil::getEnv();
	if (myNeedRepositionToStart) {
		rewind(env);
		myNeedRepositionToStart = false;
	}
	if (buffer != 0) {
		return readToBuffer(env, buffer, maxSize);
	} else {
		return skip(env, maxSize);
	}
}

void JavaInputStream::close() {
}

std::size_t JavaInputStream::sizeOfOpened() {
	if (myJavaInputStream == 0 || myJavaFile == 0) {
		return 0;
	}
	return (std::size_t)AndroidUtil::Method_ZLFile_size->call(myJavaFile);
}

void JavaInputStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	if (offset < 0) {
		return;
	}
	JNIEnv *env = AndroidUtil::getEnv();
	if (myNeedRepositionToStart || offset < (int)myOffset) {
		rewind(env);
		myNeedRepositionToStart = false;
	}
	if (offset > (int)myOffset) {
		skip(env, offset - myOffset);
	}
}

std::size_t JavaInputStream::offset() const {
	return myNeedRepositionToStart ? 0 : myOffset;
}
