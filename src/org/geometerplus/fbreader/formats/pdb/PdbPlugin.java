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

package org.geometerplus.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.fbreader.description.BookDescriptionUtil;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.plucker.PluckerTextStream;
import org.geometerplus.fbreader.option.FBOptions;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public abstract class PdbPlugin extends FormatPlugin {
	protected static String fileType(final ZLFile file) {
		final String extension = file.getExtension().toLowerCase().intern();
		if ((extension != "prc") && (extension != "pdb") && (extension != "mobi")) {
			return null;
		}

		String fileName = file.getPath();
		int index = fileName.indexOf(':');
		ZLFile baseFile = (index == -1) ? file : new ZLFile(fileName.substring(0, index));
		boolean upToDate = BookDescriptionUtil.checkInfo(baseFile);

		ZLStringOption palmTypeOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, file.getPath(), "PalmType", "");
		String palmType = palmTypeOption.getValue();
		if ((palmType.length() != 8) || !upToDate) {
			byte[] id = new byte[8];
			try {
				final InputStream stream = file.getInputStream();
				if (stream == null) {
					return null;
				}
				stream.skip(60);
				stream.read(id, 0, 8);
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			palmType = new String(id);
			if (!upToDate) {
				BookDescriptionUtil.saveInfo(baseFile);
			}
			palmTypeOption.setValue(palmType);
		}
		return palmType;
	}
	
	public	String getIconName() {
		return "pdb";
	}
	
	public boolean providesMetaInfo() {
		return false;
	}
}
