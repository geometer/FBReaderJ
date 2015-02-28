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

#ifndef __JAVAINPUTSTREAM_H__
#define __JAVAINPUTSTREAM_H__

#include <jni.h>

#include <shared_ptr.h>
#include <ZLInputStream.h>
#include <FileEncryptionInfo.h>

class JavaInputStream : public ZLInputStream {

public:
	JavaInputStream(const std::string &name, shared_ptr<FileEncryptionInfo> encryptionInfo = 0);
	~JavaInputStream();
	bool open();
	std::size_t read(char *buffer, std::size_t maxSize);
	void close();

	void seek(int offset, bool absoluteOffset);
	std::size_t offset() const;
	std::size_t sizeOfOpened();

private:
	void initStream(JNIEnv *env);
	void closeStream(JNIEnv *env);
	void rewind(JNIEnv *env);
	void ensureBufferCapacity(JNIEnv *env, std::size_t maxSize);
	std::size_t readToBuffer(JNIEnv *env, char *buffer, std::size_t maxSize);
	std::size_t skip(JNIEnv *env, std::size_t offset);

private:
	const std::string myName;
	const shared_ptr<FileEncryptionInfo> myEncryptionInfo;
	bool myNeedRepositionToStart;
	bool myMarkSupported;

	jobject myJavaFile;

	jobject myJavaInputStream;
	std::size_t myOffset;

	jbyteArray myJavaBuffer;
	std::size_t myJavaBufferSize;
};

#endif /* __JAVAINPUTSTREAM_H__ */
