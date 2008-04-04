package org.zlibrary.core.xml;

import org.zlibrary.core.util.ZLArrayUtils;

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

	public void put(String key, String value) {
		final int size = mySize++;
		String[] keys = myKeys;
		if (keys.length == size) {
			keys = ZLArrayUtils.createCopy(keys, size, size << 1);
			myKeys = keys;
			myValues = ZLArrayUtils.createCopy(myValues, size, size << 1);
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
