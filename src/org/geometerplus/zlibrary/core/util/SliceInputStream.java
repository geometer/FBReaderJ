/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

public class SliceInputStream extends ZLInputStreamWithOffset {
	private final int myStart;
	private final int myLength;
	
	public SliceInputStream(InputStream base, int start, int length) throws IOException {
		super(base);
		super.skip(start);
		myStart = start;
		myLength = length;
	}
	
	@Override
	public int available() throws IOException {
		return Math.min(super.available(), Math.max(myStart + myLength - super.offset(), 0));
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		super.skip(myStart);
	}
}
