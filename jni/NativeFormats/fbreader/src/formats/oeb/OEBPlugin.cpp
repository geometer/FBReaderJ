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
#include <ZLImage.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>
#include <ZLDir.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLXMLReader.h>

#include "OEBPlugin.h"
#include "OEBMetaInfoReader.h"
#include "OEBEncryptionReader.h"
#include "OEBUidReader.h"
#include "OEBBookReader.h"
#include "OEBCoverReader.h"
#include "OEBTextStream.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

static const std::string OPF = "opf";
static const std::string OEBZIP = "oebzip";
static const std::string EPUB = "epub";

class ContainerFileReader : public ZLXMLReader {

public:
	const std::string &rootPath() const;

private:
	void startElementHandler(const char *tag, const char **attributes);

private:
	std::string myRootPath;
};

const std::string &ContainerFileReader::rootPath() const {
	return myRootPath;
}

void ContainerFileReader::startElementHandler(const char *tag, const char **attributes) {
	const std::string tagString = ZLUnicodeUtil::toLower(tag);
	if (tagString == "rootfile") {
		const char *path = attributeValue(attributes, "full-path");
		if (path != 0) {
			myRootPath = path;
			interrupt();
		}
	}
}

OEBPlugin::~OEBPlugin() {
}

bool OEBPlugin::providesMetainfo() const {
	return true;
}

const std::string OEBPlugin::supportedFileType() const {
	return "ePub";
}

ZLFile OEBPlugin::epubFile(const ZLFile &oebFile) {
	const ZLFile epub = oebFile.extension() == OPF ? oebFile.getContainerArchive() : oebFile;
	epub.forceArchiveType(ZLFile::ZIP);
	return epub;
}

ZLFile OEBPlugin::opfFile(const ZLFile &oebFile) {
	//ZLLogger::Instance().registerClass("epub");

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

	const ZLFile containerInfoFile(zipDir->itemPath("META-INF/container.xml"));
	if (containerInfoFile.exists()) {
		ZLLogger::Instance().println("epub", "Found container file " + containerInfoFile.path());
		ContainerFileReader reader;
		reader.readDocument(containerInfoFile);
		const std::string &opfPath = reader.rootPath();
		ZLLogger::Instance().println("epub", "opf path = " + opfPath);
		if (!opfPath.empty()) {
			return ZLFile(zipDir->itemPath(opfPath));
		}
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

bool OEBPlugin::readMetainfo(Book &book) const {
	const ZLFile &file = book.file();
	return OEBMetaInfoReader(book).readMetainfo(opfFile(file));
}

std::vector<shared_ptr<FileEncryptionInfo> > OEBPlugin::readEncryptionInfos(Book &book) const {
	const ZLFile &opf = opfFile(book.file());
	return OEBEncryptionReader().readEncryptionInfos(epubFile(opf), opf);
}

bool OEBPlugin::readUids(Book &book) const {
	const ZLFile &file = book.file();
	return OEBUidReader(book).readUids(opfFile(file));
}

bool OEBPlugin::readModel(BookModel &model) const {
	const ZLFile &file = model.book()->file();
	return OEBBookReader(model).readBook(opfFile(file));
}

shared_ptr<const ZLImage> OEBPlugin::coverImage(const ZLFile &file) const {
	return OEBCoverReader().readCover(opfFile(file));
}

bool OEBPlugin::readLanguageAndEncoding(Book &book) const {
	if (book.language().empty()) {
		shared_ptr<ZLInputStream> oebStream = new OEBTextStream(opfFile(book.file()));
		detectLanguage(book, *oebStream, book.encoding());
	}
	return true;
}
