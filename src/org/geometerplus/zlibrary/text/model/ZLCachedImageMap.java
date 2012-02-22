/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

import java.util.HashMap;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageMap;

public class ZLCachedImageMap implements ZLImageMap {
	private final HashMap<String, ZLImage> myImagesMap = new HashMap<String, ZLImage>();
	private final HashMap<String, Integer> myIdsMap = new HashMap<String, Integer>();
	private final int[] myIndices;
	private final int[] myOffsets;

	private final ZLImageMapReader myReader;

	public ZLCachedImageMap(
		String[] ids, int[] indices, int[] offsets,
		String directoryName, String fileExtension, int blocksNumber
	) {
		myIndices = indices;
		myOffsets = offsets;
		for (int i = 0; i < ids.length; ++i) {
			final Integer before = myIdsMap.put(ids[i], i);
			if (before != null) {
				System.err.println("FBREADER: more than one image with id=\"" + ids[i] + "\" -- number " + before + " and number " + i);
			}
		}
		myReader = new ZLImageMapReader(directoryName, fileExtension, blocksNumber);
	}

	public ZLImage getImage(String id) {
		ZLImage image = myImagesMap.get(id);
		if (image == null) {
			final Integer pos = myIdsMap.get(id);
			if (pos != null) {
				image = myReader.readImage(myIndices[pos], myOffsets[pos]);
				myImagesMap.put(id, image);
			}
		}
		return image;
	}
}
