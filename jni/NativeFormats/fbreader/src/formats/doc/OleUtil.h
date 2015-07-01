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

#ifndef __OLEUTIL_H__
#define __OLEUTIL_H__

class OleUtil {
public:
	static int get4Bytes(const char *buffer, unsigned int offset);
	static unsigned int getU4Bytes(const char *buffer, unsigned int offset);
	static unsigned int getU2Bytes(const char *buffer, unsigned int offset);
	static unsigned int getU1Byte(const char *buffer, unsigned int offset);
	static int get1Byte(const char *buffer, unsigned int offset);
};

#endif /* __OLEUTIL_H__ */
