/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLIMAGEMAPWRITER_H__
#define __ZLIMAGEMAPWRITER_H__

#include <jni.h>

#include <ZLImage.h>
#include <ZLUnicodeUtil.h>

#include <ZLTextParagraph.h>
#include <ZLCachedMemoryAllocator.h>

class ZLImageMapWriter {

public:
	ZLImageMapWriter(const size_t rowSize, const std::string &directoryName, const std::string &fileExtension);

	void addImage(const std::string &id, const ZLImage &image);

	void flush();

	const std::vector<std::string> identifiers() const;
	const std::vector<jint> indices() const;
	const std::vector<jint> offsets() const;

	const ZLCachedMemoryAllocator &allocator() const;

private:
	void addSingleImageEntry(const ZLSingleImage &image);
	void addMultiImageEntry(const ZLMultiImage &image);

private:
	ZLCachedMemoryAllocator myAllocator;

	std::vector<std::string> myIds;
	std::vector<jint> myIndices;
	std::vector<jint> myOffsets;

private:
	ZLImageMapWriter(const ZLImageMapWriter &);
	const ZLImageMapWriter &operator = (const ZLImageMapWriter &);
};

inline const std::vector<std::string> ZLImageMapWriter::identifiers() const { return myIds; }
inline const std::vector<jint> ZLImageMapWriter::indices() const { return myIndices; }
inline const std::vector<jint> ZLImageMapWriter::offsets() const { return myOffsets; }
inline const ZLCachedMemoryAllocator &ZLImageMapWriter::allocator() const { return myAllocator; }

#endif /* __ZLIMAGEMAPWRITER_H__ */
