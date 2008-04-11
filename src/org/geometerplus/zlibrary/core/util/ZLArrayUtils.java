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
