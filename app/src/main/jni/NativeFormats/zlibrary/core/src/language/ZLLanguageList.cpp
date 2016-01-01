/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <set>

#include <ZLibrary.h>
//#include <ZLResource.h>
#include <ZLFile.h>

#include "ZLLanguageList.h"

std::vector<std::string> ZLLanguageList::ourLanguageCodes;

std::string ZLLanguageList::patternsDirectoryPath() {
	return ZLibrary::ZLibraryDirectory() + ZLibrary::FileNameDelimiter + "languagePatterns";
}

/*std::string ZLLanguageList::languageName(const std::string &code) {
	return ZLResource::resource("language")[ZLResourceKey(code)].value();
}*/

const std::vector<std::string> &ZLLanguageList::languageCodes() {
	if (ourLanguageCodes.empty()) {
		std::set<std::string> codes;
		shared_ptr<ZLDir> dir = ZLFile(patternsDirectoryPath()).directory(false);
		if (!dir.isNull()) {
			std::vector<std::string> fileNames;
			dir->collectFiles(fileNames, false);
			for (std::vector<std::string>::const_iterator it = fileNames.begin(); it != fileNames.end(); ++it) {
				const int index = it->find('_');
				if (index != -1) {
					codes.insert(it->substr(0, index));
				}
			}
		}

		for (std::set<std::string>::const_iterator it = codes.begin(); it != codes.end(); ++it) {
			ourLanguageCodes.push_back(*it);
		}
	}

	return ourLanguageCodes;
}
