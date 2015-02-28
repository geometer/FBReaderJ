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

package org.geometerplus.zlibrary.core.encodings;

import java.nio.*;
import java.nio.charset.*;

public class EncodingConverter {
	public final String Name;
	private CharsetDecoder myDecoder;

	EncodingConverter(String encoding) {
		Name = encoding;
		myDecoder = Charset.forName(encoding).newDecoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	// we assume out is large enough for this conversion
	// returns number of filled chars in out buffer
	public int convert(byte[] in, int inOffset, int inLength, char[] out) {
		final ByteBuffer inBuffer = ByteBuffer.wrap(in, inOffset, inLength);
		final CharBuffer outBuffer = CharBuffer.wrap(out, 0, out.length);
		myDecoder.decode(inBuffer, outBuffer, false);
		return outBuffer.position();
	}

	public void reset() {
		myDecoder.reset();
	}
}
