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

class JavaClass {

public:
	JavaClass(JNIEnv *env, const std::string &name);
	~JavaClass();
	jclass j() const;

private:
	JavaClass(const JavaClass&);
	const JavaClass &operator = (const JavaClass&);

private:
	const std::string myName;
	JNIEnv *myEnv;
	jclass myClass;

friend class Member;
friend class Method;
};

class Member {

protected:
	Member(const JavaClass &cls);
	JNIEnv &env() const;
	jclass jClass() const;

public:
	virtual ~Member();

private:
	Member(const Member&);
	const Member &operator = (const Member&);

private:
	const JavaClass &myClass;
};

class Constructor : public Member {

public:
	Constructor(const JavaClass &cls, const std::string &signature);
	jobject call(...);

private:
	jmethodID myId;
};

class Method : public Member {

public:
	Method(const JavaClass &cls, const std::string &name, const std::string &signature);
	virtual ~Method();

protected:
	const std::string myName;
	jmethodID myId;
};

class StaticMethod : public Member {

public:
	StaticMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	virtual ~StaticMethod();

protected:
	const std::string myName;
	jmethodID myId;
};

class VoidMethod : public Method {

public:
	VoidMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	void call(jobject base, ...);
};

class IntMethod : public Method {

public:
	IntMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	jint call(jobject base, ...);
};

class LongMethod : public Method {

public:
	LongMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	jlong call(jobject base, ...);
};

class BooleanMethod : public Method {

public:
	BooleanMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	jboolean call(jobject base, ...);
};

class StringMethod : public Method {

public:
	StringMethod(const JavaClass &cls, const std::string &name, const std::string &signature);
	jstring call(jobject base, ...);
};

class ObjectMethod : public Method {

public:
	ObjectMethod(const JavaClass &cls, const std::string &name, const std::string &returnType, const std::string &signature);
	jobject call(jobject base, ...);
};

class StaticObjectMethod : public StaticMethod {

public:
	StaticObjectMethod(const JavaClass &cls, const std::string &name, const std::string &returnType, const std::string &signature);
	jobject call(...);
};

inline jclass JavaClass::j() const { return myClass; }

inline JNIEnv &Member::env() const { return *myClass.myEnv; }
inline jclass Member::jClass() const { return myClass.myClass; }

#endif /* __JNIENVELOPE_H__ */
