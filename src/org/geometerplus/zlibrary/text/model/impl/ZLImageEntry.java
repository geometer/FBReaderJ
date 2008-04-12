/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.model.impl;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageMap;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

public final class ZLImageEntry {
	private final ZLImageMap myImageMap;
	private final String myId;
	public final short VOffset;

	ZLImageEntry(ZLImageMap imageMap, String id, short vOffset) {
		myImageMap = imageMap;
		myId = id;
		VOffset = vOffset;
	}
	
	public String getId() {
		return myId;
	}

	public ZLImage getImage() {
		return myImageMap.getImage(myId);
	}
}
