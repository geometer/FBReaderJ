/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include "liblinebreak-2.0/linebreak.h"

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_init(JNIEnv *env, jobject thiz) {
	init_linebreak();
}

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_setLineBreaksForCharArray(JNIEnv *env, jobject thiz, jcharArray data, jint offset, jint length, jstring lang, jbyteArray breaks) {
	jchar* dataArray = env->GetCharArrayElements(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray + offset, length, langArray, (char*)breaksArray);
	const jchar* start = dataArray + offset;
	const jchar* end = start + length;
	for (const jchar* ptr = start; ptr < end; ++ptr) {
		if (*ptr == (jchar)0xAD) {
			breaksArray[ptr - start] = LINEBREAK_NOBREAK;
		}
	}

	if (lang != 0) {
  	env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseCharArrayElements(data, dataArray, 0);
}

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_setLineBreaksForString(JNIEnv *env, jobject thiz, jstring data, jstring lang, jbyteArray breaks) {
	const jchar* dataArray = env->GetStringChars(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const size_t len = env->GetStringLength(data);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray, len, langArray, (char*)breaksArray);

	if (lang != 0) {
  	env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseStringChars(data, dataArray);
}
