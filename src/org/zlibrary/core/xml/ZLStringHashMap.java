package org.zlibrary.core.xml;

// optimized partially implemented hashmap String -> String
// key must be not-null
// key must be interned
// there is no remove() in this implementation
// put with the same key does not remove previous entry

public final class ZLStringHashMap {
	private final static class Entry {
		final String Key;
		final int HashCode;
		final String Value;
		Entry Next;

		Entry(String key, int hashCode, String value, Entry next) {
			Key = key;
			HashCode = hashCode;
			Value = value;
			Next = next;
		}
	}

	private Entry[] myTable;
	private int mySize;

	public ZLStringHashMap(int initialCapacity) {
		int capacity;
		for (capacity = 1; capacity < initialCapacity; capacity <<= 1);
		myTable = new Entry[capacity];
	}

	public void put(String key, String value) {
		final int size = ++mySize;
		Entry[] table = myTable;
		int length = table.length;
		if (length <= size * 4 / 3) {
			final int newLength = table.length << 1;
			Entry[] newTable = new Entry[newLength];
			for (int i = 0; i < length; ++i) {
				Entry next;
				for (Entry entry = table[i]; entry != null; entry = next) {
					final int newIndex = entry.HashCode & (newLength - 1);
					next = entry.Next;
					entry.Next = newTable[newIndex];
					newTable[newIndex] = entry;
				}
			}
			table = newTable;
			myTable = table;
			length = newLength;
		}
		final int hashCode = key.hashCode();
		final int index = hashCode & (length - 1);
		table[index] = new Entry(key, hashCode, value, table[index]);
	}

	public String getValue(String key) {
		if (mySize == 0) {
			return null;
		}
		final int hashCode = key.hashCode();
		final Entry[] table = myTable;
		final int index = hashCode & (table.length - 1);
		for (Entry entry = table[index]; entry != null; entry = entry.Next) {
			if (entry.Key == key) {
				return entry.Value;
			}
		}
		return null;
	}

	public int getSize() {
		return mySize;
	}

	public String getKey(int index) {
		final int size = mySize;
		if (index < size) {
			final Entry[] table = myTable;
			final int length = table.length;
			for (int i = 0; i < length; ++i) {
				for (Entry entry = table[i]; entry != null; entry = entry.Next) {
					if (index-- == 0) {
						return entry.Key;
					}
				}
			}
		}
		return null;
	}
  
	public boolean isEmpty() {
		return mySize == 0;
	}

	public void clear() {
		if (mySize != 0) {
			mySize = 0;
			final Entry[] table = myTable;
			final int length = table.length;
			for (int i = length; --i >= 0; ) {
				table[i] = null;
			}
		}
	}
}
