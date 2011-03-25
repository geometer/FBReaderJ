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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public class ZLHyperlinkHistoryManager {
	private static ZLHyperlinkHistoryManager ourInstance;
	private static List<ZLTextPosition> positionList;
	private static int index;

	public static ZLHyperlinkHistoryManager Instance() {
		if (ourInstance == null) {
			ourInstance = new ZLHyperlinkHistoryManager();
		}
		return ourInstance;
	}

	private ZLHyperlinkHistoryManager() {
		reset();
	}

	public ZLTextPosition back(ZLTextPosition position) {
		if (index == 0) {
			return null;
		}
		ZLTextFixedPosition fixedPosition = new ZLTextFixedPosition(position);
		if (index >= positionList.size()) {
			positionList.add(fixedPosition);
		} else {
			positionList.set(index, fixedPosition);
		}
		index = index - 1;
		return positionList.get(index);
	}

	public ZLTextPosition forward(ZLTextPosition position) {
		if (index + 1 >= positionList.size()) {
			return null;
		}
		ZLTextFixedPosition fixedPosition = new ZLTextFixedPosition(position);
		positionList.set(index, fixedPosition);
		index = index + 1;
		return positionList.get(index);
	}

	public void visit(ZLTextPosition position) {
		ZLTextFixedPosition fixedPosition = new ZLTextFixedPosition(position);
		if (index >= positionList.size()) {
			positionList.add(fixedPosition);
		} else {
			positionList.set(index, fixedPosition);
			for (int i = positionList.size() - 1; i > index; i--) {
				positionList.remove(i);
			}
		}
		index = index + 1;
	}

	public boolean hasBackHistory() {
		return index > 0;
	}

	public boolean hasForwardHistory() {
		return index + 1 < positionList.size();
	}

	public Iterator<ZLTextPosition> getIterator() {
		return positionList.iterator();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int value) {
		index = value;
	}

	public void addPosition(ZLTextPosition position) {
		positionList.add(position);
	}

	public void reset() {
		positionList = new ArrayList<ZLTextPosition>();
		index = 0;
	}
}
