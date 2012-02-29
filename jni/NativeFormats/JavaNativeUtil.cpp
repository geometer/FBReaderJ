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

#include <vector>
#include <string>

#include <AndroidUtil.h>

#include <ZLEncodingConverter.h>


extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_util_NativeUtil_collectEncodingNames(JNIEnv* env, jobject thiz, jobject namesMap) {
	ZLEncodingCollection &collection = ZLEncodingCollection::Instance();

	const std::vector<shared_ptr<ZLEncodingSet> > &sets = collection.sets();
	for (size_t i = 0; i < sets.size(); ++i) {
		shared_ptr<ZLEncodingSet> set = sets[i];
		if (set.isNull()) {
			continue;
		}

		const std::vector<ZLEncodingConverterInfoPtr> &infos = set->infos();
		for (size_t j = 0; j < infos.size(); ++j) {
			ZLEncodingConverterInfoPtr info = infos[j];
			if (info.isNull()) {
				continue;
			}
			jstring javaName = env->NewStringUTF(info->name().c_str());
			jstring javaVisibleName = env->NewStringUTF(info->visibleName().c_str());
			env->CallObjectMethod(namesMap, AndroidUtil::MID_java_util_Map_put, javaName, javaVisibleName);
			env->DeleteLocalRef(javaName);
			env->DeleteLocalRef(javaVisibleName);
		}
	}
}
