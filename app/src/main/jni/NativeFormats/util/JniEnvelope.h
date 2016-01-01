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

#ifndef __JNIENVELOPE_H__
#define __JNIENVELOPE_H__

#include <jni.h>

#include <string>

class JavaType {

protected:
	JavaType();
	virtual ~JavaType();

public:
	virtual std::string code() const = 0;

private:
	JavaType(const JavaType&);
	const JavaType &operator = (const JavaType&);
};

class JavaPrimitiveType : public JavaType {

public:
	static const JavaPrimitiveType Void;
	static const JavaPrimitiveType Int;
	static const JavaPrimitiveType Long;
	static const JavaPrimitiveType Boolean;

//protected:
public:
	JavaPrimitiveType(const std::string &code);

public:
	std::string code() const;

private:
	const std::string myCode;
};

class JavaArray : public JavaType {

public:
	JavaArray(const JavaType &base);
	std::string code() const;

private:
	const JavaType &myBase;
};

class JavaClass : public JavaType {

public:
	JavaClass(const std::string &name);
	~JavaClass();
	jclass j() const;
	std::string code() const;

private:
	const std::string myName;
	mutable jclass myClass;

friend class Member;
};

class Member {

protected:
	Member(const JavaClass &cls);
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
	Constructor(const JavaClass &cls, const std::string &parameters);
	jobject call(...);

private:
	jmethodID myId;
};

class Field : public Member {

public:
	Field(const JavaClass &cls, const std::string &name, const JavaType &type);
	virtual ~Field();

protected:
	const std::string myName;
	jfieldID myId;
};

class Method : public Member {

public:
	Method(const JavaClass &cls, const std::string &name, const JavaType &type, const std::string &parameters);
	virtual ~Method();

protected:
	const std::string myName;
	jmethodID myId;
};

class StaticMethod : public Member {

public:
	StaticMethod(const JavaClass &cls, const std::string &name, const JavaType &returnType, const std::string &parameters);
	virtual ~StaticMethod();

protected:
	const std::string myName;
	jmethodID myId;
};

class ObjectField : public Field {

public:
	ObjectField(const JavaClass &cls, const std::string &name, const JavaType &type);
	jobject value(jobject obj) const;
};

class VoidMethod : public Method {

public:
	VoidMethod(const JavaClass &cls, const std::string &name, const std::string &parameters);
	void call(jobject base, ...);
};

class IntMethod : public Method {

public:
	IntMethod(const JavaClass &cls, const std::string &name, const std::string &parameters);
	jint call(jobject base, ...);
};

class LongMethod : public Method {

public:
	LongMethod(const JavaClass &cls, const std::string &name, const std::string &parameters);
	jlong call(jobject base, ...);
};

class BooleanMethod : public Method {

public:
	BooleanMethod(const JavaClass &cls, const std::string &name, const std::string &parameters);
	jboolean call(jobject base, ...);
};

class StringMethod : public Method {

public:
	StringMethod(const JavaClass &cls, const std::string &name, const std::string &parameters);
	jstring callForJavaString(jobject base, ...);
	std::string callForCppString(jobject base, ...);
};

class ObjectMethod : public Method {

public:
	ObjectMethod(const JavaClass &cls, const std::string &name, const JavaClass &returnType, const std::string &parameters);
	jobject call(jobject base, ...);
};

class ObjectArrayMethod : public Method {

public:
	ObjectArrayMethod(const JavaClass &cls, const std::string &name, const JavaArray &returnType, const std::string &parameters);
	jobjectArray call(jobject base, ...);
};

class StaticObjectMethod : public StaticMethod {

public:
	StaticObjectMethod(const JavaClass &cls, const std::string &name, const JavaClass &returnType, const std::string &parameters);
	jobject call(...);
};

inline jclass Member::jClass() const { return myClass.j(); }

inline JavaPrimitiveType::JavaPrimitiveType(const std::string &code) : myCode(code) {}
inline std::string JavaPrimitiveType::code() const { return myCode; }

#endif /* __JNIENVELOPE_H__ */
