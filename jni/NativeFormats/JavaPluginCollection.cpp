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

#include <vector>

#include <AndroidUtil.h>
#include <JniEnvelope.h>

#include "fbreader/src/formats/FormatPlugin.h"

extern "C"
JNIEXPORT jobjectArray JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_nativePlugins(JNIEnv* env, jobject thiz, jobject systemInfo) {
	const std::vector<shared_ptr<FormatPlugin> > plugins = PluginCollection::Instance().plugins();
	const std::size_t size = plugins.size();
	jclass cls = AndroidUtil::Class_NativeFormatPlugin.j();
	// TODO: memory leak?
	jobjectArray javaPlugins = env->NewObjectArray(size, cls, 0);

	for (std::size_t i = 0; i < size; ++i) {
		jstring fileType = AndroidUtil::createJavaString(env, plugins[i]->supportedFileType());
		jobject p = AndroidUtil::StaticMethod_NativeFormatPlugin_create->call(systemInfo, fileType);
		env->SetObjectArrayElement(javaPlugins, i, p);
		env->DeleteLocalRef(p);
		env->DeleteLocalRef(fileType);
	}
	return javaPlugins;
}

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_free(JNIEnv* env, jobject thiz) {
	PluginCollection::deleteInstance();
}
