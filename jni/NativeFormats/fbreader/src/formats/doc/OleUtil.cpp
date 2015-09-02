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

#include "OleUtil.h"

int OleUtil::get4Bytes(const char *buffer, unsigned int offset) {
	const unsigned char *buf = (const unsigned char*)buffer;
	return
		   (int)buf[offset]
		| ((int)buf[offset + 1] << 8)
		| ((int)buf[offset + 2] << 16)
		| ((int)buf[offset + 3] << 24);
}

unsigned int OleUtil::getU4Bytes(const char *buffer, unsigned int offset) {
	const unsigned char *buf = (const unsigned char*)buffer;
	return
		   (unsigned int)buf[offset]
		| ((unsigned int)buf[offset + 1] << 8)
		| ((unsigned int)buf[offset + 2] << 16)
		| ((unsigned int)buf[offset + 3] << 24);
}

unsigned int OleUtil::getU2Bytes(const char *buffer, unsigned int offset) {
	const unsigned char *buf = (const unsigned char*)buffer;
	return
		   (unsigned int)buf[offset]
		| ((unsigned int)buf[offset + 1] << 8);
}

unsigned int OleUtil::getU1Byte(const char *buffer, unsigned int offset) {
	const unsigned char *buf = (const unsigned char*)buffer;
	return (unsigned int)buf[offset];
}

int OleUtil::get1Byte(const char *buffer, unsigned int offset) {
	const unsigned char *buf = (const unsigned char*)buffer;
	return (int)buf[offset];
}
