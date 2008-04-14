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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PdbUtil {
	public static short readShort(InputStream stream) {
		byte[] tmp = new byte[2];
		try {
			stream.read(tmp, 0, 2);
		} catch (IOException e) {
			return -1;
		}
		return (short)(tmp[1] + (tmp[0] << 8));
	}

	public static long readUnsignedLong(InputStream stream) {
		byte []readBuffer = new byte[8];
		try {
			stream.read(readBuffer, 0, 8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
          ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
		
		/*byte []tmp = new byte[4];
		try {
			stream.read(tmp, 0, 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp[3] + tmp[2]*256 +tmp[1] * 256^2 +tmp[0] * 256^3;*/
	}
}
