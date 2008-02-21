package org.zlibrary.core.util;

import java.util.Vector;

public class ArrayList extends Vector {
	public ArrayList() {
	}

	public ArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public final Object get(int index) {
		return super.elementAt(index);
	}

	public final void add(Object element) {
		super.addElement(element);
	}

	public final void set(int index, Object element) {
		super.setElementAt(element, index);
	}

	public final void remove(int index) {
		super.removeElementAt(index);
	}

	public final void remove(Object element) {
		super.removeElement(element);
	}

	public final void clear() {
		super.removeAllElements();
	}
}
