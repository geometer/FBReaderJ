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

#include <jni.h>

#include <string>


class AndroidLog {
public:
	AndroidLog(JNIEnv *env);

private:
	bool extractLogClass();
	bool extractSystemErr();

public:
	void w(const std::string &tag, const std::string &message);

	void errln(const std::string &message);
	void errln(jobject object);
	void errln(int value);

	void err(const std::string &message);
	void err(jobject object);
	void err(int value);

private:
	JNIEnv *myEnv;

	jclass myLogClass;

	jobject mySystemErr;
	jclass myPrintStreamClass;
};

inline AndroidLog::AndroidLog(JNIEnv *env) : myEnv(env) {
	myLogClass = 0;
	mySystemErr = 0;
	myPrintStreamClass = 0;
}

inline bool AndroidLog::extractLogClass() {
	if (myLogClass == 0) {
		myLogClass = myEnv->FindClass("android/util/Log");
	}
}

inline bool AndroidLog::extractSystemErr() {
	if (mySystemErr == 0) {
		jclass systemClass = myEnv->FindClass("java/lang/System");
		jfieldID systemErr = myEnv->GetStaticFieldID(systemClass, "err", "Ljava/io/PrintStream;");
		mySystemErr = myEnv->GetStaticObjectField(systemClass, systemErr);
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
}



inline void AndroidLog::errln(const std::string &message) {
	extractSystemErr();
	jmethodID println = myEnv->GetMethodID(myPrintStreamClass, "println", "(Ljava/lang/String;)V");
	jstring javaMessage = myEnv->NewStringUTF(message.c_str());
	myEnv->CallVoidMethod(mySystemErr, println, javaMessage);
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
