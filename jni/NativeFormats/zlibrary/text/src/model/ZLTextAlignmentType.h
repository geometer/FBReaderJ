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

#ifndef __ZLTEXTALIGNMENTTYPE_H__
#define __ZLTEXTALIGNMENTTYPE_H__

enum ZLTextAlignmentType {
	ALIGN_UNDEFINED = 0,
	ALIGN_LEFT = 1,
	ALIGN_RIGHT = 2,
	ALIGN_CENTER = 3,
	ALIGN_JUSTIFY = 4,
	ALIGN_LINESTART = 5 // left for LTR languages and right for RTL
};

#endif /* __ZLTEXTALIGNMENTTYPE_H__ */
