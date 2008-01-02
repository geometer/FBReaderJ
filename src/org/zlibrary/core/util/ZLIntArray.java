package org.zlibrary.core.util;

public final class ZLIntArray {
	private int[] myData;
	private int mySize;

	public ZLIntArray(int initialCapacity) {
		myData = new int[initialCapacity];
	}

	public void add(int number) {
		if (myData.length == mySize) {
			int[] data = new int[2 * mySize];
			System.arraycopy(myData, 0, data, 0, mySize);
			myData = data;
		}
		myData[mySize++] = number;
	}

	public int get(int index) {
		return myData[index];
	}

	public void increment(int index) {
		++myData[index];
	}

	public int size() {
		return mySize;
	}
}
