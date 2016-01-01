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

#include <ZLFile.h>
#include <ZLInputStream.h>
//#include <ZLOptions.h>

#include "PdbPlugin.h"
//#include "../../options/FBCategoryKey.h"

//#include "../../database/booksdb/BooksDBUtil.h"
//#include "../../database/booksdb/BooksDB.h"

PdbPlugin::~PdbPlugin() {
}

/*
std::string PdbPlugin::fileType(const ZLFile &file) {
	const std::string &extension = file.extension();
	if ((extension != "prc") && (extension != "pdb") && (extension != "mobi")) {
		return "";
	}

	const std::string &fileName = file.path();
	//int index = fileName.find(':');
	//ZLFile baseFile = (index == -1) ? file : ZLFile(fileName.substr(0, index));
	ZLFile baseFile(file.physicalFilePath());
	bool upToDate = BooksDBUtil::checkInfo(baseFile);

	//ZLStringOption palmTypeOption(FBCategoryKey::BOOKS, file.path(), "PalmType", "");
	std::string palmType = BooksDB::Instance().getPalmType(fileName);
	if ((palmType.length() != 8) || !upToDate) {
		shared_ptr<ZLInputStream> stream = file.inputStream();
		if (stream.isNull() || !stream->open()) {
			return "";
		}
		stream->seek(60, false);
		char id[8];
		stream->read(id, 8);
		stream->close();
		palmType = std::string(id, 8);
		if (!upToDate) {
			BooksDBUtil::saveInfo(baseFile);
		}
		//palmTypeOption.setValue(palmType);
		BooksDB::Instance().setPalmType(fileName, palmType);
	}
	return palmType;
}
*/
