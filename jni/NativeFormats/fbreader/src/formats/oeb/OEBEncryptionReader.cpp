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
#include <ZLXMLReader.h>

#include "../FormatPlugin.h"
#include "OEBEncryptionReader.h"

class EpubRightsFileReader : public ZLXMLReader {

public:
	EpubRightsFileReader();
	std::string method() const;

private:
	void startElementHandler(const char *tag, const char **attributes);
	bool processNamespaces() const;

private:
	std::string myMethod;
};

class EpubEncryptionFileReader : public ZLXMLReader {

public:
	EpubEncryptionFileReader();

	const std::vector<EncryptionInfo> &infos() const;

private:
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, std::size_t len);
	bool processNamespaces() const;

private:
	enum State {
		READ_SUCCESS,
		READ_NONE,
		READ_ENCRYPTION,
		READ_ENCRYPTED_DATA,
		READ_KEY_INFO,
		READ_KEY_NAME,
		READ_CIPHER_DATA
	};

private:
	std::vector<EncryptionInfo> myInfos;

	State myState;
	std::string myAlgorithm;
	std::string myKeyName;
	std::string myUri;
};

std::string OEBEncryptionReader::readEncryptionMethod(const ZLFile &epubFile) {
	shared_ptr<ZLDir> epubDir = epubFile.directory();
	if (epubDir.isNull()) {
		return FormatPlugin::EncryptionMethod::UNSUPPORTED;
	}

	const ZLFile rightsFile(epubDir->itemPath("META-INF/rights.xml"));
	const ZLFile encryptionFile(epubDir->itemPath("META-INF/encryption.xml"));
	if (!rightsFile.exists() && !encryptionFile.exists()) {
		return FormatPlugin::EncryptionMethod::NONE;
	}
	if (!rightsFile.exists() || !encryptionFile.exists()) {
		return FormatPlugin::EncryptionMethod::UNSUPPORTED;
	}

	EpubRightsFileReader reader;
	reader.readDocument(rightsFile);
	return reader.method();
}

std::vector<EncryptionInfo> OEBEncryptionReader::readEncryptionInfo(const ZLFile &epubFile) {
	const std::string method = readEncryptionMethod(epubFile);
	if (method == FormatPlugin::EncryptionMethod::MARLIN) {
		shared_ptr<ZLDir> epubDir = epubFile.directory();
		if (!epubDir.isNull()) {
			const ZLFile encryptionFile(epubDir->itemPath("META-INF/encryption.xml"));
			EpubEncryptionFileReader reader = EpubEncryptionFileReader();
			reader.readDocument(encryptionFile);
			return reader.infos();
		}
	}
	return std::vector<EncryptionInfo>();
}

EpubRightsFileReader::EpubRightsFileReader() : myMethod(FormatPlugin::EncryptionMethod::UNSUPPORTED) {
}

std::string EpubRightsFileReader::method() const {
	return myMethod;
}

void EpubRightsFileReader::startElementHandler(const char *tag, const char **attributes) {
	if (testTag(ZLXMLNamespace::MarlinEpub, "Marlin", tag)) {
		myMethod = FormatPlugin::EncryptionMethod::MARLIN;
	}
	interrupt();
}

bool EpubRightsFileReader::processNamespaces() const {
	return true;
}

EncryptionInfo::EncryptionInfo(const std::string &uri, const std::string &algorithm, const std::string &contentId) : Uri(uri), Algorithm(algorithm), ContentId(contentId) {
}

EpubEncryptionFileReader::EpubEncryptionFileReader() : myState(READ_NONE) {
}

const std::vector<EncryptionInfo> &EpubEncryptionFileReader::infos() const {
	//ZLLogger::Instance().registerClass("MARLIN");
	//for (std::vector<EncryptionInfo>::const_iterator it = myInfos.begin(); it != myInfos.end(); ++it) {
	//	ZLLogger::Instance().println("MARLIN", std::string("INFO: ") + it->Uri + " :: " + it->Algorithm + " :: " + it->ContentId);
	//}
	return myInfos;
}

void EpubEncryptionFileReader::startElementHandler(const char *tag, const char **attributes) {
	switch (myState) {
		case READ_SUCCESS:
			break;
		case READ_NONE:
			if (testTag(ZLXMLNamespace::EpubContainer, "encryption", tag)) {
				myState = READ_ENCRYPTION;
			} else {
				interrupt();
			}
			break;
		case READ_ENCRYPTION:
			if (testTag(ZLXMLNamespace::XMLEncryption, "EncryptedData", tag)) {
				myState = READ_ENCRYPTED_DATA;
			} else {
				interrupt();
			}
			break;
		case READ_ENCRYPTED_DATA:
			if (testTag(ZLXMLNamespace::XMLEncryption, "EncryptionMethod", tag)) {
				const char *algorithm = attributeValue(attributes, "Algorithm");
				if (algorithm != 0) {
					myAlgorithm = algorithm;
				} else {
					interrupt();
				}
			} else if (testTag(ZLXMLNamespace::XMLDigitalSignature, "KeyInfo", tag)) {
				myState = READ_KEY_INFO;
			} else if (testTag(ZLXMLNamespace::XMLEncryption, "CipherData", tag)) {
				myState = READ_CIPHER_DATA;
			} else {
				interrupt();
			}
			break;
		case READ_KEY_INFO:
			if (testTag(ZLXMLNamespace::XMLDigitalSignature, "KeyName", tag)) {
				myState = READ_KEY_NAME;
				myKeyName.clear();
			} else {
				interrupt();
			}
			break;
		case READ_KEY_NAME:
			interrupt();
			break;
		case READ_CIPHER_DATA:
			if (testTag(ZLXMLNamespace::XMLEncryption, "CipherReference", tag)) {
				const char *uri = attributeValue(attributes, "URI");
				if (uri != 0) {
					myUri = uri;
				} else {
					interrupt();
				}
			} else {
				interrupt();
			}
			break;
	}
}

void EpubEncryptionFileReader::endElementHandler(const char *tag) {
	switch (myState) {
		case READ_NONE:
		case READ_SUCCESS:
			break;
		case READ_ENCRYPTION:
			if (testTag(ZLXMLNamespace::EpubContainer, "encryption", tag)) {
				myState = READ_SUCCESS;
			}
			break;
		case READ_ENCRYPTED_DATA:
			if (testTag(ZLXMLNamespace::XMLEncryption, "EncryptedData", tag)) {
				myInfos.push_back(EncryptionInfo(myUri, myAlgorithm, myKeyName));
				myState = READ_ENCRYPTION;
			}
			break;
		case READ_KEY_INFO:
			if (testTag(ZLXMLNamespace::XMLDigitalSignature, "KeyInfo", tag)) {
				myState = READ_ENCRYPTED_DATA;
			}
			break;
		case READ_KEY_NAME:
			if (testTag(ZLXMLNamespace::XMLDigitalSignature, "KeyName", tag)) {
				myState = READ_KEY_INFO;
			}
			break;
		case READ_CIPHER_DATA:
			if (testTag(ZLXMLNamespace::XMLEncryption, "CipherData", tag)) {
				myState = READ_ENCRYPTED_DATA;
			}
			break;
	}
}

void EpubEncryptionFileReader::characterDataHandler(const char *text, std::size_t len) {
	if (myState == READ_KEY_NAME) {
		myKeyName.append(text, len);
	}
}

bool EpubEncryptionFileReader::processNamespaces() const {
	return true;
}
