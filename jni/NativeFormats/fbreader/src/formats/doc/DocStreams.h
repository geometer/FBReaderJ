/*
 * Copyright (C) 2008-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __DOCSTREAMS_H__
#define __DOCSTREAMS_H__

#include <ZLFile.h>
#include <ZLInputStream.h>

class DocReader;

class DocStream : public ZLInputStream {

public:
	DocStream(const ZLFile& file, size_t maxSize);
	~DocStream();

private:
	bool open();
	size_t read(char *buffer, size_t maxSize);
	void close();

	void seek(int offset, bool absoluteOffset);
	size_t offset() const;
	size_t sizeOfOpened();

protected:
	virtual shared_ptr<DocReader> createReader(char *buffer, size_t maxSize) = 0;

private:
	const ZLFile myFile;
	char *myBuffer;
	size_t mySize;
	size_t myOffset;
};

class DocCharStream : public DocStream {

public:
	DocCharStream(const ZLFile& file, size_t maxSize);
	~DocCharStream();

private:
	shared_ptr<DocReader> createReader(char *buffer, size_t maxSize);
};

class DocAnsiStream : public DocStream {

public:
	DocAnsiStream(const ZLFile& file, size_t maxSize);
	~DocAnsiStream();

private:
	shared_ptr<DocReader> createReader(char *buffer, size_t maxSize);
};

#endif /* __DOCSTREAMS_H__ */
