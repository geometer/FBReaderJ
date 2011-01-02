/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.oeb;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;

class OEBCoverReader {
	private static class OEBCoverImage extends ZLImageProxy {
		private final ZLFile myFile;

		OEBCoverImage(ZLFile file) {
			myFile = file;
		}

		@Override
		public ZLSingleImage getRealImage() {
			return new OEBCoverBackgroundReader().readCover(myFile);
		}

		@Override
		public int sourceType() {
			return SourceType.DISK;
		}

		@Override
		public String getId() {
			return myFile.getPath();
		}
	}

	public ZLImageProxy readCover(ZLFile file) {
		return new OEBCoverImage(file);
	}
}
