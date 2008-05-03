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

package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.formats.pdb.PdbPlugin;
import org.geometerplus.fbreader.formats.pdb.PdbStream;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class PluckerPlugin extends PdbPlugin {
	public boolean providesMetaInfo() {
		return false;
	}
	
	public boolean acceptsFile(ZLFile file) {		
		return "DataPlkr".equals(fileType(file));
	}
	
	public boolean readDescription(String path, BookDescription description) {
		ZLFile file = new ZLFile(path);

		try {
			PdbStream stream = new PluckerTextStream(file);
			if (stream.open()) {
				detectEncodingAndLanguage(description, stream);
				stream.close();
			}
		} catch (IOException e) {
		}
		
		if (description.getEncoding().length() == 0) {
			return false;
		}

		return true;
	}
	
	public	boolean readModel(BookDescription description, BookModel model)  {
		try {
			return new PluckerBookReader(description.FileName, model, description.getEncoding()).readDocument();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public	String getIconName() {
		return "plucker";
	}
}
