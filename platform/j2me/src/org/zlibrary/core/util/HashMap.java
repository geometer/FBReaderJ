package org.geometerplus.zlibrary.core.util;

import java.util.Hashtable;

public class HashMap extends Hashtable {
	public HashMap() {
	}

	public HashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity);
	}

	public final void put(Object key, int value) {
		super.put(key, new Integer(value));
	}

	public final void put(Object key, byte value) {
		super.put(key, new Byte(value));
	}
}
