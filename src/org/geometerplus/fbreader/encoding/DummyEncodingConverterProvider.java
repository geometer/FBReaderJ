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

package org.geometerplus.fbreader.encoding;

public class DummyEncodingConverterProvider extends ZLEncodingConverterProvider {

	public ZLEncodingConverter createConverter() {
		return new DummyEncodingConverter();
	}
	
	public ZLEncodingConverter createConverter(String encoding) {
		return createConverter();
	}

	public boolean providesConverter(String encoding) {
		final String lowerCasedEncoding = encoding.toLowerCase();
		return ("utf-8".equals(lowerCasedEncoding)) || ("us-ascii".equals(lowerCasedEncoding));
	}

	private static class DummyEncodingConverter extends ZLEncodingConverter {

		private DummyEncodingConverter() {}
		
		public boolean fillTable(int[] map) {
			for (int i = 0; i < 255; ++i) {
				map[i] = i;
			}
			return true;
		}

		public void reset() {}
		
		//size=end-start
		public String convert(char [] src, int start, int end) {
			return new String(src, start, end - start);
		}

	}
}
