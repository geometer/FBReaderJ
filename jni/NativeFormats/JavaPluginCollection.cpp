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

#include <AndroidLog.h>

#include <ZLFile.h>
#include <ZLInputStream.h>


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



void extension1(JNIEnv *env);

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_collectNativePlugins(JNIEnv* env, jobject thiz, jobject plugins) {
	PluginCollectionHelper helper(env);

	if (!helper.init(plugins)) {
		return;
	}

	/*if (!helper.addToArrayList(new FormatPlugin(...))) {
		return;
	}*/

	extension1(env);
}



void extension1(JNIEnv *env) {
	AndroidLog log;

	log.w("FBREADER", "extension 1 start");

	if (true) {
		ZLFile nonexistent("/mnt/sdcard/Books/b.txt");
		log.w("FBREADER", "nonexistent created");
		bool flag = nonexistent.exists();
		log.wf("FBREADER", "Does nonexistent file exist?: \"%s\"", flag ? "true" : "false");
	}

	ZLFile file("/mnt/sdcard/Books/a.txt");

	log.wf("FBREADER", "file: %s", file.path().c_str());
	log.wf("FBREADER", "exists: \"%s\"", file.exists() ? "true" : "false");
	log.wf("FBREADER", "size: %d", file.size());

	shared_ptr<ZLInputStream> input = file.inputStream();
	if (input.isNull() || !input->open()) {
		log.w("FBREADER", "unable to open file");
	} else {
		log.w("FBREADER", "contents:");
		std::string line;

		const size_t size = 256;
		char *buffer = new char[size];
		size_t length;

		do {
			length = input->read(buffer, size);
			if (length > 0) {
				size_t index = line.length();
				line.append(buffer, length);
				index = line.find('\n', index);
				if (index != std::string::npos) {
					log.w("FBREADER", line.substr(0, index));
					line.erase(0, index + 1);
				}
			}
		} while (length == size);

		if (line.length() > 0) {
			log.w("FBREADER", line);
			log.w("FBREADER", "/*no end of line*/");
		}

		log.w("FBREADER", "contents: EOF");

		delete[] buffer;
		input->close();
	}

	log.w("FBREADER", "extension 1 end");
}
