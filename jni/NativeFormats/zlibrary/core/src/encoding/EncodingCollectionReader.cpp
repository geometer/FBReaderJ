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

#include <ZLUnicodeUtil.h>

#include "EncodingCollectionReader.h"

static const std::string GROUP = "group";
static const std::string ENCODING = "encoding";
static const std::string NAME = "name";
static const std::string REGION = "region";
static const std::string ALIAS = "alias";
static const std::string CODE = "code";
static const std::string NUMBER = "number";

ZLEncodingCollectionReader::ZLEncodingCollectionReader(ZLEncodingCollection &collection) : myCollection(collection) {
}

void ZLEncodingCollectionReader::startElementHandler(const char *tag, const char **attributes) {
	if (GROUP == tag) {
		const char *name = attributeValue(attributes, NAME.c_str());
		if (name != 0) {
			myCurrentSet = new ZLEncodingSet(name);
		}
	} else if (!myCurrentSet.isNull()) {
		if (ENCODING == tag) {
			const char *name = attributeValue(attributes, NAME.c_str());
			const char *region = attributeValue(attributes, REGION.c_str());
			if ((name != 0) && (region != 0)) {
				const std::string sName = name;
				myCurrentInfo = new ZLEncodingConverterInfo(sName, region);
				myNames.push_back(sName);
			}
		} else if (!myCurrentInfo.isNull()) {
			const char *name = 0;
			if (CODE == tag) {
				name = attributeValue(attributes, NUMBER.c_str());
			} else if (ALIAS == tag) {
				name = attributeValue(attributes, NAME.c_str());
			}
			if (name != 0) {
				const std::string sName = name;
				myCurrentInfo->addAlias(sName);
				myNames.push_back(sName);
			}
		}
	}
}

void ZLEncodingCollectionReader::endElementHandler(const char *tag) {
	if (!myCurrentInfo.isNull() && (ENCODING == tag)) {
		if (myCurrentInfo->canCreateConverter()) {
			myCurrentSet->addInfo(myCurrentInfo);
			for (std::vector<std::string>::const_iterator it = myNames.begin(); it != myNames.end(); ++it) {
				myCollection.myInfosByName[ZLUnicodeUtil::toLower(*it)] = myCurrentInfo;
			}
		}
		myCurrentInfo = 0;
		myNames.clear();
	} else if (!myCurrentSet.isNull() && (GROUP == tag)) {
		if (!myCurrentSet->infos().empty()) {
			myCollection.mySets.push_back(myCurrentSet);
		}
		myCurrentSet = 0;
	}
}
