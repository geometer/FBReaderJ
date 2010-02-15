/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

public abstract class PdbUtil {
	public static void skip(InputStream stream, int numBytes) throws IOException {
		numBytes -= stream.skip(numBytes);
		for (; numBytes > 0; --numBytes) {
			if (stream.read() == -1) {
				throw new IOException("Unexpected end of stream");
			}
		}
	}

	public static int readShort(InputStream stream) throws IOException {
		final byte[] tmp = new byte[2];
		stream.read(tmp, 0, 2);
		return (tmp[1] & 0xFF) + ((tmp[0] & 0xFF) << 8);
	}

	public static long readInt(InputStream stream) throws IOException {
		final byte[] tmp = new byte[4];
		stream.read(tmp, 0, 4);
		return (((long)(tmp[0] & 0xFF)) << 24) +
			  + ((tmp[1] & 0xFF) << 16) +
			  + ((tmp[2] & 0xFF) << 8) +
			  + (tmp[3] & 0xFF);
	}
}
