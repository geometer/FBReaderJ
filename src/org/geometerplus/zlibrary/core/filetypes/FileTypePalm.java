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

import java.io.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.MimeType;

abstract class FileTypePalm extends FileType {
	protected static String palmFileType(final ZLFile file) {
		// TODO: use database instead of option (?)
		final ZLStringOption palmTypeOption = new ZLStringOption(file.getPath(), "PalmType", "");
		String palmType = palmTypeOption.getValue();
		if (palmType.length() != 8) {
			byte[] id = new byte[8];
			try {
				final InputStream stream = file.getInputStream();
				if (stream == null) {
					return null;
				}
				stream.skip(60);
				stream.read(id);
				stream.close();
			} catch (IOException e) {
			}
			palmType = new String(id).intern();
			palmTypeOption.setValue(palmType);
		}
		return palmType.intern();
	}

	private final String myPalmId;

	FileTypePalm(String id, String palmId) {
		super(id);
		myPalmId = palmId;
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension();
		return
			("pdb".equalsIgnoreCase(extension) || "prc".equalsIgnoreCase(extension)) &&
			myPalmId.equals(palmFileType(file));
	}

	@Override
	public String defaultExtension(MimeType mime) {
		return "pdb";
	}
}
