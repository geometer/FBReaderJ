/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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
#include <AndroidUtil.h>

//#include <ZLApplication.h>
#include <ZLibrary.h>
//#include <ZLLanguageUtil.h>

#include "../../../../core/src/unix/library/ZLibraryImplementation.h"

#include "../filesystem/ZLAndroidFSManager.h"
//#include "../time/ZLGtkTime.h"
//#include "../dialogs/ZLGtkDialogManager.h"
//#include "../image/ZLGtkImageManager.h"
//#include "../view/ZLGtkPaintContext.h"
//#include "../../unix/message/ZLUnixMessage.h"
//#include "../../../../core/src/util/ZLKeyUtil.h"
//#include "../../../../core/src/unix/xmlconfig/XMLConfig.h"
//#include "../../../../core/src/unix/iconv/IConvEncodingConverter.h"
//#include "../../../../core/src/unix/curl/ZLCurlNetworkManager.h"

class ZLAndroidLibraryImplementation : public ZLibraryImplementation {

private:
	void init(int &argc, char **&argv);
//	ZLPaintContext *createContext();
//	void run(ZLApplication *application);
};

void initLibrary() {
	new ZLAndroidLibraryImplementation();
}

void ZLAndroidLibraryImplementation::init(int &argc, char **&argv) {
	ZLibrary::parseArguments(argc, argv);

//	XMLConfigManager::createInstance();
	ZLAndroidFSManager::createInstance();
//	ZLGtkTimeManager::createInstance();
//	ZLGtkDialogManager::createInstance();
//	ZLUnixCommunicationManager::createInstance();
//	ZLGtkImageManager::createInstance();
//	ZLEncodingCollection::Instance().registerProvider(new IConvEncodingConverterProvider());
//	ZLCurlNetworkManager::createInstance();

//	ZLKeyUtil::setKeyNamesFileName("keynames-gtk.xml");
}

/*ZLPaintContext *ZLAndroidLibraryImplementation::createContext() {
	return new ZLGtkPaintContext();
}*/

/*void ZLAndroidLibraryImplementation::run(ZLApplication *application) {
	ZLDialogManager::Instance().createApplicationWindow(application);
	application->initWindow();
	gtk_widget_set_default_direction(ZLLanguageUtil::isRTLLanguage(ZLibrary::Language()) ? GTK_TEXT_DIR_RTL : GTK_TEXT_DIR_LTR);
	gtk_main();
	delete application;
}*/


std::string ZLibrary::Language() {
	JNIEnv *env = AndroidUtil::getEnv();
	jclass cls = env->FindClass(AndroidUtil::Class_java_util_Locale);
	jobject locale = env->CallStaticObjectMethod(cls, AndroidUtil::SMID_java_util_Locale_getDefault);
	jstring javaLang = (jstring)env->CallObjectMethod(locale, AndroidUtil::MID_java_util_Locale_getLanguage);
	const char *langData = env->GetStringUTFChars(javaLang, 0);
	std::string lang(langData);
	env->ReleaseStringUTFChars(javaLang, langData);
	env->DeleteLocalRef(javaLang);
	env->DeleteLocalRef(locale);
	env->DeleteLocalRef(cls);
	return lang;
}

std::string ZLibrary::Version() {
	JNIEnv *env = AndroidUtil::getEnv();
	jclass cls = env->FindClass(AndroidUtil::Class_ZLibrary);
	jobject zlibrary = env->CallStaticObjectMethod(cls, AndroidUtil::SMID_ZLibrary_Instance);
	jstring javaVersion = (jstring)env->CallObjectMethod(zlibrary, AndroidUtil::MID_ZLibrary_getVersionName);
	const char *versionData = env->GetStringUTFChars(javaVersion, 0);
	std::string version(versionData);
	env->ReleaseStringUTFChars(javaVersion, versionData);
	env->DeleteLocalRef(javaVersion);
	env->DeleteLocalRef(zlibrary);
	env->DeleteLocalRef(cls);
	return version;
}
