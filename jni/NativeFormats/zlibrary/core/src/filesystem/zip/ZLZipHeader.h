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

#ifndef __ZLZIPHEADER_H__
#define __ZLZIPHEADER_H__

class ZLInputStream;

struct ZLZipHeader {
	static const int SignatureLocalFile;
	static const int SignatureData;
	static const int SignatureCentralDirectory;
	static const int SignatureEndOfCentralDirectory;

	unsigned long Signature;
	unsigned short Version;
	unsigned short Flags;
	unsigned short CompressionMethod;
	unsigned short ModificationTime;
	unsigned short ModificationDate;
	unsigned long CRC32;
	unsigned long CompressedSize;
	unsigned long UncompressedSize;
	unsigned short NameLength;
	unsigned short ExtraLength;

	bool readFrom(ZLInputStream &stream);
	static void skipEntry(ZLInputStream &stream, ZLZipHeader &header);

private:
	unsigned short readShort(ZLInputStream &stream);
	unsigned long readLong(ZLInputStream &stream);
};

#endif /* __ZLZIPHEADER_H__ */
