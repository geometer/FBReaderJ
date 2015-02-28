/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.formats;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;

public final class PluginImage extends ZLImageProxy {
	public final ZLFile File;
	public final ExternalFormatPlugin Plugin;
	private volatile ZLImage myImage;

	PluginImage(ZLFile file, ExternalFormatPlugin plugin) {
		File = file;
		Plugin = plugin;
	}

	public final synchronized void setRealImage(ZLImage image) {
		if (image != null && !isSynchronized()) {
			myImage = image;
			setSynchronized();
		}
	}

	@Override
	public final ZLImage getRealImage() {
		return myImage;
	}

	@Override
	public SourceType sourceType() {
		return SourceType.SERVICE;
	}

	@Override
	public String getURI() {
		return "cover:" + File.getPath();
	}

	@Override
	public String getId() {
		return File.getPath();
	}
}
