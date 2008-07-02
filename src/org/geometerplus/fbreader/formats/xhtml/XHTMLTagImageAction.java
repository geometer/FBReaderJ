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

package org.geometerplus.fbreader.formats.xhtml;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.image.ZLFileImage;

import org.geometerplus.fbreader.bookmodel.BookReader;

class XHTMLTagImageAction extends XHTMLTagAction {
	private final String myNameAttribute;

	XHTMLTagImageAction(String nameAttribute) {
		myNameAttribute = nameAttribute;
	}

	protected void doAtStart(XHTMLReader reader, ZLStringMap xmlattributes) {
		String fileName = xmlattributes.getValue(myNameAttribute);
		if (fileName != null) {
			final BookReader modelReader = reader.getModelReader();
			boolean flag = modelReader.paragraphIsOpen();
			if (flag) {
				modelReader.endParagraph();
			}
			if (fileName.startsWith("./")) {
				fileName = fileName.substring(2);
			}
			final String fullfileName = reader.getPathPrefix() + fileName;
			modelReader.addImageReference(fullfileName, (short)0);
			modelReader.addImage(fullfileName, new ZLFileImage("image/auto", fullfileName));
			if (flag) {
				modelReader.beginParagraph();
			}
		}
	}

	protected void doAtEnd(XHTMLReader reader) {
	}
}
