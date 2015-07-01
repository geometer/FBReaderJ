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

#ifndef __ZLUNIXFILEOUTPUTSTREAM_H__
#define __ZLUNIXFILEOUTPUTSTREAM_H__

#include <stdio.h>

#include <ZLOutputStream.h>

class ZLUnixFileOutputStream : public ZLOutputStream {

public:
	ZLUnixFileOutputStream(const std::string &name);
	~ZLUnixFileOutputStream();
	bool open();
	void write(const char *data, std::size_t len);
	void write(const std::string &str);
	void close();

private:
	std::string myName;
	std::string myTemporaryName;
	bool myHasErrors;
	FILE *myFile;
};

#endif /* __ZLUNIXFILEOUTPUTSTREAM_H__ */
