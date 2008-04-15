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

public class PdbHeader {
	static public String DocName;
	static public int Flags;
	static public String Id;
	static public int[] Offsets;

	public boolean read(InputStream stream) throws IOException {
		final byte[] buffer = new byte[32];
		if (stream.read(buffer, 0, 32) != 32) {
			System.err.println("way 0");
			return false;
		}
		DocName = new String(buffer);
		Flags = PdbUtil.readShort(stream);

		stream.skip(26);
		
		if (stream.read(buffer, 0, 8) != 8) {
			System.err.println("way 1");
			return false;
		}
		Id = new String(buffer, 0, 8);

		stream.skip(8);

		int numRecords = PdbUtil.readShort(stream);
		if (numRecords <= 0) {
			System.err.println(numRecords);
			System.err.println("way 2");
			return false;
		}
		Offsets = new int[numRecords];

		for (int i = 0; i < numRecords; ++i) {
			Offsets[i] = PdbUtil.readInt(stream);
			if (stream.skip(4) != 4) {
			System.err.println("way 3");
				return false;
			}
		}

		return true;
	}
}
