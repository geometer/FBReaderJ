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

// This code is temporary: it makes eclipse not complain
#ifndef _JNI_H
#define JNIIMPORT
#define JNIEXPORT
#define JNICALL
#endif /* _JNI_H */


#include <AndroidUtil.h>


template <class T>
static T *extractPointer(JNIEnv *env, jobject base) {
	jlong ptr = env->GetLongField(base, AndroidUtil::FID_NativeFormatPlugin_NativePointer);
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
