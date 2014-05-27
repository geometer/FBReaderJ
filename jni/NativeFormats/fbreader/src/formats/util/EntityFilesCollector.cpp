/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#include <ZLStringUtil.h>
#include <ZLibrary.h>
#include <ZLFile.h>
#include <ZLDir.h>

#include "EntityFilesCollector.h"

EntityFilesCollector *EntityFilesCollector::ourInstance = 0;

EntityFilesCollector &EntityFilesCollector::Instance() {
	if (ourInstance == 0) {
		ourInstance = new EntityFilesCollector();
	}
	return *ourInstance;
}
	
const std::vector<std::string> &EntityFilesCollector::externalDTDs(const std::string &format) {
	std::map<std::string,std::vector<std::string> >::const_iterator it = myCollections.find(format);
	if (it != myCollections.end()) {
		return it->second;
	}

	std::vector<std::string> &collection = myCollections[format];

	std::string directoryName =
		ZLibrary::ApplicationDirectory() + ZLibrary::FileNameDelimiter +
		"formats" + ZLibrary::FileNameDelimiter + format;
	shared_ptr<ZLDir> dtdPath = ZLFile(directoryName).directory();
	if (!dtdPath.isNull()) {
		std::vector<std::string> files;
		dtdPath->collectFiles(files, false);
		for (std::vector<std::string>::const_iterator it = files.begin(); it != files.end(); ++it) {
			if (ZLStringUtil::stringEndsWith(*it, ".ent")) {
				collection.push_back(dtdPath->itemPath(*it));
			}
		}
	}

	return collection;
}

EntityFilesCollector::EntityFilesCollector() {
}
