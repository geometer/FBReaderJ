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

#include <ZLLogger.h>

#include "JniEnvelope.h"

static const std::string JNI_LOGGER_CLASS = "JniLog";

const JavaPrimitiveType JavaPrimitiveType::Void("V");
const JavaPrimitiveType JavaPrimitiveType::Int("I");
const JavaPrimitiveType JavaPrimitiveType::Long("J");
const JavaPrimitiveType JavaPrimitiveType::Boolean("Z");

JavaType::JavaType() {
}

JavaType::~JavaType() {
}

JavaArray::JavaArray(const JavaType &base) : myBase(base) {
}

std::string JavaArray::code() const {
	return "[" + myBase.code();
}

JavaClass::JavaClass(JNIEnv *env, const std::string &name) : myName(name), myEnv(env) {
	myClass = 0;
}

JavaClass::~JavaClass() {
	if (myClass != 0) {
		myEnv->DeleteGlobalRef(myClass);
	}
}

std::string JavaClass::code() const {
	return "L" + myName + ";";
}

jclass JavaClass::j() const {
	if (myClass == 0) {
		jclass ref = myEnv->FindClass(myName.c_str());
		myClass = (jclass)myEnv->NewGlobalRef(ref);
		myEnv->DeleteLocalRef(ref);
	}
	return myClass;
}

Member::Member(const JavaClass &cls) : myClass(cls) {
	//ZLLogger::Instance().registerClass(JNI_LOGGER_CLASS);
}

Member::~Member() {
}

Constructor::Constructor(const JavaClass &cls, const std::string &parameters) : Member(cls) {
	myId = env().GetMethodID(jClass(), "<init>", parameters.c_str());
}

jobject Constructor::call(...) {
	va_list lst;
	va_start(lst, this);
	jobject obj = env().NewObjectV(jClass(), myId, lst);
	va_end(lst);
	return obj;
}

Field::Field(const JavaClass &cls, const std::string &name, const JavaType &type) : Member(cls), myName(name) {
	myId = env().GetFieldID(jClass(), name.c_str(), type.code().c_str());
}

Field::~Field() {
}

Method::Method(const JavaClass &cls, const std::string &name, const JavaType &returnType, const std::string &parameters) : Member(cls), myName(name) {
	const std::string signature = parameters + returnType.code();
	myId = env().GetMethodID(jClass(), name.c_str(), signature.c_str());
}

Method::~Method() {
}

StaticMethod::StaticMethod(const JavaClass &cls, const std::string &name, const JavaType &returnType, const std::string &parameters) : Member(cls), myName(name) {
	const std::string signature = parameters + returnType.code();
	myId = env().GetStaticMethodID(jClass(), name.c_str(), signature.c_str());
}

StaticMethod::~StaticMethod() {
}

ObjectField::ObjectField(const JavaClass &cls, const std::string &name, const JavaType &type) : Field(cls, name, type) {
}

jobject ObjectField::value(jobject obj) const {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "getting value of ObjectField " + myName);
	jobject val = env().GetObjectField(obj, myId);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "got value of ObjectField " + myName);
	return val;
}

VoidMethod::VoidMethod(const JavaClass &cls, const std::string &name, const std::string &parameters) : Method(cls, name, JavaPrimitiveType::Void, parameters) {
}

void VoidMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling VoidMethod " + myName);
	va_list lst;
	va_start(lst, base);
	env().CallVoidMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished VoidMethod " + myName);
}

IntMethod::IntMethod(const JavaClass &cls, const std::string &name, const std::string &parameters) : Method(cls, name, JavaPrimitiveType::Int, parameters) {
}

jint IntMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling IntMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jint result = env().CallIntMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished IntMethod " + myName);
	return result;
}

LongMethod::LongMethod(const JavaClass &cls, const std::string &name, const std::string &parameters) : Method(cls, name, JavaPrimitiveType::Long, parameters) {
}

jlong LongMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling LongMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jlong result = env().CallLongMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished LongMethod " + myName);
	return result;
}

BooleanMethod::BooleanMethod(const JavaClass &cls, const std::string &name, const std::string &parameters) : Method(cls, name, JavaPrimitiveType::Boolean, parameters) {
}

jboolean BooleanMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling BooleanMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jboolean result = env().CallBooleanMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished BooleanMethod " + myName);
	return result;
}

static JavaPrimitiveType FakeString("Ljava/lang/String;");

StringMethod::StringMethod(const JavaClass &cls, const std::string &name, const std::string &parameters) : Method(cls, name, FakeString, parameters) {
}

jstring StringMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling StringMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jstring result = (jstring)env().CallObjectMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished StringMethod " + myName);
	return result;
}

ObjectMethod::ObjectMethod(const JavaClass &cls, const std::string &name, const JavaClass &returnType, const std::string &parameters) : Method(cls, name, returnType, parameters) {
}

jobject ObjectMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling ObjectMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jobject result = env().CallObjectMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished ObjectMethod " + myName);
	return result;
}

ObjectArrayMethod::ObjectArrayMethod(const JavaClass &cls, const std::string &name, const JavaArray &returnType, const std::string &parameters) : Method(cls, name, returnType, parameters) {
}

jobjectArray ObjectArrayMethod::call(jobject base, ...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling ObjectArrayMethod " + myName);
	va_list lst;
	va_start(lst, base);
	jobjectArray result = (jobjectArray)env().CallObjectMethodV(base, myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished ObjectArrayMethod " + myName);
	return result;
}

StaticObjectMethod::StaticObjectMethod(const JavaClass &cls, const std::string &name, const JavaClass &returnType, const std::string &parameters) : StaticMethod(cls, name, returnType, parameters) {
}

jobject StaticObjectMethod::call(...) {
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "calling StaticObjectMethod " + myName);
	va_list lst;
	va_start(lst, this);
	jobject result = env().CallStaticObjectMethodV(jClass(), myId, lst);
	va_end(lst);
	ZLLogger::Instance().println(JNI_LOGGER_CLASS, "finished StaticObjectMethod " + myName);
	return result;
}
