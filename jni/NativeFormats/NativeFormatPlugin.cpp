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


template <class T>
static T *extractPointer(JNIEnv *env, jobject base) {
	jclass cls = env->GetObjectClass(base);
	jmethodID getNativePointer = env->GetMethodID(cls, "getNativePointer", "()J");
	if (getNativePointer == 0) {
		return 0;
	}
	jlong ptr = env->CallLongMethod(base, getNativePointer);
	if (env->ExceptionCheck() == JNI_TRUE) {
		return 0;
	}
	return (T*)ptr;
}


class FormatPlugin;

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_acceptsFile(JNIEnv* env, jobject thiz, jobject file) {
	FormatPlugin *impl = extractPointer<FormatPlugin>(env, thiz);
	if (impl == 0) {
		return 0;
	}
	return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readMetaInfo(JNIEnv* env, jobject thiz, jobject book) {
	FormatPlugin *impl = extractPointer<FormatPlugin>(env, thiz);
	if (impl == 0) {
		return 0;
	}
	return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readModel(JNIEnv* env, jobject thiz, jobject model) {
	FormatPlugin *impl = extractPointer<FormatPlugin>(env, thiz);
	if (impl == 0) {
		return 0;
	}
	return JNI_FALSE;
}

extern "C"
JNIEXPORT jobject JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readCover(JNIEnv* env, jobject thiz, jobject file) {
	FormatPlugin *impl = extractPointer<FormatPlugin>(env, thiz);
	if (impl == 0) {
		return 0;
	}
	return 0;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_geometerplus_fbreader_formats_NativeFormatPlugin_readAnnotation(JNIEnv* env, jobject thiz, jobject file) {
	FormatPlugin *impl = extractPointer<FormatPlugin>(env, thiz);
	if (impl == 0) {
		return 0;
	}
	return 0;
}
