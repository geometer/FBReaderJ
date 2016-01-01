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

#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "ZLUnixFileOutputStream.h"

ZLUnixFileOutputStream::ZLUnixFileOutputStream(const std::string &name) : myName(name), myHasErrors(false), myFile(0) {
}

ZLUnixFileOutputStream::~ZLUnixFileOutputStream() {
	close();
}

bool ZLUnixFileOutputStream::open() {
	close();

	myTemporaryName = myName + ".XXXXXX" + '\0';
	mode_t currentMask = umask(S_IRWXO | S_IRWXG);
	int temporaryFileDescriptor = ::mkstemp(const_cast<char*>(myTemporaryName.data()));
	umask(currentMask);
	if (temporaryFileDescriptor == -1) {
		return false;
	}

	myFile = fdopen(temporaryFileDescriptor, "w+");
	return myFile != 0;
}

void ZLUnixFileOutputStream::write(const char *data, std::size_t len) {
	if (::fwrite(data, 1, len, myFile) != len) {
		myHasErrors = true;
	}
}

void ZLUnixFileOutputStream::write(const std::string &str) {
	if (::fwrite(str.data(), 1, str.length(), myFile) != (std::size_t)str.length()) {
		myHasErrors = true;
	}
}

void ZLUnixFileOutputStream::close() {
	if (myFile != 0) {
		::fclose(myFile);
		myFile = 0;
		if (!myHasErrors) {
			rename(myTemporaryName.c_str(), myName.c_str());
		}
	}
}
