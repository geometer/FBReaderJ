/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.filesystem.*;

public class OEBPlugin extends FormatPlugin {
	public boolean providesMetaInfo() {
		 return true;
	}
	
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension().intern();
		return (extension == "opf") ||
					 (extension == "oebzip") ||
					 (extension == "epub");
	}

	private String getOpfFileName(String oebFileName) {
		final ZLFile oebFile = new ZLFile(oebFileName);
		if (oebFile.getExtension().equals("opf")) {
			return oebFileName;
		}

		oebFile.forceArchiveType(ZLFile.ArchiveType.ZIP);
		final ZLDir zipDir = oebFile.getDirectory(false);
		if (zipDir == null) {
			return null;
		}

		final ArrayList fileNames = zipDir.collectFiles();
		final int len = fileNames.size();
		for (int i = 0; i < len; ++i) {
			final String shortName = (String)fileNames.get(i);
			if (shortName.endsWith(".opf")) {
				return zipDir.getItemPath(shortName);
			}
		}
		return null;
	}

	public boolean readDescription(String path, BookDescription description) {
		path = getOpfFileName(path);
		System.err.println("path = " + path);
		if (path == null) {
			return false;
		}
		return new OEBDescriptionReader(description).readDescription(path);
	}
	
	public boolean readModel(BookDescription description, BookModel model) {
		final String path = getOpfFileName(description.FileName);
		if (path == null) {
			return false;
		}
		return new OEBBookReader(model).readBook(path);
	}
	
	public String getIconName() {
		return "oeb";
	}
}
