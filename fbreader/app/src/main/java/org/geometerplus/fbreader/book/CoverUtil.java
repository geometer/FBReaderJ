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

package org.geometerplus.fbreader.book;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.formats.IFormatPluginCollection;

public abstract class CoverUtil {
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);
	private static final WeakHashMap<ZLFile,WeakReference<ZLImage>> ourCovers =
		new WeakHashMap<ZLFile,WeakReference<ZLImage>>();

	public static ZLImage getCover(AbstractBook book, IFormatPluginCollection collection) {
		if (book == null) {
			return null;
		}
		synchronized (book) {
			return getCover(ZLFile.createFileByPath(book.getPath()), collection);
		}
	}

	public static ZLImage getCover(ZLFile file, IFormatPluginCollection collection) {
		WeakReference<ZLImage> cover = ourCovers.get(file);
		if (cover == NULL_IMAGE) {
			return null;
		} else if (cover != null) {
			final ZLImage image = cover.get();
			if (image != null) {
				return image;
			}
		}
		ZLImage image = null;
		try {
			image = collection.getPlugin(file).readCover(file);
		} catch (Exception e) {
			// ignore
		}
		ourCovers.put(file, image != null ? new WeakReference<ZLImage>(image) : NULL_IMAGE);
		return image;
	}
}
