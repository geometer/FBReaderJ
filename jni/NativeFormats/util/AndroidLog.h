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

#ifndef __ANDROIDLOG_H__
#define __ANDROIDLOG_H__

#include <jni.h>

#include <cstdarg>
#include <cstdio>

#include <string>

#include <AndroidUtil.h>


class AndroidLog {
public:
	AndroidLog();
	~AndroidLog();

private:
	void extractLogClass();
	void extractSystemErr();
	void prepareBuffer();

public:
	void w(const std::string &tag, const std::string &message);

	void errln(const std::string &message);
	void errln(jobject object);
	void errln(int value);

	void err(const std::string &message);
	void err(jobject object);
	void err(int value);

	void wf(const std::string &tag, const std::string &format, ...);
	void errlnf(const std::string &format, ...);
	void errf(const std::string &format, ...);

private:
	JNIEnv *myEnv;

	jclass myLogClass;

	jobject mySystemErr;
	jclass myPrintStreamClass;

	char *myBuffer;
};

inline AndroidLog::AndroidLog() {
	myEnv = AndroidUtil::getEnv();
	myLogClass = 0;
	mySystemErr = 0;
	myPrintStreamClass = 0;
	myBuffer = 0;
}

inline AndroidLog::~AndroidLog() {
	if (myBuffer != 0) {
		delete[] myBuffer;
	}
	myEnv->DeleteLocalRef(myLogClass);
	myEnv->DeleteLocalRef(mySystemErr);
	myEnv->DeleteLocalRef(myPrintStreamClass);
}

inline void AndroidLog::extractLogClass() {
	if (myLogClass == 0) {
		myLogClass = myEnv->FindClass("android/util/Log");
	}
}

inline void AndroidLog::extractSystemErr() {
	if (mySystemErr == 0) {
		jclass systemClass = myEnv->FindClass("java/lang/System");
		jfieldID systemErr = myEnv->GetStaticFieldID(systemClass, "err", "Ljava/io/PrintStream;");
		mySystemErr = myEnv->GetStaticObjectField(systemClass, systemErr);
		myEnv->DeleteLocalRef(systemClass);
	}
	if (myPrintStreamClass == 0) {
		myPrintStreamClass = myEnv->FindClass("java/io/PrintStream");
	}
}



inline void AndroidLog::w(const std::string &tag, const std::string &message) {
	extractLogClass();
	jmethodID logW = myEnv->GetStaticMethodID(myLogClass, "w", "(Ljava/lang/String;Ljava/lang/String;)I");
	jstring javaTag = myEnv->NewStringUTF(tag.c_str());
	jstring javaMessage = myEnv->NewStringUTF(message.c_str());
	myEnv->CallStaticIntMethod(myLogClass, logW, javaTag, javaMessage);
	myEnv->DeleteLocalRef(javaTag);
	myEnv->DeleteLocalRef(javaMessage);
}



inline void AndroidLog::errln(const std::string &message) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "println", "(Ljava/lang/String;)V");
	jstring javaMessage = myEnv->NewStringUTF(message.c_str());
	myEnv->CallVoidMethod(mySystemErr, println, javaMessage);
	myEnv->DeleteLocalRef(javaMessage);
}

inline void AndroidLog::errln(jobject object) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "println", "(Ljava/lang/Object;)V");
	myEnv->CallVoidMethod(mySystemErr, println, object);
}

inline void AndroidLog::errln(int value) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "println", "(I)V");
	myEnv->CallVoidMethod(mySystemErr, println, (jint)value);
}



inline void AndroidLog::err(const std::string &message) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "print", "(Ljava/lang/String;)V");
	jstring javaMessage = myEnv->NewStringUTF(message.c_str());
	myEnv->CallVoidMethod(mySystemErr, println, javaMessage);
	myEnv->DeleteLocalRef(javaMessage);
}

inline void AndroidLog::err(jobject object) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "print", "(Ljava/lang/Object;)V");
	myEnv->CallVoidMethod(mySystemErr, println, object);
}

inline void AndroidLog::err(int value) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "print", "(I)V");
	myEnv->CallVoidMethod(mySystemErr, println, (jint)value);
}

inline void AndroidLog::prepareBuffer()	{
	if (myBuffer == 0) {
		myBuffer = new char[8192];
	}
	myBuffer[0] = '\0';
}

inline void AndroidLog::wf(const std::string &tag, const std::string &format, ...) {
	prepareBuffer();
	va_list args;
	va_start(args, format);
	vsprintf(myBuffer, format.c_str(), args);
	va_end(args);
	w(tag, myBuffer);
}

inline void AndroidLog::errlnf(const std::string &format, ...) {
	prepareBuffer();
	va_list args;
	va_start(args, format);
	vsprintf(myBuffer, format.c_str(), args);
	va_end(args);
	errln(myBuffer);
}

inline void AndroidLog::errf(const std::string &format, ...) {
	prepareBuffer();
	va_list args;
	va_start(args, format);
	vsprintf(myBuffer, format.c_str(), args);
	va_end(args);
	err(myBuffer);
}

#endif /* __ANDROIDLOG_H__ */
