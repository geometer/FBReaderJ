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

#include <cstring>

#include <ZLLogger.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>

#include "ZLFile.h"
#include "ZLFSDir.h"
#include "ZLOutputStream.h"
#include "zip/ZLZip.h"
//#include "tar/ZLTar.h"
//#include "bzip2/ZLBzip2InputStream.h"
#include "ZLFSManager.h"

const ZLFile ZLFile::NO_FILE;

ZLFile::ZLFile() : myMimeTypeIsUpToDate(true), myInfoIsFilled(true) {
}

ZLFile::ZLFile(const std::string &path, const std::string &mimeType) : myPath(path), myMimeType(mimeType), myMimeTypeIsUpToDate(!mimeType.empty()), myInfoIsFilled(false) {
	ZLFSManager::Instance().normalize(myPath);
	{
		std::size_t index = ZLFSManager::Instance().findLastFileNameDelimiter(myPath);
		if (index < myPath.length() - 1) {
			myNameWithExtension = myPath.substr(index + 1);
		} else {
			myNameWithExtension = myPath;
		}
	}
	myNameWithoutExtension = myNameWithExtension;

	std::map<std::string,ArchiveType> &forcedFiles = ZLFSManager::Instance().myForcedFiles;
	std::map<std::string,ArchiveType>::iterator it = forcedFiles.find(myPath);
	if (it != forcedFiles.end()) {
		myArchiveType = it->second;
	} else {
		myArchiveType = NONE;
		std::string lowerCaseName = ZLUnicodeUtil::toLower(myNameWithoutExtension);

		if (ZLStringUtil::stringEndsWith(lowerCaseName, ".gz")) {
			myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 3);
			lowerCaseName = lowerCaseName.substr(0, lowerCaseName.length() - 3);
			myArchiveType = (ArchiveType)(myArchiveType | GZIP);
		}
		/*if (ZLStringUtil::stringEndsWith(lowerCaseName, ".bz2")) {
			myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 4);
			lowerCaseName = lowerCaseName.substr(0, lowerCaseName.length() - 4);
			myArchiveType = (ArchiveType)(myArchiveType | BZIP2);
		}*/
		if (ZLStringUtil::stringEndsWith(lowerCaseName, ".zip")) {
			myArchiveType = (ArchiveType)(myArchiveType | ZIP);
		} /*else if (ZLStringUtil::stringEndsWith(lowerCaseName, ".tar")) {
			myArchiveType = (ArchiveType)(myArchiveType | TAR);
		} else if (ZLStringUtil::stringEndsWith(lowerCaseName, ".tgz") ||
							 ZLStringUtil::stringEndsWith(lowerCaseName, ".ipk")) {
			//myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 3) + "tar";
			myArchiveType = (ArchiveType)(myArchiveType | TAR | GZIP);
		}*/
	}

	int index = myNameWithoutExtension.rfind('.');
	if (index > 0) {
		myExtension = ZLUnicodeUtil::toLower(myNameWithoutExtension.substr(index + 1));
		myNameWithoutExtension = myNameWithoutExtension.substr(0, index);
	}
}

shared_ptr<ZLInputStream> ZLFile::envelopeCompressedStream(shared_ptr<ZLInputStream> &base) const {
	if (base != 0) {
		if (myArchiveType & GZIP) {
			return new ZLGzipInputStream(base);
		}
		/*if (myArchiveType & BZIP2) {
			return new ZLBzip2InputStream(base);
		}*/
	}
	return base;
}

shared_ptr<ZLInputStream> ZLFile::inputStream(shared_ptr<EncryptionMap> encryptionMap) const {
	shared_ptr<FileEncryptionInfo> encryptionInfo =
		encryptionMap.isNull() ? 0 : encryptionMap->info(myPath);
	if (!encryptionInfo.isNull()) {
		return 0;
	}

	shared_ptr<ZLInputStream> stream;

	int index = ZLFSManager::Instance().findArchiveFileNameDelimiter(myPath);
	if (index == -1) {
		if (isDirectory()) {
			return 0;
		}
		stream = ZLFSManager::Instance().createPlainInputStream(myPath);
	} else {
		const std::string baseName = myPath.substr(0, index);
		const ZLFile baseFile(baseName);
		shared_ptr<ZLInputStream> base = baseFile.inputStream();
		if (!base.isNull()) {
			if (baseFile.myArchiveType & ZIP) {
				stream = new ZLZipInputStream(base, baseName, myPath.substr(index + 1));
			/*} else if (baseFile.myArchiveType & TAR) {
				stream = new ZLTarInputStream(base, myPath.substr(index + 1));*/
			} else {
				if (isDirectory()) {
					return 0;
				}
				stream = ZLFSManager::Instance().createPlainInputStream(myPath);
			}
		}
	}
	return envelopeCompressedStream(stream);
}

shared_ptr<ZLOutputStream> ZLFile::outputStream(bool writeThrough) const {
	if (!writeThrough && isCompressed()) {
		return 0;
	}
	if (ZLFSManager::Instance().findArchiveFileNameDelimiter(myPath) != -1) {
		return 0;
	}
	return ZLFSManager::Instance().createOutputStream(myPath);
}

shared_ptr<ZLDir> ZLFile::directory(bool createUnexisting) const {
	if (exists()) {
		if (isDirectory()) {
			return ZLFSManager::Instance().createPlainDirectory(myPath);
		} else if (myArchiveType & ZIP) {
			return new ZLZipDir(myPath);
		} /*else if (myArchiveType & TAR) {
			return new ZLTarDir(myPath);
		}*/
	} else if (createUnexisting) {
		myInfoIsFilled = false;
		return ZLFSManager::Instance().createNewDirectory(myPath);
	}
	return 0;
}

void ZLFile::fillInfo() const {
	myInfoIsFilled = true;

	int index = ZLFSManager::Instance().findArchiveFileNameDelimiter(myPath);
	if (index == -1) {
		myInfo = ZLFSManager::Instance().fileInfo(myPath);
	} else {
		const std::string archivePath = myPath.substr(0, index);
		ZLFile archive(archivePath);
		if (!archive.isArchive()) {
			myInfo = ZLFSManager::Instance().fileInfo(myPath);
		} else {
			if (archive.exists()) {
				shared_ptr<ZLDir> dir = archive.directory();
				if (!dir.isNull()) {
					std::string itemName = myPath.substr(index + 1);
					myInfo = archive.myInfo;
					myInfo.IsDirectory = false;
					myInfo.Exists = false;
					std::vector<std::string> items;
					dir->collectFiles(items, true);
					for (std::vector<std::string>::const_iterator it = items.begin(); it != items.end(); ++it) {
						if (*it == itemName) {
							myInfo.Exists = true;
							break;
						}
					}
				} else {
					myInfo.Exists = false;
				}
			} else {
				myInfo.Exists = false;
			}
		}
	}
}

bool ZLFile::remove() const {
	if (ZLFSManager::Instance().removeFile(myPath)) {
		myInfoIsFilled = false;
		return true;
	} else {
		return false;
	}
}

void ZLFile::forceArchiveType(ArchiveType type) const {
	if (myArchiveType != type) {
		myArchiveType = type;
		ZLFSManager::Instance().myForcedFiles[myPath] = myArchiveType;
	}
}

std::string ZLFile::physicalFilePath() const {
	std::string path = myPath;
	int index;
	const ZLFSManager &manager = ZLFSManager::Instance();
	while ((index = manager.findArchiveFileNameDelimiter(path)) != -1) {
		path = path.substr(0, index);
	}
	return path;
}

std::string ZLFile::resolvedPath() const {
	std::string physical = physicalFilePath();
	std::string postfix = myPath.substr(physical.length());
	return ZLFSManager::Instance().resolveSymlink(physical) + postfix;
}

std::string ZLFile::fileNameToUtf8(const std::string &fileName) {
	return ZLFSManager::Instance().convertFilenameToUtf8(fileName);
}

bool ZLFile::exists() const {
	if (!myInfoIsFilled) {
		fillInfo();
	}
	return myInfo.Exists;
}

std::size_t ZLFile::size() const {
	if (!myInfoIsFilled) {
		fillInfo();
	}
	return myInfo.Size;
}

std::size_t ZLFile::lastModified() const {
	if (!myInfoIsFilled) {
		fillInfo();
	}
	return myInfo.MTime;
}

bool ZLFile::isDirectory() const {
	if (!myInfoIsFilled) {
		fillInfo();
	}
	return myInfo.IsDirectory;
}

ZLFile ZLFile::getContainerArchive() const {
	const int index = ZLFSManager::Instance().findArchiveFileNameDelimiter(myPath);
	if (index == -1) {
		return NO_FILE;
	}
	return ZLFile(myPath.substr(0, index));
}

const std::string &ZLFile::mimeType() const {
	if (!myMimeTypeIsUpToDate) {
		myMimeType = ZLFSManager::Instance().mimeType(myPath);
		myMimeTypeIsUpToDate = true;
	}
	return myMimeType;
}

bool ZLFile::canRemove() const {
	return ZLFSManager::Instance().canRemoveFile(path());
}

std::string ZLFile::replaceIllegalCharacters(const std::string &fileName, char replaceWith) {
	static const char charsToReplace[] = ":;<|>+\\/\"*?";
	const std::size_t len = fileName.length();
	char *data = new char[len];
	std::memcpy(data, fileName.data(), len);
	char *end = data + len;
	for (char *ptr = data; ptr != end; ++ptr) {
		if (strchr(charsToReplace, *ptr) != 0) {
			*ptr = replaceWith;
		}
	}
	std::string result(data, len);
	delete[] data;
	return result;
}
