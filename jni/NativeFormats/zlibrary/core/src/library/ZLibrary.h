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

#ifndef __ZLIBRARY_H__
#define __ZLIBRARY_H__

#include <string>

//class ZLApplication;
//class ZLPaintContext;

class ZLibrary {

public:
	static const std::string FileNameDelimiter;
	static const std::string PathDelimiter;
	static const std::string EndOfLine;
	static std::string Language();
//	static std::string Country();

	static std::string Version();

//	static const std::string BaseDirectory;
	static const std::string &ZLibraryDirectory();

//	static const std::string &ImageDirectory();
//	static const std::string &ApplicationName();
//	static const std::string &ApplicationImageDirectory();
	static const std::string &ApplicationDirectory();
//	static const std::string &DefaultFilesPathPrefix();

//	static const std::string &ApplicationWritableDirectory();

public:
	static bool init(int &argc, char **&argv);
	static void parseArguments(int &argc, char **&argv);
//	static ZLPaintContext *createContext();
//	static void run(ZLApplication *application);
	static void shutdown();

private:
//	static void initLocale();

private:
//	static bool ourLocaleIsInitialized;
//	static std::string ourLanguage;
//	static std::string ourCountry;
	static std::string ourZLibraryDirectory;

//	static std::string ourImageDirectory;
//	static std::string ourApplicationImageDirectory;
	static std::string ourApplicationName;
	static std::string ourApplicationDirectory;
//	static std::string ourApplicationWritableDirectory;
//	static std::string ourDefaultFilesPathPrefix;

private:
//	static std::string replaceRegExps(const std::string &pattern);

public:
	static void initApplication(const std::string &name);

private:
	ZLibrary();

friend class ZLApplicationBase;
};

inline const std::string &ZLibrary::ZLibraryDirectory() { return ourZLibraryDirectory; }
//inline const std::string &ZLibrary::ApplicationName() { return ourApplicationName; }
//inline const std::string &ZLibrary::ImageDirectory() { return ourImageDirectory; }
//inline const std::string &ZLibrary::ApplicationImageDirectory() { return ourApplicationImageDirectory; }
inline const std::string &ZLibrary::ApplicationDirectory() { return ourApplicationDirectory; }
//inline const std::string &ZLibrary::ApplicationWritableDirectory() { return ourApplicationWritableDirectory; }
//inline const std::string &ZLibrary::DefaultFilesPathPrefix() { return ourDefaultFilesPathPrefix; }

#endif /* __ZLIBRARY_H__ */
