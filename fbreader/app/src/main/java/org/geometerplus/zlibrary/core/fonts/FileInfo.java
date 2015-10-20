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

package org.geometerplus.zlibrary.core.fonts;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;

public final class FileInfo {
	public final String Path;
	public final FileEncryptionInfo EncryptionInfo;

	public FileInfo(String path, FileEncryptionInfo encryptionInfo) {
		Path = path;
		EncryptionInfo = encryptionInfo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FileInfo)) {
			return false;
		}
		final FileInfo oInfo = (FileInfo)other;
		return Path.equals(oInfo.Path) && ComparisonUtil.equal(EncryptionInfo, oInfo.EncryptionInfo);
	}

	@Override
	public int hashCode() {
		return Path.hashCode() + 23 * ComparisonUtil.hashCode(EncryptionInfo);
	}
}
