/*
 * Copyright (C) 2008-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __XMLTEXTSTREAM_H__
#define __XMLTEXTSTREAM_H__

#include <shared_ptr.h>
#include <ZLInputStream.h>
#include <ZLAsynchronousInputStream.h>

class XMLTextReader;

class XMLTextStream : public ZLInputStream {

public:
	XMLTextStream(shared_ptr<ZLInputStream> base, const std::string &startTag);
	~XMLTextStream();

private:
	bool open();
	std::size_t read(char *buffer, std::size_t maxSize);
	void close();
	void seek(int offset, bool absoluteOffset);
	std::size_t offset() const;
	std::size_t sizeOfOpened();

private:
	shared_ptr<ZLInputStream> myBase;
	shared_ptr<XMLTextReader> myReader;
	shared_ptr<ZLAsynchronousInputStream> myStream;
	std::string myStreamBuffer;
	std::string myDataBuffer;
	std::size_t myOffset;
};

#endif /* __XMLTEXTSTREAM_H__ */
