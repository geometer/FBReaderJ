/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.pdb.PdbPlugin;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class PluckerPlugin extends PdbPlugin {
	@Override
	public boolean acceptsFile(ZLFile file) {		
		return "DataPlkr".equals(fileType(file));
	}
	
	@Override
	public boolean readMetaInfo(Book book) {
		try {
			PluckerTextStream stream = new PluckerTextStream(book.File);
			if (stream.open()) {
				//detectEncodingAndLanguage(book, stream);
				stream.close();
			}
		} catch (IOException e) {
		}
		
		if (book.getEncoding().length() == 0) {
			return false;
		}

		return true;
	}
	
	@Override
	public boolean readModel(BookModel model)  {
		return new PluckerBookReader(model.Book.File, model, model.Book.getEncoding()).readDocument();
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		return null;
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return null;
	}
}
