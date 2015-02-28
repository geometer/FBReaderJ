/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.util;

import java.io.IOException;
import java.io.InputStream;

public class SliceInputStream extends InputStreamWithOffset {
	private final int myStart;
	private final int myLength;

	public SliceInputStream(InputStream base, int start, int length) throws IOException {
		super(base);
		baseSkip(start);
		myStart = start;
		myLength = length;
	}

	@Override
	public int read() throws IOException {
		if (offset() >= myLength) {
			return -1;
		}
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		final int maxbytes = myLength - offset();
		if (maxbytes <= 0) {
			return -1;
		}
		return super.read(b, off, Math.min(len, maxbytes));
	}

	@Override
	public long skip(long n) throws IOException {
		return super.skip(Math.min(n, Math.max(myLength - offset(), 0)));
	}

	@Override
	public int available() throws IOException {
		return Math.min(super.available(), Math.max(myLength - offset(), 0));
	}

	@Override
	public int offset() {
		return super.offset() - myStart;
	}
}
