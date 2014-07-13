/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.external;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;

import org.geometerplus.fbreader.book.MetaInfoUtil;

final class PluginImage extends ZLImageProxy {
	private final ZLFile myFile;
	private final ExternalFormatPlugin myPlugin;

	PluginImage(ZLFile file, ExternalFormatPlugin plugin) {
		myFile = file;
		myPlugin = plugin;
	}

	@Override
	protected ZLImage retrieveRealImage(Synchronizer synchronizer) {
		return ((MetaInfoUtil.PluginMetaInfoReader)synchronizer).readImage(myFile, myPlugin);
	}

	@Override
	public SourceType sourceType() {
		return SourceType.SERVICE;
	}

	@Override
	public String getId() {
		return myFile.getPath();
	}
}
