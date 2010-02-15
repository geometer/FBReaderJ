/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

class MobipocketStream extends PalmDocLikeStream {
	private final int myFileSize;

	MobipocketStream(ZLFile file) throws IOException {
		super(file);
		myFileSize = (int)file.size();

		myCompressionType = PdbUtil.readShort(myBase);
		PdbUtil.skip(myBase, 6);
		myMaxRecordIndex = Math.min(PdbUtil.readShort(myBase), myHeader.Offsets.length - 1);
		final int maxRecordSize = PdbUtil.readShort(myBase);
		if (maxRecordSize == 0) {
			throw new IOException("The records are too short");
		}
		myBuffer = new byte[maxRecordSize];
		myRecordIndex = 0;
	}

	int getImageOffset(int index) {
		try {
			return myHeader.Offsets[index + myMaxRecordIndex + 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}

	int getImageLength(int index) {
		try {
			final int i = index + myMaxRecordIndex + 1;
			final int start = myHeader.Offsets[i];
			final int end = (i == myHeader.Offsets.length) ? myFileSize : myHeader.Offsets[i + 1];
			return end - start;
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}
}
