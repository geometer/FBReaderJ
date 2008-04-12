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

package org.geometerplus.zlibrary.core.filesystem;

import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

class ZLTarDir extends ZLDir {
	ZLTarDir(String path) {		
		super(path);
	}

	public String getDelimiter() {
		return ":";
	};
	
	private static ArrayList EMPTY = new ArrayList();
	public ArrayList collectSubDirs() {
		return EMPTY;
	};
	
	public ArrayList collectFiles() {		
		ArrayList names = new ArrayList();

		try {
			InputStream stream = new ZLFile(getPath()).getInputStream();
			if (stream != null) {
				ZLTarHeader header = new ZLTarHeader();
				while (header.read(stream)) {
					if (header.IsRegularFile) {
						names.add(header.Name);
					}
					final int lenToSkip = (header.Size + 0x1ff) & -0x200;
					if (lenToSkip < 0) {
						break;
					}
					if (stream.skip(lenToSkip) != lenToSkip) {
						break;
					}
					header.erase();
				}
			}
		} catch (IOException e) {
		}

		return names;
	};
}
