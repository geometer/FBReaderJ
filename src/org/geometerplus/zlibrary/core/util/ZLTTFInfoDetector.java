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

import java.util.Map;
import java.util.HashMap;
import java.io.*;

public class ZLTTFInfoDetector {
	public Map<String,File[]> collectFonts(File[] files) {
		final HashMap<String,File[]> fonts = new HashMap<String,File[]>();
		if (files == null) {
			return fonts;
		}

		for (File f : files) {
			try {
				final ZLTTFInfo info = detectInfo(f);
				if (info != null && info.FamilyName != null) {
					File[] table = fonts.get(info.FamilyName);
					if (table == null) {
						table = new File[4];
						fonts.put(info.FamilyName, table);
					}
					if ("bold".equalsIgnoreCase(info.SubFamilyName)) {
						table[1] = f;
					} else if ("italic".equalsIgnoreCase(info.SubFamilyName) ||
							   "oblique".equalsIgnoreCase(info.SubFamilyName)) {
						table[2] = f;
					} else if ("bold italic".equalsIgnoreCase(info.SubFamilyName) ||
							   "bold oblique".equalsIgnoreCase(info.SubFamilyName)) {
						table[3] = f;
					} else {
						table[0] = f;
					}
				}
			} catch (IOException e) {
			}
		}
		return fonts;
	}

	public ZLTTFInfo detectInfo(File file) throws IOException {
		myStream = new FileInputStream(file);
		myPosition = 0;

		final byte[] subtable = new byte[12];
		myPosition += myStream.read(subtable);
		
		final int numTables = getInt16(subtable, 4);
		final byte[] tables = new byte[16 * numTables];
		myPosition += myStream.read(tables);

		TableInfo nameInfo = null;
		for (int i = 0; i < numTables; ++i) {
			if ("name".equals(new String(tables, i * 16, 4, "ascii"))) {
				nameInfo = new TableInfo(tables, i * 16);
				break;
			}
		}
		if (nameInfo == null) {
			return null;
		}
		return readFontInfo(nameInfo);
	}

	private static int getInt16(byte[] buffer, int offset) {
		return
			((buffer[offset] & 0xFF) << 8) +
			 (buffer[offset + 1] & 0xFF);
	}

	private static int getInt32(byte[] buffer, int offset) {
		if (offset <= buffer.length - 4) {
			return
				((buffer[offset++] & 0xFF) << 24) +
				((buffer[offset++] & 0xFF) << 16) +
				((buffer[offset++] & 0xFF) << 8) +
				 (buffer[offset++] & 0xFF);
		} else {
			int result = 0;
			for (int i = 0; i < 4; ++i) {
				result += offset < buffer.length ? (buffer[offset++] & 0xFF) : 0;
				result <<= 8;
			}
			return result;
		}
	}

	private InputStream myStream;
	private int myPosition;

	private static class TableInfo {
		final String Name;
		//final int CheckSum;
		final int Offset;
		final int Length;

		TableInfo(byte[] buffer, int off) throws IOException {
			Name = new String(buffer, off, 4, "ascii");
			//CheckSum = getInt32(buffer, off + 4);
			Offset = getInt32(buffer, off + 8);
			Length = getInt32(buffer, off + 12);
		}

		/*void print(PrintStream writer) {
			writer.println(Name + " : " + Offset + " : " + Length + " : " + CheckSum);
		}*/
	}

	byte[] readTable(TableInfo info) throws IOException {
		myPosition += (int)myStream.skip(info.Offset - myPosition);
		byte[] buffer = new byte[info.Length];
		while (myPosition < info.Offset) {
			int len = myStream.read(buffer, 0, Math.min(info.Offset - myPosition, info.Length));
			if (len <= 0) {
				throw new IOException("Table " + info.Name + " not found in TTF file");
			}
			myPosition += len;
		}
		myPosition += myStream.read(buffer);
		/*
		int sum = 0;
		for (int i = 0; i < info.Length; i += 4) {
			sum += getInt32(buffer, i);
		}
		if (info.CheckSum != sum) {
			//System.out.println(info.Length + ":" + info.CheckSum + ":" + sum);
			//throw new IOException("Checksum for table " + info.Name + " is not correct");
		}
		*/
		return buffer;
	}

	private ZLTTFInfo readFontInfo(TableInfo nameInfo) throws IOException {
		if (nameInfo == null || nameInfo.Offset < myPosition || nameInfo.Length <= 0) {
			return null;
		}
		byte[] buffer;
		try {
			buffer = readTable(nameInfo);
		} catch (Throwable e) {
			return null;
		} 
		if (getInt16(buffer, 0) != 0) {
			throw new IOException("Name table format is invalid");
		}
		final int count = Math.min(getInt16(buffer, 2), (buffer.length - 6) / 12);
		final int stringOffset = getInt16(buffer, 4);
		final ZLTTFInfo fontInfo = new ZLTTFInfo();
		for (int i = 0; i < count; ++i) {
			final int platformId = getInt16(buffer, 12 * i + 6);
			//final int platformSpecificId = getInt16(buffer, 12 * i + 8);
			final int languageId = getInt16(buffer, 12 * i + 10);
			final int nameId = getInt16(buffer, 12 * i + 12);
			final int length = getInt16(buffer, 12 * i + 14);
			final int offset = getInt16(buffer, 12 * i + 16);
			switch (nameId) {
				case 1:
					if ((fontInfo.FamilyName == null || languageId == 1033) &&
						stringOffset + offset + length <= buffer.length) {
						fontInfo.FamilyName = new String(
							buffer, stringOffset + offset, length,
							platformId == 1 ? "windows-1252" : "UTF-16BE"
						);
					}
					break;
				case 2:
					if ((fontInfo.FamilyName == null || languageId == 1033) &&
						stringOffset + offset + length <= buffer.length) {
						fontInfo.SubFamilyName = new String(
							buffer, stringOffset + offset, length,
							platformId == 1 ? "windows-1252" : "UTF-16BE"
						);
					}
					break;
			}
		}
		return fontInfo;
	}
}
