/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#include <AndroidUtil.h>

#include "ZLZip.h"
#include "ZLZipHeader.h"

ZLZipEntryCache::Info::Info() : Offset(-1) {
}

const ZLZipEntryCache &ZLZipEntryCache::cache(ZLInputStream &stream) {
	static const std::string zipEntryMapKey = "zipEntryMap";
	shared_ptr<ZLUserData> data = stream.getUserData(zipEntryMapKey);
	if (data.isNull()) {
		data = new ZLZipEntryCache(stream);
		stream.addUserData(zipEntryMapKey, data);
	}
	return (const ZLZipEntryCache&)*data;
}

ZLZipEntryCache::ZLZipEntryCache(ZLInputStream &baseStream) {
	if (!baseStream.open()) {
		return;
	}

	ZLZipHeader header;
	while (header.readFrom(baseStream)) {
		Info *infoPtr = 0;
		if (header.Signature == (unsigned long)ZLZipHeader::SignatureLocalFile) {
			std::string entryName(header.NameLength, '\0');
			if ((unsigned int)baseStream.read((char*)entryName.data(), header.NameLength) == header.NameLength) {
				entryName = AndroidUtil::convertNonUtfString(entryName);
				Info &info = myInfoMap[entryName];
				info.Offset = baseStream.offset() + header.ExtraLength;
				info.CompressionMethod = header.CompressionMethod;
				info.CompressedSize = header.CompressedSize;
				info.UncompressedSize = header.UncompressedSize;
				infoPtr = &info;
			}
		}
		ZLZipHeader::skipEntry(baseStream, header);
		if (infoPtr != 0) {
			infoPtr->UncompressedSize = header.UncompressedSize;
		}
	}
	baseStream.close();
}

ZLZipEntryCache::Info ZLZipEntryCache::info(const std::string &entryName) const {
	std::map<std::string,Info>::const_iterator it = myInfoMap.find(entryName);
	return (it != myInfoMap.end()) ? it->second : Info();
}

void ZLZipEntryCache::collectFileNames(std::vector<std::string> &names) const {
	for (std::map<std::string,Info>::const_iterator it = myInfoMap.begin(); it != myInfoMap.end(); ++it) {
		names.push_back(it->first);
	}
}
