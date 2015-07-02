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

#include <cstdlib>
#include <cctype>

#include <ZLibrary.h>
#include <ZLFile.h>
#include <ZLXMLReader.h>

#include "HtmlEntityCollection.h"

class CollectionReader : public ZLXMLReader {

public:
	CollectionReader(std::map<std::string,int> &collection);
	void startElementHandler(const char *tag, const char **attributes);

private:
	std::map<std::string,int> &myCollection;
};

std::map<std::string,int> HtmlEntityCollection::ourCollection;

int HtmlEntityCollection::symbolNumber(const std::string &name) {
	if (ourCollection.empty()) {
		CollectionReader(ourCollection).readDocument(ZLFile(
			ZLibrary::ApplicationDirectory() + ZLibrary::FileNameDelimiter +
			"formats" + ZLibrary::FileNameDelimiter +
			"html" + ZLibrary::FileNameDelimiter + "html.ent"
		));
	}
	std::map<std::string,int>::const_iterator it = ourCollection.find(name);
	return (it == ourCollection.end()) ? 0 : it->second;
}

CollectionReader::CollectionReader(std::map<std::string,int> &collection) : myCollection(collection) {
}

void CollectionReader::startElementHandler(const char *tag, const char **attributes) {
	static const std::string ENTITY = "entity";

	if (ENTITY == tag) {
		for (int i = 0; i < 4; ++i) {
			if (attributes[i] == 0) {
				return;
			}
		}
		static const std::string _name = "name";
		static const std::string _number = "number";
		if (_name == attributes[0] && _number == attributes[2]) {
			myCollection[attributes[1]] = std::atoi(attributes[3]);
		}
	}
}
