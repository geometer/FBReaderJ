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

package org.geometerplus.zlibrary.core.util;

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

	public final void add(int index, Object element) {
		super.insertElementAt(element, index);
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
