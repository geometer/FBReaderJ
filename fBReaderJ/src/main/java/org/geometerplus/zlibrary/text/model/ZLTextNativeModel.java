/*
 * Copyright (C) 2011-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class ZLTextNativeModel extends ZLTextPlainModel {
	public ZLTextNativeModel(
		String id, String language, int paragraphsNumber,
		int[] entryIndices, int[] entryOffsets,
		int[] paragraphLengths, int[] textSizes,
		byte[] paragraphKinds,
		String directoryName, String fileExtension, int blocksNumber,
		Map<String,ZLImage> imageMap,
		FontManager fontManager
	) {
		super(
			id, language,
			entryIndices, entryOffsets, paragraphLengths, textSizes, paragraphKinds,
			new CachedCharStorageRO(directoryName, fileExtension, blocksNumber),
			imageMap,
			fontManager
		);
		myParagraphsNumber = paragraphsNumber;
	}
}
