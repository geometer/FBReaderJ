/*
 * Copyright (C) 2012-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.filetypes;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

class FileTypeFB2 extends FileType {
	FileTypeFB2() {
		super("fb2");
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		final String lName = file.getShortName().toLowerCase();
		return lName.endsWith(".fb2") || lName.endsWith(".fb2.zip");
	}

	private final List<MimeType> myMimeTypes = new ArrayList<MimeType>();

	@Override
	public List<MimeType> mimeTypes() {
		if (myMimeTypes.isEmpty()) {
			myMimeTypes.addAll(MimeType.TYPES_FB2);
			myMimeTypes.addAll(MimeType.TYPES_FB2_ZIP);
		}
		return myMimeTypes;
	}

	@Override
	public MimeType mimeType(ZLFile file) {
		final String lName = file.getShortName().toLowerCase();
		if (lName.endsWith(".fb2")) {
			return MimeType.APP_FB2_XML;
		} else if (lName.endsWith(".fb2.zip")) {
			return MimeType.APP_FB2_ZIP;
		} else {
			return MimeType.NULL;
		}
	}

	@Override
	public MimeType rawMimeType(ZLFile file) {
		final String lName = file.getShortName().toLowerCase();
		if (lName.endsWith(".fb2")) {
			return MimeType.TEXT_XML;
		} else if (lName.endsWith(".fb2.zip")) {
			return MimeType.APP_ZIP;
		} else {
			return MimeType.NULL;
		}
	}

	@Override
	public String defaultExtension(MimeType mime) {
		mime = mime.clean();
		if (MimeType.TYPES_FB2.contains(mime) || MimeType.TEXT_XML.equals(mime)) {
			return "fb2";
		}
		if (MimeType.TYPES_FB2_ZIP.contains(mime) || MimeType.APP_ZIP.equals(mime)) {
			return "fb2.zip";
		}
		return "fb2";
	}
}
