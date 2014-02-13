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

#include <ZLDir.h>
#include <ZLLogger.h>
#include <ZLXMLNamespace.h>

#include "OEBEncryptionReader.h"

OEBEncryptionReader::OEBEncryptionReader() : myType("unknown") {
}

std::string OEBEncryptionReader::readEncryptionInfo(const ZLFile &epubFile) {
	shared_ptr<ZLDir> epubDir = epubFile.directory();
	if (!epubDir.isNull()) {
		const ZLFile rightsFile(epubDir->itemPath("META-INF/rights.xml"));
		if (rightsFile.exists()) {
			readDocument(rightsFile);
		} else {
			myType = "none";
		}
	}
	return myType;
}

void OEBEncryptionReader::startElementHandler(const char *tag, const char **attributes) {
	if (testTag(ZLXMLNamespace::MarlinEpub, "Marlin", tag)) {
		myType = "marlin";
	}
	interrupt();
}

bool OEBEncryptionReader::processNamespaces() const {
	return true;
}
