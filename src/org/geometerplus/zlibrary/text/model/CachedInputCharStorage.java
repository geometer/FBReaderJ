/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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
import java.util.*;

public final class CachedInputCharStorage implements CharStorage {
	private final ArrayList<WeakReference<char[]>> myArray = new ArrayList<WeakReference<char[]>>();
	private final String myDirectoryName;
	private final String myFileExtension;

	public CachedInputCharStorage(String directoryName, String fileExtension, int blocksNumber) {
		myDirectoryName = directoryName + '/';
		myFileExtension = '.' + fileExtension;
		myArray.addAll(Collections.nCopies(blocksNumber, new WeakReference<char[]>(null)));
	}

	public String fileName(int index) {
		return myDirectoryName + index + myFileExtension;
	}

	public int size() {
		return myArray.size();
	}

	public char[] block(int index) {
		char[] block = myArray.get(index).get();
		if (block == null) {
			try {
				File file = new File(fileName(index));
				int size = (int)file.length();
				if (size < 0) {
					throw new CachedCharStorageException("Error during reading " + fileName(index));
				}
				block = new char[size / 2];
				InputStreamReader reader =
					new InputStreamReader(
						new FileInputStream(file),
						"UTF-16LE"
					);
				if (reader.read(block) != block.length) {
					throw new CachedCharStorageException("Error during reading " + fileName(index));
				}
				reader.close();
			} catch (FileNotFoundException e) {
				throw new CachedCharStorageException("Error during reading " + fileName(index));
			} catch (IOException e) {
				throw new CachedCharStorageException("Error during reading " + fileName(index));
			}
			myArray.set(index, new WeakReference<char[]>(block));
		}
		return block;
	}

	public char[] createNewBlock(int minimumLength) {
		throw new UnsupportedOperationException("CachedInputCharStorage is immutable storage.");
	}

	public void freezeLastBlock() {
	}
}
