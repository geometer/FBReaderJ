/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

//#include <ZLTimeManager.h>
//#include <ZLDialogManager.h>
//#include <ZLImageManager.h>
//#include <ZLMessage.h>
#include <ZLUnicodeUtil.h>
#include <ZLLogger.h>

#include "ZLibrary.h"
#include "../filesystem/ZLFSManager.h"
//#include "../options/ZLConfig.h"
//#include "../network/ZLNetworkManager.h"

//bool ZLibrary::ourLocaleIsInitialized = false;
//std::string ZLibrary::ourLanguage;
//std::string ZLibrary::ourCountry;
std::string ZLibrary::ourZLibraryDirectory;

std::string ZLibrary::ourApplicationName;
//std::string ZLibrary::ourImageDirectory;
//std::string ZLibrary::ourApplicationImageDirectory;
std::string ZLibrary::ourApplicationDirectory;
//std::string ZLibrary::ourApplicationWritableDirectory;
//std::string ZLibrary::ourDefaultFilesPathPrefix;

//const std::string ZLibrary::BaseDirectory = std::string(BASEDIR);

void ZLibrary::parseArguments(int &argc, char **&argv) {
	static const std::string LANGUAGE_OPTION = "-lang";
	static const std::string LOGGER_OPTION = "-log";
	while ((argc > 2) && (argv[1] != 0) && (argv[2] != 0)) {
		const std::string argument = argv[1];
		/*if (LANGUAGE_OPTION == argument) {
			ourLocaleIsInitialized = true;
			std::string locale = argv[2];
			int index = locale.find('_');
			if (index >= 0) {
				ourLanguage = locale.substr(0, index);
				ourCountry = locale.substr(index + 1);
			} else {
				ourLanguage = locale;
			}
		} else*/ if (LOGGER_OPTION == argument) {
			std::string loggerClasses = argv[2];
			while (std::size_t index = loggerClasses.find(':') != std::string::npos) {
				ZLLogger::Instance().registerClass(loggerClasses.substr(0, index));
				loggerClasses.erase(0, index + 1);
			}
			ZLLogger::Instance().registerClass(loggerClasses);
		} else {
			ZLLogger::Instance().println(ZLLogger::DEFAULT_CLASS, "unknown argument: " + argument);
		}
		argc -= 2;
		argv += 2;
	}
	ourZLibraryDirectory = ".";//BaseDirectory + FileNameDelimiter + "zlibrary";
}

void ZLibrary::shutdown() {
//	ZLNetworkManager::deleteInstance();
//	ZLImageManager::deleteInstance();
//	ZLCommunicationManager::deleteInstance();
//	ZLDialogManager::deleteInstance();
	ZLFSManager::deleteInstance();
//	ZLTimeManager::deleteInstance();
//	ZLConfigManager::deleteInstance();
}

/*std::string ZLibrary::replaceRegExps(const std::string &pattern) {
	static const std::string NAME_PATTERN = "%APPLICATION_NAME%";
	static const std::string LOWERCASENAME_PATTERN = "%application_name%";
	std::string str = pattern;
	int index = -1;
	while ((index = str.find(NAME_PATTERN)) != -1) {
	  str.erase(index, NAME_PATTERN.length());
		str.insert(index, ourApplicationName);
	}
	while ((index = str.find(LOWERCASENAME_PATTERN)) != -1) {
	  str.erase(index, LOWERCASENAME_PATTERN.length());
		str.insert(index, ZLUnicodeUtil::toLower(ourApplicationName));
	}
	return str;
}*/

void ZLibrary::initApplication(const std::string &name) {
	ourApplicationName = name;
//	ourImageDirectory = replaceRegExps(IMAGEDIR);
//	ourApplicationImageDirectory = replaceRegExps(APPIMAGEDIR);
	ourApplicationDirectory = ".";//BaseDirectory + FileNameDelimiter + ourApplicationName;
//	ourApplicationWritableDirectory =
//#ifdef XMLCONFIGHOMEDIR
//		XMLCONFIGHOMEDIR + FileNameDelimiter + "." + name;
//#else
//		"~" + FileNameDelimiter + "." + name;
//#endif
//	ourDefaultFilesPathPrefix = ourApplicationDirectory + FileNameDelimiter + "default" + FileNameDelimiter;
}

/*std::string ZLibrary::Language() {
	if (ourLanguage.empty()) {
		if (!ourLocaleIsInitialized) {
			initLocale();
			ourLocaleIsInitialized = true;
		}
	}
	if (ourLanguage.empty()) {
		ourLanguage = "en";
	}
	return ourLanguage;
}*/

/*std::string ZLibrary::Country() {
	if (ourCountry.empty() && !ourLocaleIsInitialized) {
		initLocale();
		ourLocaleIsInitialized = true;
	}
	return ourCountry;
}*/
