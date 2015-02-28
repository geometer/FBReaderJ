/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.text.model;

import java.util.Map;

import org.geometerplus.zlibrary.core.image.ZLImage;

public final class ZLImageEntry {
	private final Map<String,ZLImage> myImageMap;
	public final String Id;
	public final short VOffset;
	public final boolean IsCover;

	ZLImageEntry(Map<String,ZLImage> imageMap, String id, short vOffset, boolean isCover) {
		myImageMap = imageMap;
		Id = id;
		VOffset = vOffset;
		IsCover = isCover;
	}

	public ZLImage getImage() {
		return myImageMap.get(Id);
	}
}
