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

#include "JniEnvelope.h"

Method::Method(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature) {
	myEnv = env;
	myId = env->GetMethodID(cls, name.c_str(), signature.c_str());
}

Method::~Method() {
}

VoidMethod::VoidMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature) : Method(env, cls, name, signature + "V") {
}

void VoidMethod::call(jobject base, ...) {
	va_list lst;
	va_start(lst, base);
	myEnv->CallVoidMethod(base, myId, lst);
	va_end(lst);
}

IntMethod::IntMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature) : Method(env, cls, name, signature + "I") {
}

jint IntMethod::call(jobject base, ...) {
	va_list lst;
	va_start(lst, base);
	jint result = myEnv->CallIntMethod(base, myId, lst);
	va_end(lst);
	return result;
}

LongMethod::LongMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature) : Method(env, cls, name, signature + "J") {
}

jlong LongMethod::call(jobject base, ...) {
	va_list lst;
	va_start(lst, base);
	jlong result = myEnv->CallLongMethod(base, myId, lst);
	va_end(lst);
	return result;
}

BooleanMethod::BooleanMethod(JNIEnv *env, jclass cls, const std::string &name, const std::string &signature) : Method(env, cls, name, signature + "Z") {
}

jboolean BooleanMethod::call(jobject base, ...) {
	va_list lst;
	va_start(lst, base);
	jboolean result = myEnv->CallBooleanMethod(base, myId, lst);
	va_end(lst);
	return result;
}
