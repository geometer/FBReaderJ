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

#ifndef __JNIENVELOPE_H__
#define __JNIENVELOPE_H__

#include <jni.h>

#include <string>

class Method {

public:
	Method(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature);
	virtual ~Method();

protected:
	JNIEnv *myEnv;
	jmethodID myId;
};

class VoidMethod : public Method {

public:
	VoidMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature);
	void call(jobject base, ...);
};

class IntMethod : public Method {

public:
	IntMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature);
	jint call(jobject base, ...);
};

class LongMethod : public Method {

public:
	LongMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature);
	jlong call(jobject base, ...);
};

class BooleanMethod : public Method {

public:
	BooleanMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature);
	jboolean call(jobject base, ...);
};

#endif /* __JNIENVELOPE_H__ */
