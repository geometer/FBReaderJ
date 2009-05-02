/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import java.util.Date;

import org.geometerplus.zlibrary.text.view.impl.ZLTextPosition;

public class Bookmark {
	public final static int CREATION = 0;
	public final static int MODIFICATION = 1;
	public final static int ACCESS = 2;

	private String myText;
	private final Date myCreationDate;
	private Date myModificationDate;
	private Date myAccessDate;
	private int myAccessCount;
	private final ZLTextPosition myPosition;

	Bookmark(String text, Date creationDate, Date modificationDate, Date accessDate, int accessCount, int paragraphIndex, int wordIndex, int charIndex) {
		myText = text;
		myCreationDate = creationDate;
		myModificationDate = modificationDate;
		myAccessDate = accessDate;
		myAccessCount = accessCount;
		myPosition = new ZLTextPosition(paragraphIndex, wordIndex, charIndex);
	}

	public Bookmark(String text, ZLTextPosition position) {
		myText = text;
		myCreationDate = new Date();
		myPosition = position;
	}

	public String getText() {
		return myText;
	}

	public ZLTextPosition getPosition() {
		return myPosition;
	}

	public Date getTime(int timeStamp) {
		switch (timeStamp) {
			default:
			case CREATION:
				return myCreationDate;
			case MODIFICATION:
				return myModificationDate;
			case ACCESS:
				return myAccessDate;
		}
	}

	public int getAccessCount() {
		return myAccessCount;
	}

	boolean setText(String text) {
		if (!text.equals(myText)) {
			myText = text;
			myModificationDate = new Date();
			return true;
		}
		return false;
	}

	void onAccess() {
		myAccessDate = new Date();
		++myAccessCount;
	}
}
