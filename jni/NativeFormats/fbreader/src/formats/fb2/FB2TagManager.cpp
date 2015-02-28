/*
 * Copyright (C) 2008-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <ZLFile.h>
#include <ZLXMLReader.h>
#include <ZLibrary.h>
#include <ZLUnicodeUtil.h>

#include "FB2TagManager.h"

class FB2TagInfoReader : public ZLXMLReader {

public:
	FB2TagInfoReader(std::map<std::string,std::vector<std::string> > &tagMap);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);

private:
	std::map<std::string,std::vector<std::string> > &myTagMap;

	std::string myCategoryName;
	std::string mySubCategoryName;
	std::vector<std::string> myGenreIds;
	std::string myLanguage;
};

FB2TagInfoReader::FB2TagInfoReader(std::map<std::string,std::vector<std::string> > &tagMap) : myTagMap(tagMap) {
	myLanguage = ZLibrary::Language();
	if (myLanguage != "ru") {
		myLanguage = "en";
	}
}

static const std::string CATEGORY_NAME_TAG = "root-descr";
static const std::string SUBCATEGORY_NAME_TAG = "genre-descr";
static const std::string GENRE_TAG = "genre";
static const std::string SUBGENRE_TAG = "subgenre";
static const std::string SUBGENRE_ALT_TAG = "genre-alt";

void FB2TagInfoReader::startElementHandler(const char *tag, const char **attributes) {
	if ((SUBGENRE_TAG == tag) || (SUBGENRE_ALT_TAG == tag)) {
		const char *id = attributeValue(attributes, "value");
		if (id != 0) {
			myGenreIds.push_back(id);
		}
	} else if (CATEGORY_NAME_TAG == tag) {
		const char *lang = attributeValue(attributes, "lang");
		if ((lang != 0) && (myLanguage == lang)) {
			const char *name = attributeValue(attributes, "genre-title");
			if (name != 0) {
				myCategoryName = name;
				ZLUnicodeUtil::utf8Trim(myCategoryName);
			}
		}
	} else if (SUBCATEGORY_NAME_TAG == tag) {
		const char *lang = attributeValue(attributes, "lang");
		if ((lang != 0) && (myLanguage == lang)) {
			const char *name = attributeValue(attributes, "title");
			if (name != 0) {
				mySubCategoryName = name;
				ZLUnicodeUtil::utf8Trim(mySubCategoryName);
			}
		}
	}
}

void FB2TagInfoReader::endElementHandler(const char *tag) {
	if (GENRE_TAG == tag) {
		myCategoryName.erase();
		mySubCategoryName.erase();
		myGenreIds.clear();
	} else if (SUBGENRE_TAG == tag) {
		if (!myCategoryName.empty() && !mySubCategoryName.empty()) {
			const std::string fullTagName = myCategoryName + '/' + mySubCategoryName;
			for (std::vector<std::string>::const_iterator it = myGenreIds.begin(); it != myGenreIds.end(); ++it) {
				myTagMap[*it].push_back(fullTagName);
			}
		}
		mySubCategoryName.erase();
		myGenreIds.clear();
	}
}

FB2TagManager *FB2TagManager::ourInstance = 0;

const FB2TagManager &FB2TagManager::Instance() {
	if (ourInstance == 0) {
		ourInstance = new FB2TagManager();
	}
	return *ourInstance;
}

FB2TagManager::FB2TagManager() {
	FB2TagInfoReader(myTagMap).readDocument(ZLFile(
		ZLibrary::ApplicationDirectory() + ZLibrary::FileNameDelimiter +
		"formats" + ZLibrary::FileNameDelimiter + "fb2" +
		ZLibrary::FileNameDelimiter + "fb2genres.xml"
	));
}

const std::vector<std::string> &FB2TagManager::humanReadableTags(const std::string &id) const {
	static const std::vector<std::string> EMPTY;
	std::map<std::string,std::vector<std::string> >::const_iterator it = myTagMap.find(id);
	return (it != myTagMap.end()) ? it->second : EMPTY;
}
