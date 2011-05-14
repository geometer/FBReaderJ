/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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
#include <ZLImage.h>
#include <ZLStringUtil.h>
#include <ZLDir.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLMimeType.h>

#include "OEBPlugin.h"
#include "OEBMetaInfoReader.h"
#include "OEBBookReader.h"
#include "OEBCoverReader.h"
#include "OEBTextStream.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

static const std::string OPF = "opf";
static const std::string OEBZIP = "oebzip";
static const std::string EPUB = "epub";

OEBPlugin::~OEBPlugin() {
}

bool OEBPlugin::providesMetaInfo() const {
	return true;
}

bool OEBPlugin::acceptsFile(const ZLFile &file) const {
	const std::string &mimeType = file.mimeType();
	const std::string &extension = file.extension();
	if (!mimeType.empty()) {
		return 
			mimeType == ZLMimeType::APPLICATION_EPUB_ZIP ||
			(mimeType == ZLMimeType::APPLICATION_XML && extension == OPF) ||
			(mimeType == ZLMimeType::APPLICATION_ZIP && extension == OEBZIP);
	}
	return extension == OPF || extension == OEBZIP || extension == EPUB;
}

ZLFile OEBPlugin::opfFile(const ZLFile &oebFile) {
	if (oebFile.extension() == OPF) {
		return oebFile;
	}

	ZLLogger::Instance().println("epub", "Looking for opf file in " + oebFile.path());
	oebFile.forceArchiveType(ZLFile::ZIP);
	shared_ptr<ZLDir> zipDir = oebFile.directory(false);
	if (zipDir.isNull()) {
		ZLLogger::Instance().println("epub", "Couldn't open zip archive");
		return ZLFile::NO_FILE;
	}
	std::vector<std::string> fileNames;
	zipDir->collectFiles(fileNames, false);
	for (std::vector<std::string>::const_iterator it = fileNames.begin(); it != fileNames.end(); ++it) {
		ZLLogger::Instance().println("epub", "Item: " + *it);
		if (ZLStringUtil::stringEndsWith(*it, ".opf")) {
			return ZLFile(zipDir->itemPath(*it));
		}
	}
	ZLLogger::Instance().println("epub", "Opf file not found");
	return ZLFile::NO_FILE;
}

bool OEBPlugin::readMetaInfo(Book &book) const {
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> lock = file.inputStream();
	const ZLFile opfFile = this->opfFile(file);
	bool code = OEBMetaInfoReader(book).readMetaInfo(opfFile);
	if (code && book.language().empty()) {
		shared_ptr<ZLInputStream> oebStream = new OEBTextStream(opfFile);
		detectLanguage(book, *oebStream);
	}
	return code;
}

class InputStreamLock : public ZLUserData {

public:
	InputStreamLock(shared_ptr<ZLInputStream> stream);

private:
	shared_ptr<ZLInputStream> myStream;
};

InputStreamLock::InputStreamLock(shared_ptr<ZLInputStream> stream) : myStream(stream) {
}

bool OEBPlugin::readModel(BookModel &model) const {
	const ZLFile &file = model.book()->file();
	model.addUserData(
		"inputStreamLock",
		new InputStreamLock(file.inputStream())
	);
	return OEBBookReader(model).readBook(opfFile(file));
}

shared_ptr<ZLImage> OEBPlugin::coverImage(const ZLFile &file) const {
	return OEBCoverReader().readCover(opfFile(file));
}
