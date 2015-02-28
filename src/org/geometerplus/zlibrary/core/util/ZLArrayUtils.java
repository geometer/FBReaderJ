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

public abstract class ZLArrayUtils {
	public static boolean[] createCopy(boolean[] array, int dataSize, int newLength) {
		boolean[] newArray = new boolean[newLength];
		if (dataSize > 0) {
			System.arraycopy(array, 0, newArray, 0, dataSize);
		}
		return newArray;
	}

	public static byte[] createCopy(byte[] array, int dataSize, int newLength) {
		byte[] newArray = new byte[newLength];
		if (dataSize > 0) {
			System.arraycopy(array, 0, newArray, 0, dataSize);
		}
		return newArray;
	}

	public static char[] createCopy(char[] array, int dataSize, int newLength) {
		char[] newArray = new char[newLength];
		if (dataSize > 0) {
			System.arraycopy(array, 0, newArray, 0, dataSize);
		}
		return newArray;
	}

	public static int[] createCopy(int[] array, int dataSize, int newLength) {
		int[] newArray = new int[newLength];
		if (dataSize > 0) {
			System.arraycopy(array, 0, newArray, 0, dataSize);
		}
		return newArray;
	}

	public static String[] createCopy(String[] array, int dataSize, int newLength) {
		String[] newArray = new String[newLength];
		if (dataSize > 0) {
			System.arraycopy(array, 0, newArray, 0, dataSize);
		}
		return newArray;
	}
}
