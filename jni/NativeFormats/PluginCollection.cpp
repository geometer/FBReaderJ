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


class PluginCollectionHelper {

public:
	PluginCollectionHelper(JNIEnv* env);
	bool init(jobject arrayList);

	template <class T>
	bool addToArrayList(T *impl);

private:
	JNIEnv *myEnv;

	jobject myArrayList;
	jmethodID myAddToArrayList;

	jclass myClassNative;
	jmethodID myInitNative;
};

PluginCollectionHelper::PluginCollectionHelper(JNIEnv* env) : myEnv(env) {
}

bool PluginCollectionHelper::init(jobject arrayList) {
	myArrayList = arrayList;

	jclass cls = myEnv->GetObjectClass(myArrayList);
	myAddToArrayList = myEnv->GetMethodID(cls, "add", "(Ljava/lang/Object;)Z");
	if (myAddToArrayList == 0) {
		return false;
	}

	myClassNative = myEnv->FindClass("org/geometerplus/fbreader/formats/NativeFormatPlugin");
	if (myClassNative == 0) {
		return false;
	}
	myInitNative = myEnv->GetMethodID(myClassNative, "<init>", "(J)V");
	if (myInitNative == 0) {
		return false;
	}
	return true;
}

template <class T>
bool PluginCollectionHelper::addToArrayList(T *impl) {
	const jobject base = myEnv->NewObject(myClassNative, myInitNative, (jlong)impl);
	if (base == 0) {
		return false;
	}
	myEnv->CallBooleanMethod(myArrayList, myAddToArrayList, base);
	return myEnv->ExceptionCheck() == JNI_FALSE;
}




extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_collectNativePlugins(JNIEnv* env, jobject thiz, jobject plugins) {
	PluginCollectionHelper helper(env);

	AndroidLog log(env);

	if (!helper.init(plugins)) {
		return;
	}

	/*if (!helper.addToArrayList(new FormatPlugin(...))) {
		return;
	}*/
}
