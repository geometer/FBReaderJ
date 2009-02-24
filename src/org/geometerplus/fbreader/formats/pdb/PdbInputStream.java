/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class PdbInputStream extends InputStream {
	private final InputStream myBase;
	private int myOffset = 0;
	private final int mySize;
	
	public PdbInputStream(ZLFile file) throws IOException {
		mySize = (int)file.size();
		myBase = file.getInputStream();
	}
	
	public int read() throws IOException {
		int result = myBase.read();
		if (result != -1) {
			myOffset ++;
		}
		return result;
	}

	public int available() throws IOException {
		return super.available();
	}

	public void close() throws IOException {
		myOffset = 0;
		super.close();
	}

	public synchronized void mark(int readlimit) {
		super.mark(readlimit);
	}

	public boolean markSupported() {
		return super.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return super.read(b);
	}

	public synchronized void reset() throws IOException {
//		myOffset = 0;
		super.reset();
	}

	public int offset() {
		return myOffset;
	}
	
	public int sizeOfOpened() {
		return mySize - myOffset;
	}
}
