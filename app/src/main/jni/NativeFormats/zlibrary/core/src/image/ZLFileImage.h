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

#ifndef __ZLFILEIMAGE_H__
#define __ZLFILEIMAGE_H__

#include <vector>

#include <shared_ptr.h>
#include <ZLFile.h>
#include <FileEncryptionInfo.h>

#include "ZLImage.h"

class ZLFileImage : public ZLSingleImage {

public:
	struct Block {
		unsigned int offset;
		unsigned int size;

		Block(unsigned int off, unsigned int s);
	};
	typedef std::vector<Block> Blocks;

public:
	ZLFileImage(const ZLFile &file, const std::string &encoding, std::size_t offset, std::size_t size = 0, shared_ptr<FileEncryptionInfo> encryptionInfo = 0);
	ZLFileImage(const ZLFile &file, const std::string &encoding, const Blocks &blocks);

	//Kind kind() const;
	const ZLFile &file() const;
	const std::string &encoding() const;
	shared_ptr<FileEncryptionInfo> encryptionInfo() const;
	const ZLFileImage::Blocks& blocks() const;

protected:
	//shared_ptr<ZLInputStream> inputStream() const;

private:
	const ZLFile myFile;
	const std::string myEncoding;
	shared_ptr<FileEncryptionInfo> myEncryptionInfo;
	Blocks myBlocks;
};

inline ZLFileImage::Block::Block(unsigned int off, unsigned int s) : offset(off), size(s) {}

inline ZLFileImage::ZLFileImage(const ZLFile &file, const std::string &encoding, std::size_t offset, std::size_t size, shared_ptr<FileEncryptionInfo> encryptionInfo) : ZLSingleImage(file.mimeType()), myFile(file), myEncoding(encoding), myEncryptionInfo(encryptionInfo) {
	myBlocks.push_back(Block(offset, size));
}

inline ZLFileImage::ZLFileImage(const ZLFile &file, const std::string &encoding, const ZLFileImage::Blocks &blocks) : ZLSingleImage(file.mimeType()), myFile(file), myEncoding(encoding), myBlocks(blocks) { }

//inline ZLSingleImage::Kind ZLFileImage::kind() const { return FILE_IMAGE; }
inline const ZLFile &ZLFileImage::file() const { return myFile; }
inline const std::string &ZLFileImage::encoding() const { return myEncoding; }
inline shared_ptr<FileEncryptionInfo> ZLFileImage::encryptionInfo() const { return myEncryptionInfo; }
inline const ZLFileImage::Blocks &ZLFileImage::blocks() const { return myBlocks; }
//inline shared_ptr<ZLInputStream> ZLFileImage::inputStream() const { return myFile.inputStream(); }

#endif /* __ZLFILEIMAGE_H__ */
