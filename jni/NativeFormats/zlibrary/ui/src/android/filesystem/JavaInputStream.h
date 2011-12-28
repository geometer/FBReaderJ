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

#ifndef __JAVAINPUTSTREAM_H__
#define __JAVAINPUTSTREAM_H__

#include <jni.h>

#include <ZLInputStream.h>

class JavaInputStream : public ZLInputStream {

public:
	JavaInputStream(const std::string &name);
	~JavaInputStream();
	bool open();
	size_t read(char *buffer, size_t maxSize);
	void close();

	void seek(int offset, bool absoluteOffset);
	size_t offset() const;
	size_t sizeOfOpened();

private:
	void initStream(JNIEnv *env);
	void closeStream(JNIEnv *env);
	void rewind(JNIEnv *env);
	void ensureBufferCapacity(JNIEnv *env, size_t maxSize);
	size_t readToBuffer(JNIEnv *env, char *buffer, size_t maxSize);
	size_t skip(JNIEnv *env, size_t offset);

private:
	std::string myName;
	bool myNeedRepositionToStart;

	jobject myJavaFile;

	jobject myJavaInputStream;
	size_t myOffset;

	jbyteArray myJavaBuffer;
	size_t myJavaBufferSize;
};

#endif /* __JAVAINPUTSTREAM_H__ */
