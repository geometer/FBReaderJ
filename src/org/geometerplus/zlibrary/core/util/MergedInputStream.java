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

public class MergedInputStream extends InputStream {
	private final InputStream[] myStreams;
	private InputStream myCurrentStream;
	private int myCurrentStreamNumber;

	public MergedInputStream(InputStream[] streams) throws IOException {
		myStreams = streams;
		myCurrentStream = streams[0];
		myCurrentStreamNumber = 0;
	}

	@Override
	public int read() throws IOException {
		int readed = -1;
		boolean streamIsAvailable = true;
		while (readed == -1 && streamIsAvailable) {
			readed = myCurrentStream.read();
			if (readed == -1) {
				streamIsAvailable = nextStream();
			}
		}
		return readed;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesToRead = len;
		int bytesReaded = 0;
		boolean streamIsAvailable = true;
		while (bytesToRead > 0 && streamIsAvailable) {
			int readed = myCurrentStream.read(b, off, bytesToRead);
			if (readed != -1) {
				bytesToRead -= readed;
				off += readed;
				bytesReaded += readed;
			}
			if (bytesToRead != 0) {
				streamIsAvailable = nextStream();
			}
		}
		return bytesReaded == 0 ? -1 : bytesReaded;
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = myCurrentStream.skip(n);
		boolean streamIsAvailable = true;
		while (skipped < n && streamIsAvailable) {
			streamIsAvailable = nextStream();
			if (streamIsAvailable) {
				skipped += myCurrentStream.skip(n - skipped);
			}
		}
		return skipped;
	}

	@Override
	public int available() throws IOException {
		int total = 0;
		for (int i = myCurrentStreamNumber; i < myStreams.length; ++i) {
			total += myStreams[i].available();
		}
		return total;
	}

	private boolean nextStream() {
		if (myCurrentStreamNumber + 1 >= myStreams.length) {
			return false;
		}
		++myCurrentStreamNumber;
		myCurrentStream = myStreams[myCurrentStreamNumber];
		return true;
	}
}
