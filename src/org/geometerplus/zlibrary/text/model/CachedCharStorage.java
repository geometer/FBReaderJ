/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.model;

import java.lang.ref.WeakReference;
import java.io.*;

public final class CachedCharStorage extends CachedCharStorageBase {
	private final int myBlockSize;

	public CachedCharStorage(int blockSize, String directoryName, String fileExtension) {
		super(directoryName, fileExtension);
		myBlockSize = blockSize;
		new File(directoryName).mkdirs();
	}

	public char[] createNewBlock(int minimumLength) {
		int blockSize = myBlockSize;
		if (minimumLength > blockSize) {
			blockSize = minimumLength;
		}
		char[] block = new char[blockSize];
		myArray.add(new WeakReference<char[]>(block));
		return block;
	}

	public void freezeLastBlock() {
		int index = myArray.size() - 1;
		if (index >= 0) {
			char[] block = myArray.get(index).get();
			if (block == null) {
				throw new CachedCharStorageException("Block reference in null during freeze");
			}
			try {
				final OutputStreamWriter writer =
					new OutputStreamWriter(
						new FileOutputStream(fileName(index)),
						"UTF-16LE"
					);
				writer.write(block);
				writer.close();
			} catch (IOException e) {
				throw new CachedCharStorageException("Error during writing " + fileName(index));
			}
		}
	}
}
