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

#ifndef __ZLBOOLEAN3_H__
#define __ZLBOOLEAN3_H__

enum ZLBoolean3 {
	B3_FALSE = 0,
	B3_TRUE = 1,
	B3_UNDEFINED = 2
};

inline ZLBoolean3 b3Value(bool value) {
	return value ? B3_TRUE : B3_FALSE;
}

inline bool boolValue(ZLBoolean3 value, bool defaultValue) {
	switch (value) {
		default:
			return defaultValue;
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
	}
}

#endif /* __ZLBOOLEAN3_H__ */
