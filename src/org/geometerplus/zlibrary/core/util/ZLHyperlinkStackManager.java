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

import java.util.Iterator;
import java.util.Stack;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public abstract class ZLHyperlinkStackManager {
	private static ZLHyperlinkStackManager ourInstance;
	private static Stack<ZLTextPosition> positionStack;
	
	public static ZLHyperlinkStackManager Instance() {
		return ourInstance;
	}

	protected ZLHyperlinkStackManager() {
		ourInstance = this;
		reset();
	}

	public void pushPosition(ZLTextPosition position) {
		ZLTextFixedPosition fixedPosition = new ZLTextFixedPosition(position);
		positionStack.push(fixedPosition);
	}

	public boolean emptyStack() {
		return positionStack.empty();
	}
	
	public ZLTextPosition popPosition() {
		return positionStack.pop();
	}

	public Iterator<ZLTextPosition> getIterator() {
		return positionStack.iterator();
	}
	
	public void reset() {
		positionStack = new Stack<ZLTextPosition>();
	}
}
