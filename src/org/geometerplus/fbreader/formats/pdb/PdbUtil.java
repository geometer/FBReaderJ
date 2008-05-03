/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.pdb;

import java.io.*;

public class PdbUtil {
	public static int readShort(InputStream stream) {
		final byte[] tmp = new byte[2];
		try {
			stream.read(tmp, 0, 2);
		} catch (IOException e) {
			return -1;
		}
	//	int i = ((tmp[1] & 0xFF) + ((tmp[0] & 0xFF) << 8));
	//	if (i > Short.MAX_VALUE)
	//	System.out.println("i = " + i);
		return ((tmp[1] & 0xFF) + ((tmp[0] & 0xFF) << 8));
	}

	//? long
	public static int readInt(InputStream stream) {
		final byte[] tmp = new byte[4];
		try {
			stream.read(tmp, 0, 4);
		} catch (IOException e) {
			return -1;
		}
		return  (tmp[0] << 24) +
					 ((tmp[1] & 0xFF) << 16) +
					 ((tmp[2] & 0xFF) << 8) +
						(tmp[3] & 0xFF);
	}
}
