/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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
