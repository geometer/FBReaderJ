package org.zlibrary.core.xml;

// optimized partially implemented map String -> String
// key must be interned
// there is no remove() in this implementation
// put with the same key does not remove old entry

public final class ZLStringMap {
	private String[] myKeys;
	private String[] myValues;
	private int mySize;

	public ZLStringMap() {
		myKeys = new String[8];
		myValues = new String[8];
	}

	private void extend() {
		final int length = myKeys.length;
		String[] tmp = new String[2 * length];
		System.arraycopy(myKeys, 0, tmp, 0, length);
		myKeys = tmp;
		tmp = new String[2 * length];
		System.arraycopy(myValues, 0, tmp, 0, length);
		myValues = tmp;
	}

	public void put(String key, String value) {
		final int size = mySize++;
		String[] keys = myKeys;
		if (keys.length == size) {
			extend();
		}
		keys[size] = key;
		myValues[size] = value;
	}

	public String getValue(String key) {
		int index = mySize;
		if (index > 0) {
			final String[] keys = myKeys;
			while (--index >= 0) {
				if (keys[index] == key) {
					return myValues[index];
				}
			}
		}
		return null;
	}

	public int getSize() {
		return mySize;
	}

	public String getKey(int index) {
		return myKeys[index];
	}

	public void clear() {
		mySize = 0;
	}
}
