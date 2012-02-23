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

#include <jni.h>

// This code is temporary: it makes eclipse not complain
#ifndef _JNI_H
#define JNIIMPORT
#define JNIEXPORT
#define JNICALL
#endif /* _JNI_H */


#include <AndroidUtil.h>

#include <ZLFile.h>

#include "fbreader/src/formats/FormatPlugin.h"


extern "C"
JNIEXPORT jobjectArray JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_nativePlugins(JNIEnv* env, jobject thiz) {
	const std::vector<shared_ptr<FormatPlugin> > plugins = PluginCollection::Instance().plugins();
	const size_t size = plugins.size();
	jclass cls = env->FindClass(AndroidUtil::Class_NativeFormatPlugin);
	jobjectArray javaPlugins = env->NewObjectArray(size, cls, NULL);

	for (size_t i = 0; i < size; ++i) {
		FormatPlugin *ptr = &*(plugins[i]);
		env->SetObjectArrayElement(
			javaPlugins, i,
			env->NewObject(cls, AndroidUtil::MID_NativeFormatPlugin_init, (jlong)ptr)
		);
	}
	return javaPlugins;
}

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_free(JNIEnv* env, jobject thiz) {
	PluginCollection::deleteInstance();
}
