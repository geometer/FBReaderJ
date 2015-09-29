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

package org.geometerplus.zlibrary.core.drm;

import org.geometerplus.zlibrary.core.util.MiscUtil;

public class FileEncryptionInfo {
	public final String Uri;
	public final String Method;
	public final String Algorithm;
	public final String ContentId;

	public FileEncryptionInfo(String uri, String method, String algorithm, String contentId) {
		Uri = uri;
		Method = method;
		Algorithm = algorithm;
		ContentId = contentId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FileEncryptionInfo)) {
			return false;
		}
		final FileEncryptionInfo oInfo = (FileEncryptionInfo)other;
		return
			MiscUtil.equals(Uri, oInfo.Uri) &&
			MiscUtil.equals(Method, oInfo.Method) &&
			MiscUtil.equals(Algorithm, oInfo.Algorithm) &&
			MiscUtil.equals(ContentId, oInfo.ContentId);
	}

	@Override
	public int hashCode() {
		return
			MiscUtil.hashCode(Uri) +
			23 * (MiscUtil.hashCode(Method) +
				  23 * (MiscUtil.hashCode(Algorithm) +
						23 * MiscUtil.hashCode(ContentId)));
	}
}
