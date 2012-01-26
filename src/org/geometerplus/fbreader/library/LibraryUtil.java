/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;

public abstract class LibraryUtil {
	private static final HashMap<String,WeakReference<ZLImage>> ourCoverMap =
		new HashMap<String,WeakReference<ZLImage>>();
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);

	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	public static ZLImage getCover(ZLFile file) {
		if (file == null) {
			return null;
		}
		synchronized (ourCoverMap) {
			final String path = file.getPath();
			final WeakReference<ZLImage> ref = ourCoverMap.get(path);
			if (ref == NULL_IMAGE) {
				return null;
			} else if (ref != null) {
				final ZLImage image = ref.get();
				if (image != null) {
					return image;
				}
			}
			ZLImage image = null;
			final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
			if (plugin != null) {
				image = plugin.readCover(file);
			}
			if (image == null) {
				ourCoverMap.put(path, NULL_IMAGE);
			} else {
				ourCoverMap.put(path, new WeakReference<ZLImage>(image));
			}
			return image;
		}
	}

	public static String getAnnotation(ZLFile file) {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
		return plugin != null ? plugin.readAnnotation(file) : null;
	}
}
