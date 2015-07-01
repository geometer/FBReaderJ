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

#ifndef __ZLFILE_H__
#define __ZLFILE_H__

#include <string>

#include <shared_ptr.h>
#include <ZLFileInfo.h>
#include <FileEncryptionInfo.h>

class ZLDir;
class ZLInputStream;
class ZLOutputStream;

class ZLFile {

public:
	static const ZLFile NO_FILE;

public:
	static std::string fileNameToUtf8(const std::string &fileName);
	static std::string replaceIllegalCharacters(const std::string &fileName, char replaceWith);

public:
	enum ArchiveType {
		NONE = 0,
		GZIP = 0x0001,
		//BZIP2 = 0x0002,
		COMPRESSED = 0x00ff,
		ZIP = 0x0100,
		//TAR = 0x0200,
		ARCHIVE = 0xff00,
	};

private:
	ZLFile();

public:
	explicit ZLFile(const std::string &path, const std::string &mimeType = std::string());
	~ZLFile();

	bool exists() const;
	std::size_t size() const;
	std::size_t lastModified() const;

	void forceArchiveType(ArchiveType type) const;

	bool isCompressed() const;
	bool isDirectory() const;
	bool isArchive() const;

	ZLFile getContainerArchive() const;

	bool remove() const;
	bool canRemove() const;

	const std::string &path() const;
	const std::string &name(bool hideExtension) const;
	const std::string &extension() const;

	const std::string &mimeType() const;

	std::string physicalFilePath() const;
	std::string resolvedPath() const;

	shared_ptr<ZLInputStream> inputStream(shared_ptr<EncryptionMap> encryptionMap = 0) const;
	shared_ptr<ZLOutputStream> outputStream(bool writeThrough = false) const;
	shared_ptr<ZLDir> directory(bool createUnexisting = false) const;

	bool operator == (const ZLFile &other) const;
	bool operator != (const ZLFile &other) const;
	bool operator < (const ZLFile &other) const;

private:
	void fillInfo() const;
	shared_ptr<ZLInputStream> envelopeCompressedStream(shared_ptr<ZLInputStream> &base) const;

private:
	std::string myPath;
	std::string myNameWithExtension;
	std::string myNameWithoutExtension;
	std::string myExtension;
	mutable std::string myMimeType;
	mutable bool myMimeTypeIsUpToDate;
	mutable ArchiveType myArchiveType;
	mutable ZLFileInfo myInfo;
	mutable bool myInfoIsFilled;
};

inline ZLFile::~ZLFile() {}

inline bool ZLFile::isCompressed() const { return (myArchiveType & COMPRESSED) != 0; }
inline bool ZLFile::isArchive() const { return (myArchiveType & ARCHIVE) != 0; }

inline const std::string &ZLFile::path() const { return myPath; }
inline const std::string &ZLFile::name(bool hideExtension) const { return hideExtension ? myNameWithoutExtension : myNameWithExtension; }
inline const std::string &ZLFile::extension() const { return myExtension; }

inline bool ZLFile::operator == (const ZLFile &other) const { return myPath == other.myPath; }
inline bool ZLFile::operator != (const ZLFile &other) const { return myPath != other.myPath; }
inline bool ZLFile::operator < (const ZLFile &other) const { return myPath < other.myPath; }

#endif /* __ZLFILE_H__ */
